package com.example

import android.content.Context
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.test.core.app.ApplicationProvider
import android.view.inputmethod.InputConnection
import com.example.data.entity.ClipboardItem
import com.example.data.AppDatabase
import com.example.data.KeyboardDataRepository
import com.example.ime.InputConnectionManager
import com.example.keyboard.ShiftState
import com.example.language.avro.AvroPhoneticEngine
import com.example.security.EncryptedCredentialStorageService
import com.example.util.ClipboardUtils
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NXV Keyboard", appName)
    }

    @Test
    fun `test avro phonetic conversions`() {
        val engine = AvroPhoneticEngine()

        // Independent vowels
        assertEquals("আমি", engine.convertWord("ami"))
        assertEquals("তুমি", engine.convertWord("tumi"))
        assertEquals("ভালো", engine.convertWord("bhalo"))

        // Juktakkhor and clusters
        val bangladesh = engine.convertWord("bangladesh")
        assertTrue(bangladesh.contains("বাংলাদেশ"))

        // Candidates check
        val candidates = engine.getCandidates("kemon")
        assertTrue(candidates.isNotEmpty())
    }

    @Test
    fun `test encrypted credential storage service CRUD operations`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = EncryptedCredentialStorageService(context)

        // Clear initially
        storage.clearAllCredentials()

        // 1. Save new credential
        val saved = storage.saveCredential(
            serviceName = "Google",
            username = "test@gmail.com",
            password = "SecretPassword123!",
            packageName = "com.google.android.gm"
        )
        assertNotNull(saved)
        assertEquals("Google", saved.serviceName)
        assertEquals("test@gmail.com", saved.username)
        assertEquals("SecretPassword123!", saved.password)

        // 2. Retrieve credentials flow
        val list = storage.credentialsFlow.first()
        assertEquals(1, list.size)
        assertEquals("SecretPassword123!", list[0].password)

        // 3. Search by package or query
        val results = storage.getCredentialsForPackageOrQuery("com.google.android.gm")
        assertEquals(1, results.size)
        assertEquals("Google", results[0].serviceName)

        val queryResults = storage.getCredentialsForPackageOrQuery("", "google")
        assertEquals(1, queryResults.size)

        // 4. Update password
        val updated = storage.saveCredential(
            serviceName = "Google",
            username = "test@gmail.com",
            password = "NewUpdatedPassword456!",
            packageName = "com.google.android.gm"
        )
        assertEquals("NewUpdatedPassword456!", updated.password)
        val updatedList = storage.credentialsFlow.first()
        assertEquals(1, updatedList.size)
        assertEquals("NewUpdatedPassword456!", updatedList[0].password)

        // 5. Delete credential
        val deleted = storage.deleteCredential(updated.id)
        assertTrue(deleted)
        val emptyList = storage.credentialsFlow.first()
        assertTrue(emptyList.isEmpty())
    }

    @Test
    fun `test password and account field detection`() {
        val manager = InputConnectionManager()

        // Test standard password field
        val passwordInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        manager.update(null, passwordInfo)
        assertTrue(manager.isPasswordField())
        assertFalse(manager.isEmailOrAccountField())

        // Test web password field
        val webPassInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }
        manager.update(null, webPassInfo)
        assertTrue(manager.isPasswordField())

        // Test email/account field
        val emailInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        manager.update(null, emailInfo)
        assertFalse(manager.isPasswordField())
        assertTrue(manager.isEmailOrAccountField())
    }

    @Test
    fun `test shift state properties and state transitions`() {
        // Test ShiftState properties
        val lower = ShiftState.LOWERCASE
        assertFalse(lower.isUppercase)
        assertFalse(lower.isCapsLock)
        assertFalse(lower.isShiftOnce)

        val shifted = ShiftState.SHIFT_ONCE
        assertTrue(shifted.isUppercase)
        assertFalse(shifted.isCapsLock)
        assertTrue(shifted.isShiftOnce)

        val caps = ShiftState.CAPS_LOCK
        assertTrue(caps.isUppercase)
        assertTrue(caps.isCapsLock)
        assertFalse(caps.isShiftOnce)
    }

    @Test
    fun `test grapheme-aware backspace calculation with Bangla and emoji`() {
        val manager = InputConnectionManager()

        // Single English character
        assertEquals(1, manager.calculateLastGraphemeLength("hello"))
        assertEquals(1, manager.calculateLastGraphemeLength("A"))

        // Bangla characters and vowel signs (kar)
        // "বাংলা" -> ends with Aa-kar (া)
        assertEquals(1, manager.calculateLastGraphemeLength("বাংলা"))
        // "ভালো" -> ends with O-kar (ো)
        assertEquals(1, manager.calculateLastGraphemeLength("ভালো"))
        // "আমি" -> ends with I-kar (ি)
        assertEquals(1, manager.calculateLastGraphemeLength("আমি"))
        // "বাংলাদেশ" -> ends with Talobbo-Sha (শ)
        assertEquals(1, manager.calculateLastGraphemeLength("বাংলাদেশ"))

        // Emoji surrogate pair (U+1F60A is 2 chars in UTF-16)
        val emojiStr = "Hi 😊"
        assertEquals(2, manager.calculateLastGraphemeLength(emojiStr))

        // Empty string
        assertEquals(0, manager.calculateLastGraphemeLength(""))
    }

    @Test
    fun `test clipboard relative time formatting`() {
        val now = 1700000000000L

        // 10 seconds ago -> "Just now"
        assertEquals("Just now", ClipboardUtils.formatRelativeTime(now - 10_000L, now))

        // 5 minutes ago -> "5m ago"
        assertEquals("5m ago", ClipboardUtils.formatRelativeTime(now - (5 * 60 * 1000L), now))

        // 3 hours ago -> "3h ago"
        assertEquals("3h ago", ClipboardUtils.formatRelativeTime(now - (3 * 3600 * 1000L), now))

        // 1 day ago -> "Yesterday"
        assertEquals("Yesterday", ClipboardUtils.formatRelativeTime(now - (25 * 3600 * 1000L), now))
    }

    @Test
    fun `test clipboard word and char count helper`() {
        val summary1 = ClipboardUtils.getWordAndCharCount("Hello world")
        assertEquals("11 chars • 2 words", summary1)

        val summary2 = ClipboardUtils.getWordAndCharCount("Dhaka")
        assertEquals("5 chars • 1 word", summary2)

        val summary3 = ClipboardUtils.getWordAndCharCount("")
        assertEquals("0 chars • 0 words", summary3)
    }

    @Test
    fun `test clipboard repository insert deduplication and pinning`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = KeyboardDataRepository(
            clipboardDao = db.clipboardDao(),
            shortcutDao = db.shortcutDao(),
            dictionaryDao = db.dictionaryDao(),
            credentialDao = db.credentialDao()
        )

        // 1. Add item
        repo.addClipboardItem("Copied text snippet 1")
        var items = repo.clipboardItems.first()
        assertEquals(1, items.size)
        assertEquals("Copied text snippet 1", items[0].text)
        assertFalse(items[0].isPinned)

        // 2. Add duplicate item: should not duplicate, only refresh timestamp
        val initialTimestamp = items[0].timestamp
        Thread.sleep(10)
        repo.addClipboardItem("Copied text snippet 1")
        items = repo.clipboardItems.first()
        assertEquals(1, items.size)
        assertTrue(items[0].timestamp >= initialTimestamp)

        // 3. Add second item and pin it
        repo.addClipboardItem("Address: 123 Main St")
        items = repo.clipboardItems.first()
        assertEquals(2, items.size)

        val addressItem = items.first { it.text.contains("123 Main St") }
        repo.togglePinClipboardItem(addressItem)

        items = repo.clipboardItems.first()
        val pinnedItem = items.first { it.text.contains("123 Main St") }
        assertTrue(pinnedItem.isPinned)
        // Pinned item should be first due to ORDER BY isPinned DESC
        assertEquals("Address: 123 Main St", items[0].text)

        // 4. Clear unpinned: pinned should remain intact
        repo.clearClipboard(keepPinned = true)
        items = repo.clipboardItems.first()
        assertEquals(1, items.size)
        assertEquals("Address: 123 Main St", items[0].text)
        assertTrue(items[0].isPinned)

        // 5. Delete pinned item
        repo.deleteClipboardItem(items[0].id)
        items = repo.clipboardItems.first()
        assertTrue(items.isEmpty())

        db.close()
    }

    @Test
    fun `test theme system and palettes retrieval`() {
        val puppy = com.example.theme.KeyboardThemes.getPalette("puppy_pop")
        assertEquals("puppy_pop", puppy.themeId)
        assertEquals("Puppy Pop White", puppy.themeName)
        assertEquals(com.example.theme.ThemeSpecialIconStyle.PUPPY_MINIMAL, puppy.specialIconStyle)
        assertEquals(com.example.theme.KeyPopupStyle.PUPPY_CHARACTER, puppy.popupStyle)

        val strawberry = com.example.theme.KeyboardThemes.getPalette("strawberry_dessert")
        assertEquals("strawberry_dessert", strawberry.themeId)
        assertEquals("Strawberry Dessert", strawberry.themeName)
        assertEquals(com.example.theme.ThemeSpecialIconStyle.STRAWBERRY_DESSERT, strawberry.specialIconStyle)
        assertEquals(com.example.theme.KeyPopupStyle.STRAWBERRY_SWEET, strawberry.popupStyle)

        val defaultTheme = com.example.theme.KeyboardThemes.getPalette("geometric")
        assertEquals("geometric", defaultTheme.themeId)

        // Verify all themes list contains the new themes
        val allThemeIds = com.example.theme.KeyboardThemes.ALL_THEMES.map { it.themeId }
        assertTrue(allThemeIds.contains("puppy_pop"))
        assertTrue(allThemeIds.contains("strawberry_dessert"))
        assertTrue(allThemeIds.contains("geometric"))
    }

    @Test
    fun `test vibration and sound preferences updates`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.data.preferences.KeyboardPreferences(context)

        // Test vibration update
        prefs.updateVibrationEnabled(false)
        var current = prefs.settingsFlow.first()
        assertFalse(current.keyVibrationEnabled)

        prefs.updateVibrationEnabled(true)
        current = prefs.settingsFlow.first()
        assertTrue(current.keyVibrationEnabled)

        // Test sound update
        prefs.updateSoundEnabled(true)
        current = prefs.settingsFlow.first()
        assertTrue(current.keySoundEnabled)

        prefs.updateSoundEnabled(false)
        current = prefs.settingsFlow.first()
        assertFalse(current.keySoundEnabled)
    }
}


