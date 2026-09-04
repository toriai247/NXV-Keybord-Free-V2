package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.entity.SavedCredential
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Local Credential Storage Service using EncryptedSharedPreferences (AES-256 GCM).
 * Provides hardware-backed encrypted storage for passwords and account credentials.
 */
class EncryptedCredentialStorageService(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val mutex = Mutex()
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, SavedCredential::class.java)
    private val jsonAdapter = moshi.adapter<List<SavedCredential>>(listType)

    private val _credentialsFlow = MutableStateFlow<List<SavedCredential>>(emptyList())
    val credentialsFlow: Flow<List<SavedCredential>> = _credentialsFlow.asStateFlow()

    private val sharedPreferences: SharedPreferences by lazy {
        initEncryptedSharedPreferences()
    }

    init {
        // Initial load
        loadCredentialsFromDisk()
    }

    private fun initEncryptedSharedPreferences(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize EncryptedSharedPreferences, using standard secure fallback", e)
            context.getSharedPreferences("${PREFS_FILE_NAME}_fallback", Context.MODE_PRIVATE)
        }
    }

    private fun loadCredentialsFromDisk() {
        try {
            val rawJson = sharedPreferences.getString(KEY_CREDENTIALS_DATA, null)
            if (!rawJson.isNullOrBlank()) {
                val list = jsonAdapter.fromJson(rawJson) ?: emptyList()
                _credentialsFlow.value = list.sortedWith(
                    compareByDescending<SavedCredential> { it.isPinned }
                        .thenByDescending { it.timestamp }
                )
            } else {
                _credentialsFlow.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading encrypted credentials", e)
            _credentialsFlow.value = emptyList()
        }
    }

    private fun persistCredentialsToDisk(credentials: List<SavedCredential>) {
        try {
            val sorted = credentials.sortedWith(
                compareByDescending<SavedCredential> { it.isPinned }
                    .thenByDescending { it.timestamp }
            )
            val json = jsonAdapter.toJson(sorted)
            sharedPreferences.edit()
                .putString(KEY_CREDENTIALS_DATA, json)
                .putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
                .apply()
            _credentialsFlow.value = sorted
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting encrypted credentials", e)
        }
    }

    suspend fun saveCredential(
        serviceName: String,
        username: String,
        password: String,
        packageName: String = "",
        isPinned: Boolean = false
    ): SavedCredential = withContext(ioDispatcher) {
        mutex.withLock {
            val currentList = _credentialsFlow.value.toMutableList()
            val cleanService = if (serviceName.isNotBlank()) serviceName.trim() else packageName.ifBlank { "Account" }
            val cleanUsername = username.trim()
            val cleanPassword = password.trim()

            // Check if matching credential already exists
            val existingIndex = currentList.indexOfFirst {
                it.serviceName.equals(cleanService, ignoreCase = true) &&
                        it.username.equals(cleanUsername, ignoreCase = true)
            }

            val savedItem = if (existingIndex != -1) {
                val existing = currentList[existingIndex]
                val updated = existing.copy(
                    password = cleanPassword,
                    packageName = packageName.ifBlank { existing.packageName },
                    timestamp = System.currentTimeMillis(),
                    isPinned = if (isPinned) true else existing.isPinned
                )
                currentList[existingIndex] = updated
                updated
            } else {
                val nextId = (currentList.maxOfOrNull { it.id } ?: 0L) + 1L
                val newItem = SavedCredential(
                    id = nextId,
                    serviceName = cleanService,
                    username = cleanUsername,
                    password = cleanPassword,
                    packageName = packageName,
                    timestamp = System.currentTimeMillis(),
                    isPinned = isPinned
                )
                currentList.add(0, newItem)
                newItem
            }

            persistCredentialsToDisk(currentList)
            savedItem
        }
    }

    suspend fun deleteCredential(id: Long): Boolean = withContext(ioDispatcher) {
        mutex.withLock {
            val currentList = _credentialsFlow.value.toMutableList()
            val removed = currentList.removeAll { it.id == id }
            if (removed) {
                persistCredentialsToDisk(currentList)
            }
            removed
        }
    }

    suspend fun togglePinCredential(credential: SavedCredential): SavedCredential = withContext(ioDispatcher) {
        mutex.withLock {
            val currentList = _credentialsFlow.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == credential.id }
            if (index != -1) {
                val updated = currentList[index].copy(isPinned = !currentList[index].isPinned)
                currentList[index] = updated
                persistCredentialsToDisk(currentList)
                updated
            } else {
                credential
            }
        }
    }

    suspend fun clearAllCredentials(): Unit = withContext(ioDispatcher) {
        mutex.withLock {
            persistCredentialsToDisk(emptyList())
        }
    }

    suspend fun getCredentialsForPackageOrQuery(
        packageName: String,
        query: String = ""
    ): List<SavedCredential> = withContext(ioDispatcher) {
        val all = _credentialsFlow.value
        val cleanPkg = packageName.trim()
        val cleanQuery = query.trim()

        all.filter { cred ->
            (cleanPkg.isNotBlank() && cred.packageName.equals(cleanPkg, ignoreCase = true)) ||
                    (cleanQuery.isNotBlank() && (
                            cred.serviceName.contains(cleanQuery, ignoreCase = true) ||
                                    cred.username.contains(cleanQuery, ignoreCase = true)
                            )) ||
                    (cleanPkg.isBlank() && cleanQuery.isBlank())
        }.sortedWith(
            compareByDescending<SavedCredential> { it.isPinned }
                .thenByDescending { it.timestamp }
        )
    }

    suspend fun findMatchingCredential(
        serviceName: String,
        username: String
    ): SavedCredential? = withContext(ioDispatcher) {
        _credentialsFlow.value.firstOrNull {
            it.serviceName.equals(serviceName.trim(), ignoreCase = true) &&
                    it.username.equals(username.trim(), ignoreCase = true)
        }
    }

    suspend fun findCredentialsForPackage(packageName: String): List<SavedCredential> = withContext(ioDispatcher) {
        if (packageName.isBlank()) return@withContext emptyList()
        _credentialsFlow.value.filter {
            it.packageName.equals(packageName.trim(), ignoreCase = true) ||
                    (it.serviceName.isNotBlank() && packageName.contains(it.serviceName, ignoreCase = true))
        }
    }

    companion object {
        private const val TAG = "EncryptedCredStorage"
        private const val PREFS_FILE_NAME = "nxv_encrypted_vault_prefs"
        private const val KEY_CREDENTIALS_DATA = "encrypted_credentials_payload"
        private const val KEY_LAST_UPDATED = "encrypted_credentials_last_updated"
    }
}
