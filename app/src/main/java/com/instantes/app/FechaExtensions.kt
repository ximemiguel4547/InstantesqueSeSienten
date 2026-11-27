package com.instantes.app

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.aFechaLegible(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}
