package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.KeyboardDataRepository
import com.example.data.preferences.KeyboardPreferences
import com.example.security.EncryptedCredentialStorageService

class NXVApplication : Application() {

    lateinit var preferences: KeyboardPreferences
        private set

    lateinit var credentialStorageService: EncryptedCredentialStorageService
        private set

    lateinit var repository: KeyboardDataRepository
        private set

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = KeyboardPreferences(this)
        database = AppDatabase.getInstance(this)
        credentialStorageService = EncryptedCredentialStorageService(this)
        repository = KeyboardDataRepository(
            clipboardDao = database.clipboardDao(),
            shortcutDao = database.shortcutDao(),
            dictionaryDao = database.dictionaryDao(),
            credentialDao = database.credentialDao(),
            encryptedCredentialStorage = credentialStorageService
        )
    }

    companion object {
        lateinit var instance: NXVApplication
            private set
    }
}

