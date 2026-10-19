package com.gym.manager.domain

import com.gym.manager.data.db.MemberFull
import com.gym.manager.util.DateUtils
import com.gym.manager.util.PaymentTypes
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class MemberStatus { ACTIVE, ENDING_SOON, ENDS_TODAY, EXPIRED, FROZEN }

/**
 * Business rules ported from the original Member class.
 *
 * One deliberate fix: frozen days are counted only from the start of the current
 * subscription. The original re-counted old freezes after every renewal, which
 * gave members free extra days each time they renewed.
 */
object MemberRules {
    const val SOON_DAYS = 7

    fun frozenDays(m: MemberFull, today: LocalDate = DateUtils.today()): Int {
        val subStart = DateUtils.parse(m.member.subscribeDate) ?: return 0
        var total = 0
        for (f in m.freezes) {
            val s0 = DateUtils.parse(f.startDate) ?: continue
            val e = DateUtils.parse(f.endDate) ?: continue
            val s = maxOf(s0, subStart)
            val ee = minOf(e, today)
            if (!ee.isBefore(s)) total += ChronoUnit.DAYS.between(s, ee).toInt()
        }
        return total
    }

    fun isFrozen(m: MemberFull, today: LocalDate = DateUtils.today()): Boolean =
        m.freezes.any {
            val s = DateUtils.parse(it.startDate)
            val e = DateUtils.parse(it.endDate)
            s != null && e != null && !today.isBefore(s) && !today.isAfter(e)
        }

    fun endDate(m: MemberFull, today: LocalDate = DateUtils.today()): LocalDate {
        val start = DateUtils.parse(m.member.subscribeDate) ?: today
        return start.plusDays((m.member.packageDays + frozenDays(m, today)).toLong())
    }

    fun daysRemaining(m: MemberFull, today: LocalDate = DateUtils.today()): Int =
        ChronoUnit.DAYS.between(today, endDate(m, today)).toInt()

    fun status(m: MemberFull, today: LocalDate = DateUtils.today()): MemberStatus {
        if (isFrozen(m, today)) return MemberStatus.FROZEN
        val r = daysRemaining(m, today)
        return when {
            r < 0 -> MemberStatus.EXPIRED
            r == 0 -> MemberStatus.ENDS_TODAY
            r <= SOON_DAYS -> MemberStatus.ENDING_SOON
            else -> MemberStatus.ACTIVE
        }
    }

    /** Sum of income payments only (price_update is excluded). */
    fun totalPaid(m: MemberFull): Long =
        m.payments.filter { it.type in PaymentTypes.income }.sumOf { it.amount }

    /** A renewal starts from today, or from the old end date if the member is still active. */
    fun renewalStart(m: MemberFull, today: LocalDate = DateUtils.today()): LocalDate =
        maxOf(today, endDate(m, today))

    fun renewalEnd(m: MemberFull, days: Int, today: LocalDate = DateUtils.today()): LocalDate =
        renewalStart(m, today).plusDays(days.toLong())
}

/** Everything a screen needs to display one member. */
data class MemberUi(
    val full: MemberFull,
    val status: MemberStatus,
    val daysRemaining: Int,
    val endDate: LocalDate,
    val totalPaid: Long
) {
    val m get() = full.member

    companion object {
        fun from(f: MemberFull, today: LocalDate = DateUtils.today()) = MemberUi(
            full = f,
            status = MemberRules.status(f, today),
            daysRemaining = MemberRules.daysRemaining(f, today),
            endDate = MemberRules.endDate(f, today),
            totalPaid = MemberRules.totalPaid(f)
        )
    }
}
