@file:Suppress("DEPRECATION")

package com.example.data.local.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class DatabasePassphraseStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Synchronized
    fun getOrCreatePassphraseString(): String {
        prefs.getString(KEY_PASSPHRASE, null)?.let { return it }
        val random = SecureRandom()
        val passphrase = buildString(PASSPHRASE_LENGTH) {
            repeat(PASSPHRASE_LENGTH) {
                append(PASSPHRASE_ALPHABET[random.nextInt(PASSPHRASE_ALPHABET.length)])
            }
        }
        prefs.edit { putString(KEY_PASSPHRASE, passphrase) }
        return passphrase
    }

    companion object {
        private const val PREFS_NAME = "fieldflow_db_passphrase"
        private const val KEY_PASSPHRASE = "sqlcipher_passphrase"
        private const val PASSPHRASE_LENGTH = 48
        private const val PASSPHRASE_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }
}
