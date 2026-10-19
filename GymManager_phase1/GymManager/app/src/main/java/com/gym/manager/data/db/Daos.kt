package com.gym.manager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Transaction
    @Query("SELECT * FROM members ORDER BY name")
    fun observeAll(): Flow<List<MemberFull>>

    @Transaction
    @Query("SELECT * FROM members WHERE memberId = :id")
    fun observeOne(id: String): Flow<MemberFull?>

    @Transaction
    @Query("SELECT * FROM members WHERE memberId = :id")
    suspend fun getFull(id: String): MemberFull?

    @Query("SELECT * FROM members WHERE memberId = :id")
    suspend fun getById(id: String): MemberEntity?

    @Query("SELECT * FROM members WHERE phone = :phone AND memberId != :excludeId LIMIT 1")
    suspend fun findByPhone(phone: String, excludeId: String): MemberEntity?

    @Insert
    suspend fun insert(member: MemberEntity)

    @Update
    suspend fun update(member: MemberEntity)

    @Query("DELETE FROM members WHERE memberId = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: PaymentEntity): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE date LIKE :prefix || '%' AND type IN (:types)")
    fun observeIncomeForMonth(prefix: String, types: List<String>): Flow<Long>
}

@Dao
interface AuditDao {
    @Insert
    suspend fun insert(entry: AuditEntity)

    @Query("SELECT COUNT(*) FROM audit_log")
    suspend fun count(): Int

    /** Keeps only the newest [keep] rows. */
    @Query("DELETE FROM audit_log WHERE id IN (SELECT id FROM audit_log ORDER BY id DESC LIMIT -1 OFFSET :keep)")
    suspend fun trim(keep: Int)
}
