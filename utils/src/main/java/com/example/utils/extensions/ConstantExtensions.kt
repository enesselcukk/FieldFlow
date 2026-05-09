package com.example.utils.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedDate(): String {
    val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    return fmt.format(Date(this))
}
