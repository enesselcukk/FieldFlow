package com.example.fieldflow.activation

import android.security.keystore.KeyProperties

internal const val ACTIVATION_KEYSTORE_PROVIDER = "AndroidKeyStore"

internal const val ACTIVATION_AES_GCM_TRANSFORM =
    "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"

internal const val ACTIVATION_KEYSTORE_ALIAS = "fieldflow_activation_code_ks"
internal const val ACTIVATION_GCM_IV_BYTES = 12
internal const val ACTIVATION_GCM_TAG_BITS = 128
internal const val ACTIVATION_GCM_AUTH_TAG_BYTES = ACTIVATION_GCM_TAG_BITS / Byte.SIZE_BITS
internal const val ACTIVATION_EMBEDDED_AES_KEY_BYTES = 16
