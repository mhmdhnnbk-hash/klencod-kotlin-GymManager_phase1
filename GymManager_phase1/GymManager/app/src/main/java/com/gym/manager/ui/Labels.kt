package com.gym.manager.ui

import androidx.compose.ui.graphics.Color
import com.gym.manager.domain.MemberStatus
import com.gym.manager.ui.theme.StatusActive
import com.gym.manager.ui.theme.StatusExpired
import com.gym.manager.ui.theme.StatusFrozen
import com.gym.manager.ui.theme.StatusSoon

fun statusLabel(s: MemberStatus, days: Int): String = when (s) {
    MemberStatus.FROZEN -> "مجمّد"
    MemberStatus.EXPIRED -> "منتهي"
    MemberStatus.ENDS_TODAY -> "ينتهي اليوم"
    MemberStatus.ENDING_SOON -> "ينتهي خلال $days يوم"
    MemberStatus.ACTIVE -> "نشط"
}

fun statusColor(s: MemberStatus): Color = when (s) {
    MemberStatus.FROZEN -> StatusFrozen
    MemberStatus.EXPIRED -> StatusExpired
    MemberStatus.ENDS_TODAY, MemberStatus.ENDING_SOON -> StatusSoon
    MemberStatus.ACTIVE -> StatusActive
}

fun methodLabel(m: String): String = when (m) {
    "cash" -> "نقدًا"
    "card" -> "بطاقة"
    "transfer" -> "تحويل"
    "wallet" -> "محفظة"
    else -> "أخرى"
}

fun paymentTypeLabel(t: String): String = when (t) {
    "subscription" -> "اشتراك"
    "renewal" -> "تجديد"
    "extra_days" -> "أيام إضافية"
    "additional_payment" -> "دفعة إضافية"
    "price_update" -> "تعديل سعر"
    else -> t
}
