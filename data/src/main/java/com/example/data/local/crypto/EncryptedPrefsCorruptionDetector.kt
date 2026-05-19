package com.example.data.local.crypto

import java.io.IOException
import javax.crypto.AEADBadTagException
import kotlin.sequences.generateSequence
import java.security.KeyStoreException as JcaKeyStoreException

internal object EncryptedPrefsCorruptionDetector {

    private const val ANDROID_KEY_STORE_EXCEPTION = "android.security.KeyStoreException"

    fun isRecoverable(error: Throwable): Boolean =
        error.causeChain().any(::signalsCorruption)

    private fun Throwable.causeChain(): Sequence<Throwable> =
        generateSequence(this) { it.cause }

    private fun signalsCorruption(cause: Throwable): Boolean = when {
        cause is AEADBadTagException -> true
        cause is JcaKeyStoreException -> true
        cause.javaClass.name == ANDROID_KEY_STORE_EXCEPTION -> true
        cause is IOException && cause.message.indicatesVerificationFailure() -> true
        cause.message.indicatesVerificationFailure() -> true
        else -> false
    }

    private fun String?.indicatesVerificationFailure(): Boolean {
        if (this == null) return false
        return contains("tag", ignoreCase = true) ||
            contains("verification", ignoreCase = true) ||
            contains("Signature/MAC verification", ignoreCase = true) ||
            contains("MAC verification failed", ignoreCase = true)
    }
}
