@file:Suppress("DEPRECATION")

package com.example.data.local.crypto

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DatabasePassphraseStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val secureRandom = SecureRandom()

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        openEncryptedPrefsWithRecovery()
    }

    @Synchronized
    fun getOrCreatePassphraseString(): String {
        prefs.getString(KEY_PASSPHRASE, null)?.let { return it }
        val passphrase = generatePassphrase()
        prefs.edit { putString(KEY_PASSPHRASE, passphrase) }
        return passphrase
    }

    private fun openEncryptedPrefsWithRecovery(): SharedPreferences {
        try {
            return createEncryptedPrefs()
        } catch (error: Throwable) {
            if (!EncryptedPrefsCorruptionDetector.isRecoverable(error)) throw error
            Log.w(TAG, "Encrypted passphrase store unreadable; wiping prefs and local DB", error)
            wipeBrokenPassphraseArtifacts()
        }

        return try {
            createEncryptedPrefs()
        } catch (retryError: Throwable) {
            Log.e(TAG, "Encrypted passphrase store failed after recovery wipe", retryError)
            throw retryError
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    private fun wipeBrokenPassphraseArtifacts() {
        try {
            context.deleteSharedPreferences(PREFS_NAME)
        } catch (error: Exception) {
            Log.e(TAG, "deleteSharedPreferences failed", error)
        }
        SqlCipherDatabaseMigrator.wipeRoomDatabase(context)
    }

    private fun generatePassphrase(): String {
        val bytes = ByteArray(PASSPHRASE_BYTE_LENGTH)
        secureRandom.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private companion object {
        private const val TAG = "DatabasePassphraseStore"
        private const val PREFS_NAME = "fieldflow_db_passphrase"
        private const val KEY_PASSPHRASE = "sqlcipher_passphrase"
        private const val PASSPHRASE_BYTE_LENGTH = 36
    }
}
