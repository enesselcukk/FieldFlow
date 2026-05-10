package com.example.fieldflow.activation

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.activationDataStore by preferencesDataStore(name = "activation_prefs")

internal class AppActivationStore(
    private val context: Context,
) {
    private val mutex = Mutex()

    private val activatedKey = booleanPreferencesKey("is_activated")
    private val encryptedCodeKey = stringPreferencesKey("activation_code_encrypted")

    val isActivated: Flow<Boolean> = context.activationDataStore.data
        .map { preferences: Preferences -> preferences[activatedKey] ?: false }

    suspend fun getExpectedActivationCode(): String = mutex.withLock {
        val snapshot = context.activationDataStore.data.first()
        val stored = snapshot[encryptedCodeKey]
        if (stored != null) {
            try {
                return decryptWithKeystore(stored)
            } catch (_: GeneralSecurityException) {
                context.activationDataStore.edit { it.remove(encryptedCodeKey) }
            }
        }
        val plaintext = decryptEmbeddedPayload()
        val sealed = encryptWithKeystore(plaintext)
        context.activationDataStore.edit { prefs ->
            prefs[encryptedCodeKey] = sealed
        }
        plaintext
    }

    suspend fun setActivated(value: Boolean) {
        context.activationDataStore.edit { prefs ->
            prefs[activatedKey] = value
        }
    }

    private fun decryptEmbeddedPayload(): String {
        val decoded = Base64.decode(EmbeddedActivationPayload.CIPHER_TEXT_B64, Base64.NO_WRAP)
        val minLen = ACTIVATION_GCM_IV_BYTES + ACTIVATION_GCM_AUTH_TAG_BYTES
        check(decoded.size >= minLen) { "embedded activation blob size invalid" }
        val iv = decoded.copyOfRange(0, ACTIVATION_GCM_IV_BYTES)
        val ciphertextPlusTag =
            decoded.copyOfRange(ACTIVATION_GCM_IV_BYTES, decoded.size)
        val cipher = Cipher.getInstance(ACTIVATION_AES_GCM_TRANSFORM)
        cipher.init(
            Cipher.DECRYPT_MODE,
            deriveEmbeddedSecretKey(),
            GCMParameterSpec(ACTIVATION_GCM_TAG_BITS, iv),
        )
        return String(cipher.doFinal(ciphertextPlusTag), Charsets.UTF_8)
    }

    private fun deriveEmbeddedSecretKey(): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(EmbeddedActivationPayload.KEY_MATERIAL_LABEL.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(
            digest.digest().copyOfRange(0, ACTIVATION_EMBEDDED_AES_KEY_BYTES),
            KeyProperties.KEY_ALGORITHM_AES,
        )
    }

    private fun getOrCreateKeystoreSecretKey(): SecretKey {
        val ks = KeyStore.getInstance(ACTIVATION_KEYSTORE_PROVIDER).apply { load(null) }
        (ks.getKey(ACTIVATION_KEYSTORE_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ACTIVATION_KEYSTORE_PROVIDER).run {
            init(
                KeyGenParameterSpec.Builder(
                    ACTIVATION_KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
            generateKey()
        }
    }

    private fun encryptWithKeystore(plaintext: String): String {
        val cipher = Cipher.getInstance(ACTIVATION_AES_GCM_TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKeystoreSecretKey())
        val iv = cipher.iv
        val sealed = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + sealed, Base64.NO_WRAP)
    }

    private fun decryptWithKeystore(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        check(combined.size > ACTIVATION_GCM_IV_BYTES) { "keystore ciphertext size invalid" }
        val iv = combined.copyOfRange(0, ACTIVATION_GCM_IV_BYTES)
        val ciphertextPlusTag = combined.copyOfRange(ACTIVATION_GCM_IV_BYTES, combined.size)
        val cipher = Cipher.getInstance(ACTIVATION_AES_GCM_TRANSFORM)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKeystoreSecretKey(),
            GCMParameterSpec(ACTIVATION_GCM_TAG_BITS, iv),
        )
        return String(cipher.doFinal(ciphertextPlusTag), Charsets.UTF_8)
    }
}
