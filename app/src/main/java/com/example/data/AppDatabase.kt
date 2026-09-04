package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ClipboardDao
import com.example.data.dao.CredentialDao
import com.example.data.dao.DictionaryDao
import com.example.data.dao.ShortcutDao
import com.example.data.entity.ClipboardItem
import com.example.data.entity.DictionaryWord
import com.example.data.entity.SavedCredential
import com.example.data.entity.TextShortcut

@Database(
    entities = [ClipboardItem::class, TextShortcut::class, DictionaryWord::class, SavedCredential::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
    abstract fun credentialDao(): CredentialDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun dictionaryDao(): DictionaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nxv_keyboard_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
