package com.gym.manager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MemberEntity::class, PaymentEntity::class, FreezeEntity::class, AttendanceEntity::class,
        DebtEntity::class, NoteEntity::class, MedicalEntity::class, GoalEntity::class,
        VisitorEntity::class, ExpenseEntity::class, PackageEntity::class, ClassEntity::class,
        BookingEntity::class, PromoEntity::class, PromoUseEntity::class, AuditEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class GymDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun paymentDao(): PaymentDao
    abstract fun auditDao(): AuditDao
}
