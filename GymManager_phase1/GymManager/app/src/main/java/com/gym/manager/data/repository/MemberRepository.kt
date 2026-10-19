package com.gym.manager.data.repository

import androidx.room.withTransaction
import com.gym.manager.data.db.GymDatabase
import com.gym.manager.data.db.MemberEntity
import com.gym.manager.data.db.MemberFull
import com.gym.manager.data.db.PaymentEntity
import com.gym.manager.domain.MemberRules
import com.gym.manager.util.DateUtils
import com.gym.manager.util.Limits
import com.gym.manager.util.PaymentMethods
import com.gym.manager.util.PaymentTypes
import com.gym.manager.util.Validators
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class MemberRepository(
    private val db: GymDatabase,
    private val audit: AuditLogger
) {
    private val memberDao = db.memberDao()
    private val paymentDao = db.paymentDao()

    data class NewMember(
        val name: String,
        val memberId: String,
        val phone: String,
        val subscribeDate: LocalDate,
        val days: Int,
        val price: Long,
        val method: String
    )

    fun observeAll(): Flow<List<MemberFull>> = memberDao.observeAll()
    fun observeOne(id: String): Flow<MemberFull?> = memberDao.observeOne(id)
    fun observeMonthIncome(): Flow<Long> =
        paymentDao.observeIncomeForMonth(DateUtils.monthPrefix(), PaymentTypes.income)

    suspend fun get(id: String): MemberEntity? = memberDao.getById(id)

    suspend fun addMember(input: NewMember, confirmDuplicatePhone: Boolean): Outcome = safe {
        val name = input.name.trim()
        if (name.isEmpty()) return@safe Outcome.Fail("الاسم مطلوب")
        val id = Validators.cleanId(input.memberId)
            ?: return@safe Outcome.Fail("رقم العضوية: حروف إنجليزية وأرقام و _ فقط")
        if (!Validators.isValidPhone(input.phone))
            return@safe Outcome.Fail("رقم الهاتف غير صحيح (من ${Limits.PHONE_MIN} إلى ${Limits.PHONE_MAX} رقمًا)")
        if (!Validators.isValidDays(input.days))
            return@safe Outcome.Fail("عدد الأيام بين ${Limits.MIN_DAYS} و ${Limits.MAX_DAYS}")
        if (!Validators.isValidPrice(input.price))
            return@safe Outcome.Fail("السعر بين ${Limits.MIN_PRICE} و ${Limits.MAX_PRICE}")
        if (input.subscribeDate.isAfter(DateUtils.today()))
            return@safe Outcome.Fail("تاريخ الاشتراك لا يمكن أن يكون في المستقبل")
        if (input.method !in PaymentMethods.all) return@safe Outcome.Fail("طريقة الدفع غير صحيحة")
        if (memberDao.getById(id) != null) return@safe Outcome.Fail("رقم العضوية $id مستخدم بالفعل")

        val phone = Validators.normalizePhone(input.phone)
        if (!confirmDuplicatePhone) {
            val dup = memberDao.findByPhone(phone, id)
            if (dup != null) return@safe Outcome.Confirm("هذا الرقم مسجل للعضو «${dup.name}». هل تريد المتابعة؟")
        }

        val subDate = DateUtils.format(input.subscribeDate)
        db.withTransaction {
            memberDao.insert(
                MemberEntity(
                    memberId = id, name = name, phone = phone, subscribeDate = subDate,
                    packageDays = input.days, price = input.price,
                    bookingDate = DateUtils.todayStr(), preferredMethod = input.method
                )
            )
            paymentDao.insert(
                PaymentEntity(
                    memberId = id, date = subDate, amount = input.price, days = input.days,
                    type = PaymentTypes.SUBSCRIPTION, method = input.method
                )
            )
            audit.log("add_member", id, "$name | ${input.price}")
        }
        Outcome.Ok
    }

    suspend fun renew(memberId: String, days: Int, price: Long, method: String): Outcome = safe {
        if (!Validators.isValidDays(days))
            return@safe Outcome.Fail("عدد الأيام بين ${Limits.MIN_DAYS} و ${Limits.MAX_DAYS}")
        if (!Validators.isValidPrice(price))
            return@safe Outcome.Fail("السعر بين ${Limits.MIN_PRICE} و ${Limits.MAX_PRICE}")
        if (method !in PaymentMethods.all) return@safe Outcome.Fail("طريقة الدفع غير صحيحة")
        val m = memberDao.getFull(memberId) ?: return@safe Outcome.Fail("العضو غير موجود")

        val today = DateUtils.today()
        val newStart = MemberRules.renewalStart(m, today)
        db.withTransaction {
            memberDao.update(
                m.member.copy(
                    subscribeDate = DateUtils.format(newStart), packageDays = days, price = price,
                    bookingDate = DateUtils.format(today)
                )
            )
            paymentDao.insert(
                PaymentEntity(
                    memberId = memberId, date = DateUtils.format(today), amount = price, days = days,
                    type = PaymentTypes.RENEWAL, method = method
                )
            )
            audit.log("renew", memberId, "$days يوم | $price")
        }
        Outcome.Ok
    }

    suspend fun updateContact(memberId: String, name: String, phone: String): Outcome = safe {
        val m = memberDao.getById(memberId) ?: return@safe Outcome.Fail("العضو غير موجود")
        val newName = name.trim()
        if (newName.isEmpty()) return@safe Outcome.Fail("الاسم مطلوب")
        if (!Validators.isValidPhone(phone))
            return@safe Outcome.Fail("رقم الهاتف غير صحيح (من ${Limits.PHONE_MIN} إلى ${Limits.PHONE_MAX} رقمًا)")
        val newPhone = Validators.normalizePhone(phone)
        val dup = memberDao.findByPhone(newPhone, memberId)
        if (dup != null) return@safe Outcome.Fail("هذا الرقم مسجل للعضو «${dup.name}»")
        db.withTransaction {
            memberDao.update(m.copy(name = newName, phone = newPhone))
            audit.log("edit", memberId)
        }
        Outcome.Ok
    }

    suspend fun delete(memberId: String): Outcome = safe {
        val m = memberDao.getById(memberId) ?: return@safe Outcome.Fail("العضو غير موجود")
        db.withTransaction {
            memberDao.deleteById(memberId) // payments, freezes, etc. are removed by cascade
            audit.log("delete", memberId, m.name)
        }
        Outcome.Ok
    }

    private suspend fun safe(block: suspend () -> Outcome): Outcome = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Outcome.Fail("تعذّر تنفيذ العملية: ${e.localizedMessage ?: "خطأ غير معروف"}")
    }
}
