package com.gym.manager.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/** All dates are stored as "yyyy-MM-dd" text (same as the original file). */
object DateUtils {
    private val iso: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val month: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH)
    private val stamp: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    fun today(): LocalDate = LocalDate.now()
    fun todayStr(): String = format(today())
    fun format(d: LocalDate): String = d.format(iso)
    fun monthPrefix(d: LocalDate = today()): String = d.format(month)
    fun nowStamp(): String = java.time.LocalDateTime.now().format(stamp)

    fun parse(s: String?): LocalDate? = try {
        if (s == null) null else LocalDate.parse(s, iso)
    } catch (e: DateTimeParseException) {
        null
    }
}
