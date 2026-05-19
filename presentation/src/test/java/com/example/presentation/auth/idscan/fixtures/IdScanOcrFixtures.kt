package com.example.presentation.auth.idscan.fixtures

internal object IdScanOcrFixtures {

    const val NOT_FOUND_MESSAGE = "Kimlik bilgisi bulunamadı"

    val latinNameSurname = """
        NAME Enes
        SURNAME Selçuk
    """.trimIndent()

    val latinInlineColons = """
        SURNAME: Selçuk
        ADI: Enes
    """.trimIndent()

    val turkishMultiline = """
        ADI
        Ahmet
        SOYADI
        Yılmaz
    """.trimIndent()

    val nameWithMrzNoise = """
        NAME Ali
        SURNAME Veli
        P<TURVELI<<ALI<<<<<<<<<<<<<<<<<<<<<<<<<
    """.trimIndent()

    val nameWithDateNoise = """
        NAME Ayşe
        SURNAME Demir
        01.01.1990
    """.trimIndent()

    val nameWithSerialLine = """
        NAME Can
        SURNAME Öz
        SERİ NO 123456
    """.trimIndent()

    const val UNRELATED_TEXT = "hello world"
    const val EMPTY_LABELS = "no labels here"
}
