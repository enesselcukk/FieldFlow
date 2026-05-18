@file:Suppress("DEPRECATION")

package com.example.data.local.crypto

import android.content.Context
import android.content.SharedPreferences
import android.security.KeyStoreException
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.crypto.AEADBadTagException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.sequences.generateSequence
import java.security.KeyStoreException as JcaKeyStoreException

@Singleton
internal class DatabasePassphraseStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        openEncryptedPrefsWithRecovery()
    }

    private fun openEncryptedPrefsWithRecovery(): SharedPreferences {
        try {
            return buildEncryptedPrefs()
        } catch (first: Throwable) {
            if (!signalsCorruptedKeystoreOrPrefs(first)) throw first
            Log.w(TAG, "Encrypted passphrase store unreadable; wiping prefs and local DB", first)
            wipeBrokenPassphraseArtifacts()
            try {
                return buildEncryptedPrefs()
            } catch (retry: Throwable) {
                Log.e(TAG, "Encrypted passphrase store failed after recovery wipe", retry)
                throw retry
            }
        }
    }

    private fun buildEncryptedPrefs(): SharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    private fun wipeBrokenPassphraseArtifacts() {
        try {
            context.deleteSharedPreferences(PREFS_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "deleteSharedPreferences failed", e)
        }
        SqlCipherDatabaseMigrator.wipeRoomDatabase(context)
    }

    @Synchronized
    fun getOrCreatePassphraseString(): String {
        prefs.getString(KEY_PASSPHRASE, null)?.let { return it }
        val random = java.security.SecureRandom()
        val passphrase = buildString(PASSPHRASE_LENGTH) {
            repeat(PASSPHRASE_LENGTH) {
                append(PASSPHRASE_ALPHABET[random.nextInt(PASSPHRASE_ALPHABET.length)])
            }
        }
        prefs.edit { putString(KEY_PASSPHRASE, passphrase) }
        return passphrase
    }

    private companion object {
        private const val TAG = "DatabasePassphraseStore"
        private const val PREFS_NAME = "fieldflow_db_passphrase"
        private const val KEY_PASSPHRASE = "sqlcipher_passphrase"
        private const val PASSPHRASE_LENGTH = 48
        private const val PASSPHRASE_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        private fun Throwable.walkCauses(): Sequence<Throwable> =
            generateSequence(this) { it.cause }

        /**
         * True when decrypting persistence from Keystore-derived keys failed (prefs backup/restore mismatch,
         * OS upgrade edge cases, or corrupted files). Recoverable only by wiping and reissued secrets.
         */
        private fun signalsCorruptedKeystoreOrPrefs(root: Throwable): Boolean =
            root.walkCauses().any { throwable ->
                when {
                    throwable is AEADBadTagException -> true
                    throwable is JcaKeyStoreException -> true
                    throwable.javaClass.name == "android.security.KeyStoreException" -> true
                    throwable is IOException &&
                        (
                            throwable.message?.contains("tag", ignoreCase = true) == true ||
                                throwable.message?.contains("verification", ignoreCase = true) == true
                            ) -> true
                    throwable.message?.contains("Signature/MAC verification", ignoreCase = true) == true -> true
                    throwable.message?.contains("MAC verification failed", ignoreCase = true) == true -> true
                    else -> false
                }
            }
    }
}
