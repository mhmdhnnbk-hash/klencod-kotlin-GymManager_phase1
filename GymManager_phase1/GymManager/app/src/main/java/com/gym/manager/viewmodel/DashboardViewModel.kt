package com.gym.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.manager.data.repository.MemberRepository
import com.gym.manager.domain.MemberStatus
import com.gym.manager.domain.MemberUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

data class DashboardState(
    val total: Int = 0,
    val active: Int = 0,
    val endingSoon: Int = 0,
    val expired: Int = 0,
    val frozen: Int = 0,
    val monthIncome: Long = 0,
    val soonList: List<MemberUi> = emptyList()
)

class DashboardViewModel(repo: MemberRepository) : ViewModel() {
    val state: StateFlow<DashboardState> = combine(repo.observeAll(), repo.observeMonthIncome()) { list, income ->
        val ui = list.map { MemberUi.from(it) }
        val soon = ui.filter { it.status == MemberStatus.ENDING_SOON || it.status == MemberStatus.ENDS_TODAY }
            .sortedBy { it.daysRemaining }
        DashboardState(
            total = ui.size,
            // same definitions as the original show_stats
            active = ui.count { it.status != MemberStatus.FROZEN && it.daysRemaining >= 0 },
            endingSoon = soon.size,
            expired = ui.count { it.daysRemaining < 0 },
            frozen = ui.count { it.status == MemberStatus.FROZEN },
            monthIncome = income,
            soonList = soon
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())
}
