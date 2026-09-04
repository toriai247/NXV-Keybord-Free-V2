package com.example.data

import com.example.data.dao.ClipboardDao
import com.example.data.dao.CredentialDao
import com.example.data.dao.DictionaryDao
import com.example.data.dao.ShortcutDao
import com.example.data.entity.ClipboardItem
import com.example.data.entity.DictionaryWord
import com.example.data.entity.SavedCredential
import com.example.data.entity.TextShortcut
import com.example.security.EncryptedCredentialStorageService
import kotlinx.coroutines.flow.Flow

class KeyboardDataRepository(
    private val clipboardDao: ClipboardDao,
    private val shortcutDao: ShortcutDao,
    private val dictionaryDao: DictionaryDao,
    private val credentialDao: CredentialDao,
    val encryptedCredentialStorage: EncryptedCredentialStorageService? = null
) {
    // Clipboard
    val clipboardItems: Flow<List<ClipboardItem>> = clipboardDao.getAllItems()

    suspend fun addClipboardItem(text: String, isPinned: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val existing = clipboardDao.findByText(trimmed)
        if (existing != null) {
            clipboardDao.updateTimestamp(existing.id, System.currentTimeMillis())
        } else {
            clipboardDao.insertItem(
                ClipboardItem(
                    text = trimmed,
                    timestamp = System.currentTimeMillis(),
                    isPinned = isPinned
                )
            )
        }
    }

    suspend fun togglePinClipboardItem(item: ClipboardItem) {
        clipboardDao.updateItem(item.copy(isPinned = !item.isPinned))
    }

    suspend fun deleteClipboardItem(id: Long) {
        clipboardDao.deleteById(id)
    }

    suspend fun clearClipboard(keepPinned: Boolean = true) {
        if (keepPinned) {
            clipboardDao.clearUnpinned()
        } else {
            clipboardDao.clearAll()
        }
    }

    // Saved Passwords / Credentials Vault via EncryptedSharedPreferences
    val savedCredentials: Flow<List<SavedCredential>> =
        encryptedCredentialStorage?.credentialsFlow ?: credentialDao.getAllCredentials()

    suspend fun getCredentialsForPackageOrQuery(packageName: String, query: String = ""): List<SavedCredential> {
        return encryptedCredentialStorage?.getCredentialsForPackageOrQuery(packageName, query)
            ?: credentialDao.getCredentialsFor(packageName, query)
    }

    suspend fun saveCredential(
        serviceName: String,
        username: String,
        password: String,
        packageName: String = "",
        isPinned: Boolean = false
    ): Long {
        if (password.isBlank()) return -1L
        val cleanService = if (serviceName.isNotBlank()) serviceName.trim() else packageName.ifBlank { "Account" }
        
        // Save to Encrypted Storage
        val savedEncrypted = encryptedCredentialStorage?.saveCredential(
            serviceName = cleanService,
            username = username.trim(),
            password = password,
            packageName = packageName,
            isPinned = isPinned
        )

        // Also sync to Room database
        val existing = credentialDao.findMatching(cleanService, username.trim())
        val roomResultId = if (existing != null) {
            val updated = existing.copy(
                password = password,
                packageName = packageName.ifBlank { existing.packageName },
                timestamp = System.currentTimeMillis()
            )
            credentialDao.updateCredential(updated)
            existing.id
        } else {
            credentialDao.insertCredential(
                SavedCredential(
                    serviceName = cleanService,
                    username = username.trim(),
                    password = password,
                    packageName = packageName,
                    timestamp = System.currentTimeMillis(),
                    isPinned = isPinned
                )
            )
        }
        return savedEncrypted?.id ?: roomResultId
    }

    suspend fun deleteCredential(id: Long) {
        encryptedCredentialStorage?.deleteCredential(id)
        credentialDao.deleteById(id)
    }

    suspend fun clearAllCredentials() {
        encryptedCredentialStorage?.clearAllCredentials()
        credentialDao.clearAll()
    }

    suspend fun togglePinCredential(credential: SavedCredential) {
        encryptedCredentialStorage?.togglePinCredential(credential)
        credentialDao.updateCredential(credential.copy(isPinned = !credential.isPinned))
    }

    // Shortcuts
    val shortcuts: Flow<List<TextShortcut>> = shortcutDao.getAllShortcuts()

    suspend fun getShortcutExpansion(input: String): String? {
        return shortcutDao.getExpansionFor(input.trim())?.expansion
    }

    suspend fun addOrUpdateShortcut(shortcut: String, expansion: String) {
        if (shortcut.isNotBlank() && expansion.isNotBlank()) {
            shortcutDao.insertShortcut(
                TextShortcut(
                    shortcut = shortcut.trim().lowercase(),
                    expansion = expansion.trim()
                )
            )
        }
    }

    suspend fun deleteShortcut(id: Long) {
        shortcutDao.deleteById(id)
    }

    // Dictionary / Learned Words
    val allDictionaryWords: Flow<List<DictionaryWord>> = dictionaryDao.getAllWords()

    suspend fun getWordSuggestions(prefix: String, locale: String): List<String> {
        val matching = dictionaryDao.getWordsMatching(locale, prefix).map { it.word }
        if (matching.isNotEmpty()) return matching
        return dictionaryDao.getWordsMatchingAny(prefix).map { it.word }
    }

    suspend fun learnWord(word: String, locale: String) {
        val trimmed = word.trim()
        if (trimmed.length <= 1) return
        val rowId = dictionaryDao.insertWord(DictionaryWord(word = trimmed, frequency = 1, locale = locale))
        if (rowId == -1L) {
            dictionaryDao.incrementFrequency(trimmed, locale)
        }
    }

    suspend fun deleteWord(id: Long) {
        dictionaryDao.deleteById(id)
    }

    suspend fun clearDictionary() {
        dictionaryDao.clearAll()
    }
}
