package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_items")
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(
    tableName = "text_shortcuts",
    indices = [Index(value = ["shortcut"], unique = true)]
)
data class TextShortcut(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shortcut: String,
    val expansion: String
)

@Entity(
    tableName = "dictionary_words",
    indices = [Index(value = ["word", "locale"], unique = true)]
)
data class DictionaryWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val frequency: Int = 1,
    val locale: String = "en"
)

@Entity(
    tableName = "saved_credentials",
    indices = [Index(value = ["serviceName", "username"])]
)
data class SavedCredential(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serviceName: String,
    val username: String,
    val password: String,
    val packageName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
