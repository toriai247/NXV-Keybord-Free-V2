package com.example.dictionary

import android.content.Context
import android.database.Cursor
import android.provider.UserDictionary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidUserDictionary(private val context: Context) {

    suspend fun queryWords(prefix: String, maxCount: Int = 3): List<String> = withContext(Dispatchers.IO) {
        if (prefix.isBlank()) return@withContext emptyList()
        try {
            val cursor: Cursor? = context.contentResolver.query(
                UserDictionary.Words.CONTENT_URI,
                arrayOf(UserDictionary.Words.WORD),
                "${UserDictionary.Words.WORD} LIKE ?",
                arrayOf("$prefix%"),
                "${UserDictionary.Words.FREQUENCY} DESC"
            )
            val list = mutableListOf<String>()
            cursor?.use {
                val colIndex = it.getColumnIndex(UserDictionary.Words.WORD)
                while (it.moveToNext() && list.size < maxCount) {
                    if (colIndex >= 0) {
                        val word = it.getString(colIndex)
                        if (!word.isNullOrBlank() && !list.contains(word)) {
                            list.add(word)
                        }
                    }
                }
            }
            list
        } catch (_: Exception) {
            // Graceful fallback if permission is not granted or provider not available
            emptyList()
        }
    }
}
