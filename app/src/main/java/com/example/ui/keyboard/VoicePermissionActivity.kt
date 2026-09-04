package com.example.ui.keyboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Transparent helper activity to request RECORD_AUDIO runtime permission from IME service or UI.
 * Dismisses cleanly without animation upon permission response.
 */
class VoicePermissionActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResultListener?.invoke(isGranted)
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            onPermissionResultListener?.invoke(true)
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    companion object {
        var onPermissionResultListener: ((Boolean) -> Unit)? = null

        fun requestPermission(context: Context, onResult: ((Boolean) -> Unit)? = null) {
            onPermissionResultListener = onResult
            val intent = Intent(context, VoicePermissionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
            context.startActivity(intent)
        }
    }
}
