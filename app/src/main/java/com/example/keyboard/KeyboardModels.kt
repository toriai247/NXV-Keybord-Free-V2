package com.example.keyboard

enum class KeyboardMode {
    ENGLISH,
    BANGLA,
    AVRO,
    NUMBERS,
    SYMBOLS,
    EMOJI,
    STICKERS,
    CLIPBOARD,
    VAULT,
    TEXT_EDIT,
    NUMBER_PAD,
    VOICE,
    MEDIA,
    AI
}

enum class OneHandedMode(val modeKey: String) {
    NORMAL("none"),
    LEFT("left"),
    RIGHT("right");

    val isOneHanded: Boolean get() = this != NORMAL
    val isLeft: Boolean get() = this == LEFT
    val isRight: Boolean get() = this == RIGHT

    companion object {
        fun fromString(str: String?): OneHandedMode = when (str?.lowercase()) {
            "left" -> LEFT
            "right" -> RIGHT
            else -> NORMAL
        }
    }
}

enum class ShiftState {
    LOWERCASE,
    SHIFT_ONCE,
    CAPS_LOCK,
    MANUAL_UPPERCASE;

    val isUppercase: Boolean
        get() = this == SHIFT_ONCE || this == CAPS_LOCK || this == MANUAL_UPPERCASE

    val isCapsLock: Boolean
        get() = this == CAPS_LOCK || this == MANUAL_UPPERCASE

    val isShiftOnce: Boolean
        get() = this == SHIFT_ONCE

    val isLowercase: Boolean
        get() = this == LOWERCASE

    companion object {
        val OFF: ShiftState get() = LOWERCASE
        val ON: ShiftState get() = SHIFT_ONCE
    }
}

sealed interface KeyAction {
    data class Character(
        val char: String,
        val shiftChar: String? = null,
        val altSymbol: String? = null
    ) : KeyAction

    object Space : KeyAction
    object Backspace : KeyAction
    object Enter : KeyAction
    object Shift : KeyAction
    object LanguageSwitch : KeyAction
    object ShowLanguageSelector : KeyAction
    data class SwitchMode(val targetMode: KeyboardMode) : KeyAction
    object CloseKeyboard : KeyAction
}
