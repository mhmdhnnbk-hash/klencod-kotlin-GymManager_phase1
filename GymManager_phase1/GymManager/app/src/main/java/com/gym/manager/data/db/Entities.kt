package com.gym.manager.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/*
 * Full database schema. Tables for later phases are declared now so the
 * database never needs a migration while the app is being built.
 * Dates are "yyyy-MM-dd" text, money is whole pounds (Long).
 */

@Entity(
    tableName = "members",
    indices = [Index("phone"), Index(value = ["lockerNumber"], unique = true)]
)
data class MemberEntity(
    @PrimaryKey val memberId: String,
    val name: String,
    val phone: String,
    val subscribeDate: String,
    val packageDays: Int,
    val price: Long,
    val bookingDate: String,
    val photoPath: String? = null,
    val lockerNumber: String? = null,
    val familyId: String? = null,
    val preferredMethod: String = "cash"
)

private const val CASCADE = ForeignKey.CASCADE

@Entity(
    tableName = "payments",
    foreignKeys = [ForeignKey(MemberEntity::class, ["memberId"], ["memberId"], onDelete = CASCADE)],
    indices = [Index("memberId"), Index("date")]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: String,
    val date: String,
    val amount: Long,
    val days: Int,
    val type: String,
    val method: String
)

@Entity(
    tableName = "freezes",
    foreignKeys = [ForeignKey(MemberEntity::class, ["memberId"], ["memberId"], onDelete = CASCADE)],
    indices = [Index("memberId")]
)
data class FreezeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: String,
    val startDate: String,
    val endDate: String,
    val reason: String = ""
)

@Entity(
    tableName = "attendance",
    foreignKeys = [ForeignKey(MemberEntity::class, ["memberId"], ["memberId"], onDelete = CASCADE)],
    indices = [Index("memberId"), Index("date")]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: String,
    val date: String,
    val timeIn: String,
    val timeOut: String? = null
)

@Entity(
    tableName = "debts",
    foreignKeys = [ForeignKey(MemberEntity::class, ["memberId"], ["memberId"], onDelete = CASCADE)],
    indices = [Index("memberId")]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: String,
    val amount: Long,
    val paid: Long = 0,
    val dueDate: String,
    val note: String = "",
    val created: String
)

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(MemberEntity::class, ["memberId"], ["memberId"], onDelete = CASCADE)],
    indices = [Index("memberId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: String,
    val date: String,
    val text: String
)

@Entity(
    tableName = "medical_conditions",
    foreignKeys = [ForeignKey(MemberEntity::class, ["memberId"], ["memberId"], onDelete = CASCADE)],
    indices = [Index("memberId")]
)
data class MedicalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: String,
    val condition: String
)

@Entity(
    tableName = "goals",
    foreignKeys = [ForeignKey(MemberEntity::class, ["memberId"], ["memberId"], onDelete = CASCADE)],
    indices = [Index("memberId")]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: String,
    val type: String,
    val target: String,
    val current: String = "",
    val started: String,
    val notes: String = ""
)

@Entity(tableName = "visitors")
data class VisitorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val amount: Long,
    val date: String,
    val method: String,
    val note: String = "",
    val ptype: String = "day_pass",
    val created: String,
    val convertedToMember: String? = null
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val category: String,
    val date: String,
    val note: String = "",
    val paidBy: String = "",
    val created: String
)

@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey val name: String,
    val days: Int,
    val price: Long,
    val description: String = ""
)

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dayOfWeek: Int,
    val time: String,
    val capacity: Int,
    val notes: String = ""
)

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(ClassEntity::class, ["id"], ["classId"], onDelete = CASCADE),
        ForeignKey(MemberEntity::class, ["memberId"], ["memberId"], onDelete = CASCADE)
    ],
    indices = [Index(value = ["classId", "memberId", "date"], unique = true), Index("memberId")]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val memberId: String,
    val date: String
)

@Entity(tableName = "promos")
data class PromoEntity(
    @PrimaryKey val code: String,
    val discountPercent: Int,
    val validUntil: String? = null,
    val maxUses: Int? = null
)

@Entity(
    tableName = "promo_uses",
    foreignKeys = [ForeignKey(PromoEntity::class, ["code"], ["code"], onDelete = CASCADE)],
    indices = [Index("code")]
)
data class PromoUseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val memberId: String,
    val date: String
)

@Entity(tableName = "audit_log")
data class AuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: String,
    val action: String,
    val target: String = "",
    val details: String = ""
)

/** A member together with everything needed to compute status / end date. */
data class MemberFull(
    @Embedded val member: MemberEntity,
    @Relation(parentColumn = "memberId", entityColumn = "memberId")
    val payments: List<PaymentEntity>,
    @Relation(parentColumn = "memberId", entityColumn = "memberId")
    val freezes: List<FreezeEntity>
)
