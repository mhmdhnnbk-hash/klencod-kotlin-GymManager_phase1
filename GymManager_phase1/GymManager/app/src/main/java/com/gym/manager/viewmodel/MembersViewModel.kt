package com.gym.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.manager.data.repository.MemberRepository
import com.gym.manager.domain.MemberStatus
import com.gym.manager.domain.MemberUi
import com.gym.manager.util.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

enum class StatusFilter(val label: String) {
    ALL("الكل"), ACTIVE("نشط"), ENDING_SOON("ينتهي قريبًا"), EXPIRED("منتهٍ"), FROZEN("مجمّد")
}

class MembersViewModel(repo: MemberRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    private val _filter = MutableStateFlow(StatusFilter.ALL)
    val filter = _filter.asStateFlow()

    fun setQuery(q: String) { _query.value = q }
    fun setFilter(f: StatusFilter) { _filter.value = f }

    val members: StateFlow<List<MemberUi>> = combine(repo.observeAll(), _query, _filter) { list, q, f ->
        list.map { MemberUi.from(it) }.filter { matchesFilter(it, f) && matchesQuery(it, q) }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun matchesFilter(m: MemberUi, f: StatusFilter) = when (f) {
        StatusFilter.ALL -> true
        StatusFilter.ACTIVE -> m.status != MemberStatus.FROZEN && m.daysRemaining >= 0
        StatusFilter.ENDING_SOON -> m.status == MemberStatus.ENDING_SOON || m.status == MemberStatus.ENDS_TODAY
        StatusFilter.EXPIRED -> m.daysRemaining < 0
        StatusFilter.FROZEN -> m.status == MemberStatus.FROZEN
    }

    /** Same search rule as the original: name, ID, or phone (3+ digits). */
    private fun matchesQuery(m: MemberUi, raw: String): Boolean {
        val t = Validators.normalizeDigits(raw).trim()
        if (t.isEmpty()) return true
        val up = t.uppercase()
        val pq = Validators.normalizePhone(t)
        val isPhone = pq.length >= 3 && pq.all { it in '0'..'9' }
        return m.m.name.uppercase().contains(up) ||
            m.m.memberId.contains(up) ||
            (isPhone && m.m.phone.contains(pq))
    }
}
