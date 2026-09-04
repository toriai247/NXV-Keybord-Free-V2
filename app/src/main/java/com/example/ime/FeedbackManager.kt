package com.example.ime

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

class FeedbackManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playKeySound(enabled: Boolean) {
        if (!enabled) return
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.4f)
        } catch (_: Exception) {}
    }

    fun performHapticFeedback(view: View?, enabled: Boolean, strength: String) {
        if (!enabled) return
        try {
            // Priority 1: View haptic feedback
            if (view != null) {
                val constant = when (strength.lowercase()) {
                    "low" -> HapticFeedbackConstants.KEYBOARD_TAP
                    "high" -> HapticFeedbackConstants.LONG_PRESS
                    else -> HapticFeedbackConstants.KEYBOARD_TAP
                }
                val performed = view.performHapticFeedback(constant)
                if (performed) return
            }

            // Priority 2: Direct Vibrator
            val vib = vibrator ?: return
            if (!vib.hasVibrator()) return

            val durationMs = when (strength.lowercase()) {
                "low" -> 10L
                "high" -> 35L
                else -> 20L
            }

            val amplitude = when (strength.lowercase()) {
                "low" -> 50
                "high" -> 200
                else -> 120
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
