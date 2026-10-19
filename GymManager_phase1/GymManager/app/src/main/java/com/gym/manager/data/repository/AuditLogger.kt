package com.gym.manager.data.repository

import com.gym.manager.data.db.AuditDao
import com.gym.manager.data.db.AuditEntity
import com.gym.manager.util.DateUtils

/** Writes the operations log. Keeps at most 10,000 rows (trims to 5,000), like the original. */
class AuditLogger(private val dao: AuditDao) {
    suspend fun log(action: String, target: String = "", details: String = "") {
        dao.insert(AuditEntity(time = DateUtils.nowStamp(), action = action, target = target, details = details))
        if (dao.count() > 10_000) dao.trim(5_000)
    }
}
