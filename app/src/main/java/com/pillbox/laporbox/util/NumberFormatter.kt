package com.pillbox.laporbox.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatAmount(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale("id", "ID"))
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    val formatter = DecimalFormat("#,###", symbols)
    return formatter.format(amount)
}