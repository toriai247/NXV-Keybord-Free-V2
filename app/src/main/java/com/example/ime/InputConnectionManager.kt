package com.example.ime

import android.os.Build
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import java.text.BreakIterator

class InputConnectionManager {

    private var inputConnection: InputConnection? = null
    private var editorInfo: EditorInfo? = null

    // Track active composing text (especially for Avro phonetic live typing)
    private var composingWord: StringBuilder = StringBuilder()

    fun update(ic: InputConnection?, info: EditorInfo?) {
        this.inputConnection = ic
        this.editorInfo = info
        this.composingWord.clear()
    }

    fun hasComposingText(): Boolean = composingWord.isNotEmpty()

    fun getComposingText(): String = composingWord.toString()

    fun clearComposing() {
        if (composingWord.isNotEmpty()) {
            inputConnection?.finishComposingText()
            composingWord.clear()
        }
    }

    fun isPasswordField(): Boolean {
        val info = editorInfo ?: return false
        val inputType = info.inputType
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val clazz = inputType and InputType.TYPE_MASK_CLASS

        if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == 0x80 || // TYPE_TEXT_VARIATION_PASSWORD
            variation == 0x90 || // TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            variation == 0xe0    // TYPE_TEXT_VARIATION_WEB_PASSWORD
        ) {
            return true
        }

        if (clazz == InputType.TYPE_CLASS_NUMBER && (inputType and InputType.TYPE_NUMBER_VARIATION_PASSWORD) != 0) {
            return true
        }

        // Additional hint detection
        val hint = info.hintText?.toString()?.lowercase() ?: ""
        if (hint.contains("password") || hint.contains("passcode") || hint.contains("pin code")) {
            return true
        }

        return false
    }

    fun isEmailOrAccountField(): Boolean {
        val info = editorInfo ?: return false
        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ||
            variation == InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        ) {
            return true
        }
        val hint = info.hintText?.toString()?.lowercase() ?: ""
        return hint.contains("email") || hint.contains("username") || hint.contains("login") || hint.contains("account")
    }

    fun isEmailField(): Boolean {
        val info = editorInfo ?: return false
        return (info.inputType and InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    }

    fun isUrlField(): Boolean {
        val info = editorInfo ?: return false
        return (info.inputType and InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_URI
    }

    fun isNumberOnlyField(): Boolean {
        val info = editorInfo ?: return false
        val clazz = info.inputType and InputType.TYPE_MASK_CLASS
        return clazz == InputType.TYPE_CLASS_NUMBER || clazz == InputType.TYPE_CLASS_PHONE
    }

    fun isMultiline(): Boolean {
        val info = editorInfo ?: return false
        return (info.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
    }

    fun commitText(text: String) {
        val ic = inputConnection ?: return
        if (composingWord.isNotEmpty()) {
            ic.finishComposingText()
            composingWord.clear()
        }
        ic.commitText(text, 1)
    }

    fun setComposing(text: String, rawTypedWord: String) {
        val ic = inputConnection ?: return
        composingWord.clear()
        composingWord.append(rawTypedWord)
        ic.setComposingText(text, 1)
    }

    fun finishComposing(commitAs: String? = null) {
        val ic = inputConnection ?: return
        if (commitAs != null) {
            ic.commitText(commitAs, 1)
        } else {
            ic.finishComposingText()
        }
        composingWord.clear()
    }

    /**
     * Grapheme-aware safe backspace deletion.
     * Prevents breaking combining Bengali Unicode clusters and surrogate pairs.
     * Accurately handles characters like বাংলা, ভালো, আমি, বাংলাদেশ and emoji clusters.
     */
    fun handleDelete(): Boolean {
        val ic = inputConnection ?: return false

        // 1. If currently in active composing word (e.g. Avro live typing)
        if (composingWord.isNotEmpty()) {
            composingWord.deleteCharAt(composingWord.length - 1)
            if (composingWord.isEmpty()) {
                try {
                    ic.finishComposingText()
                } catch (_: Exception) {}
                return false
            }
            return true // Caller should re-evaluate composing text with remaining chars
        }

        // 2. Safe deletion of selected text if any
        val selected = try {
            ic.getSelectedText(0)
        } catch (_: Exception) {
            null
        }
        if (!selected.isNullOrEmpty()) {
            try {
                ic.commitText("", 1)
            } catch (_: Exception) {
                sendDelKeyEvent(ic)
            }
            return false
        }

        // 3. Inspect text before cursor to find exact grapheme cluster boundary
        val textBefore = try {
            ic.getTextBeforeCursor(64, 0)?.toString()
        } catch (_: Exception) {
            null
        }

        if (textBefore.isNullOrEmpty()) {
            sendDelKeyEvent(ic)
            return false
        }

        // Calculate how many UTF-16 code units comprise the last logical grapheme cluster
        val charsToDelete = calculateLastGraphemeLength(textBefore)

        val deleted = try {
            ic.deleteSurroundingText(charsToDelete, 0)
        } catch (_: Exception) {
            false
        }

        if (!deleted) {
            sendDelKeyEvent(ic)
        }
        return false
    }

    private fun sendDelKeyEvent(ic: InputConnection) {
        try {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        } catch (_: Exception) {}
    }

    /**
     * Accurately calculates the length of the last user-perceived character (grapheme cluster).
     * Prevents splitting surrogate pairs (emoji) and deletes combining vowel signs (kar)
     * one logical component at a time without corrupting the base consonant.
     */
    fun calculateLastGraphemeLength(text: String): Int {
        if (text.isEmpty()) return 0
        if (text.length == 1) return 1

        val lastChar = text.last()

        // 1. Emoji & Surrogate pairs (e.g. U+1F60A 😊, flags, multi-part emojis)
        if (Character.isSurrogate(lastChar)) {
            return try {
                val boundary = BreakIterator.getCharacterInstance()
                boundary.setText(text)
                val end = boundary.last()
                val start = boundary.previous()
                if (start != BreakIterator.DONE && end > start) {
                    end - start
                } else if (text.length >= 2 && Character.isSurrogate(text[text.length - 2])) {
                    2
                } else {
                    1
                }
            } catch (_: Exception) {
                if (text.length >= 2 && Character.isSurrogate(text[text.length - 2])) 2 else 1
            }
        }

        // 2. Combining vowel marks (kar), hasanta/virama, and diacritics in Bangla & Unicode.
        // Single tap on Backspace deletes one combining mark at a time, preserving the base consonant.
        val type = Character.getType(lastChar)
        val isBanglaCombiningMark = lastChar in '\u0981'..'\u0983' || // candrabindu, anusvara, visarga
                lastChar == '\u09BC' || // nukta
                lastChar in '\u09BE'..'\u09CD' || // vowel signs aa through au, and virama/hasanta
                lastChar == '\u09D7' // au length mark
        if (isBanglaCombiningMark ||
            type == Character.NON_SPACING_MARK.toInt() ||
            type == Character.COMBINING_SPACING_MARK.toInt()
        ) {
            return 1
        }

        // 3. For base characters and other scripts, use BreakIterator
        return try {
            val boundary = BreakIterator.getCharacterInstance()
            boundary.setText(text)
            val end = boundary.last()
            val start = boundary.previous()
            if (start != BreakIterator.DONE && end > start) {
                end - start
            } else {
                1
            }
        } catch (_: Exception) {
            1
        }
    }

    fun moveCursor(offset: Int) {
        val ic = inputConnection ?: return
        if (offset == 0) return
        val keyCode = if (offset < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        val count = Math.abs(offset)
        for (i in 0 until count) {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }
    }

    fun moveCursorUp(select: Boolean = false) {
        val ic = inputConnection ?: return
        sendDirectionKey(ic, KeyEvent.KEYCODE_DPAD_UP, select)
    }

    fun moveCursorDown(select: Boolean = false) {
        val ic = inputConnection ?: return
        sendDirectionKey(ic, KeyEvent.KEYCODE_DPAD_DOWN, select)
    }

    fun moveCursorLeft(select: Boolean = false) {
        val ic = inputConnection ?: return
        sendDirectionKey(ic, KeyEvent.KEYCODE_DPAD_LEFT, select)
    }

    fun moveCursorRight(select: Boolean = false) {
        val ic = inputConnection ?: return
        sendDirectionKey(ic, KeyEvent.KEYCODE_DPAD_RIGHT, select)
    }

    fun moveToStartOfLine(select: Boolean = false) {
        val ic = inputConnection ?: return
        sendDirectionKey(ic, KeyEvent.KEYCODE_MOVE_HOME, select)
    }

    fun moveToEndOfLine(select: Boolean = false) {
        val ic = inputConnection ?: return
        sendDirectionKey(ic, KeyEvent.KEYCODE_MOVE_END, select)
    }

    private fun sendDirectionKey(ic: InputConnection, keyCode: Int, select: Boolean) {
        try {
            val eventTime = android.os.SystemClock.uptimeMillis()
            val metaState = if (select) KeyEvent.META_SHIFT_ON else 0
            ic.sendKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
            ic.sendKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0, metaState))
        } catch (_: Exception) {}
    }

    fun selectAll() {
        val ic = inputConnection ?: return
        try {
            ic.performContextMenuAction(android.R.id.selectAll)
        } catch (_: Exception) {}
    }

    fun copyText() {
        val ic = inputConnection ?: return
        try {
            ic.performContextMenuAction(android.R.id.copy)
        } catch (_: Exception) {}
    }

    fun pasteText() {
        val ic = inputConnection ?: return
        try {
            ic.performContextMenuAction(android.R.id.paste)
        } catch (_: Exception) {}
    }

    fun cutText() {
        val ic = inputConnection ?: return
        try {
            ic.performContextMenuAction(android.R.id.cut)
        } catch (_: Exception) {}
    }

    fun getTextBeforeCursor(length: Int = 100): String {
        val ic = inputConnection ?: return ""
        return ic.getTextBeforeCursor(length, 0)?.toString() ?: ""
    }

    fun handleEnter() {
        val ic = inputConnection ?: return
        finishComposing()

        val info = editorInfo
        val imeAction = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE

        if (imeAction != EditorInfo.IME_ACTION_NONE &&
            imeAction != EditorInfo.IME_ACTION_UNSPECIFIED &&
            !isMultiline()
        ) {
            ic.performEditorAction(imeAction)
        } else {
            ic.commitText("\n", 1)
        }
    }

    fun getCurrentWordBeforeCursor(): String {
        val ic = inputConnection ?: return ""
        val text = ic.getTextBeforeCursor(40, 0)?.toString() ?: return ""
        if (text.isEmpty()) return ""

        val lastIndex = text.indexOfLast { it.isWhitespace() || it in ".,;:!?()[]{}\"'/\\-+=|~" }
        return if (lastIndex == -1) {
            text
        } else {
            text.substring(lastIndex + 1)
        }
    }

    fun replaceCurrentWord(replacement: String) {
        val ic = inputConnection ?: return
        if (composingWord.isNotEmpty()) {
            ic.commitText(replacement, 1)
            composingWord.clear()
            return
        }

        val currentWord = getCurrentWordBeforeCursor()
        if (currentWord.isNotEmpty()) {
            ic.deleteSurroundingText(currentWord.length, 0)
        }
        ic.commitText(replacement, 1)
    }

    fun getEnterActionLabel(): String {
        val info = editorInfo ?: return "↵"
        return when (info.imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_GO -> "Go"
            EditorInfo.IME_ACTION_SEARCH -> "Search"
            EditorInfo.IME_ACTION_SEND -> "Send"
            EditorInfo.IME_ACTION_NEXT -> "Next"
            EditorInfo.IME_ACTION_DONE -> "Done"
            else -> "↵"
        }
    }
}
