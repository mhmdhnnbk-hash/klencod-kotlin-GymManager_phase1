package com.gym.manager

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.gym.manager.data.db.GymDatabase
import com.gym.manager.data.repository.AuditLogger
import com.gym.manager.data.repository.MemberRepository

class GymApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Manual dependency container: one database, one repository per area. */
class AppContainer(context: Context) {
    val db: GymDatabase = Room.databaseBuilder(context, GymDatabase::class.java, "gym.db").build()
    val audit = AuditLogger(db.auditDao())
    val members = MemberRepository(db, audit)
}
