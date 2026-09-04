package com.example.util

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

object ImeStatusHelper {
    /**
     * Checks whether NXV Keyboard is enabled in Android System Settings.
     */
    fun isKeyboardEnabled(context: Context): Boolean {
        return try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                ?: return false
            val enabledList = imm.enabledInputMethodList ?: return false
            val packageName = context.packageName
            enabledList.any { it.packageName == packageName }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks whether NXV Keyboard is currently selected as the active/default input method.
     */
    fun isKeyboardSelected(context: Context): Boolean {
        return try {
            val currentIme = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            ) ?: return false
            currentIme.contains(context.packageName)
        } catch (_: Exception) {
            false
        }
    }
}
