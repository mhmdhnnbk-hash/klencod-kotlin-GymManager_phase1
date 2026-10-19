package com.gym.manager.util

object Validators {
    /** Converts Arabic-Indic digits (٠-٩ / ۰-۹) to ASCII so users can type either. */
    fun normalizeDigits(s: String): String {
        val sb = StringBuilder(s.length)
        for (c in s) {
            sb.append(
                when (c) {
                    in '\u0660'..'\u0669' -> '0' + (c - '\u0660')
                    in '\u06F0'..'\u06F9' -> '0' + (c - '\u06F0')
                    else -> c
                }
            )
        }
        return sb.toString()
    }

    private val idRegex = Regex("^[A-Z0-9_]+$")
    private val spaceDash = Regex("[\\s\\-]")

    /** Member ID: trimmed, upper-case, letters/digits/underscore only. Null if invalid. */
    fun cleanId(s: String): String? {
        val t = normalizeDigits(s).trim().uppercase()
        return if (t.isNotEmpty() && idRegex.matches(t)) t else null
    }

    fun normalizePhone(s: String): String = normalizeDigits(s).replace(spaceDash, "")

    fun isValidPhone(s: String): Boolean {
        val c = normalizePhone(s)
        return c.length in Limits.PHONE_MIN..Limits.PHONE_MAX && c.all { it in '0'..'9' }
    }

    fun parseInt(s: String): Int? = normalizeDigits(s).trim().toIntOrNull()
    fun parseLong(s: String): Long? = normalizeDigits(s).trim().toLongOrNull()

    fun isValidDays(d: Int) = d in Limits.MIN_DAYS..Limits.MAX_DAYS
    fun isValidPrice(p: Long) = p in Limits.MIN_PRICE..Limits.MAX_PRICE
}
