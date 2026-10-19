package com.gym.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.manager.data.repository.MemberRepository
import com.gym.manager.data.repository.Outcome
import com.gym.manager.domain.MemberUi
import com.gym.manager.util.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailState(val loaded: Boolean = false, val member: MemberUi? = null)

class MemberDetailViewModel(
    private val repo: MemberRepository,
    private val memberId: String
) : ViewModel() {

    val state: StateFlow<DetailState> = repo.observeOne(memberId)
        .map { DetailState(loaded = true, member = it?.let { f -> MemberUi.from(f) }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailState())

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun renew(daysText: String, priceText: String, method: String) {
        val days = Validators.parseInt(daysText)
        val price = Validators.parseLong(priceText)
        if (days == null || price == null) {
            _events.trySend(UiEvent.Message("أدخل عدد أيام وسعرًا صحيحين"))
            return
        }
        viewModelScope.launch {
            when (val r = repo.renew(memberId, days, price, method)) {
                Outcome.Ok -> {
                    _events.send(UiEvent.Message("تم تجديد الاشتراك"))
                    _events.send(UiEvent.Renewed)
                }
                is Outcome.Fail -> _events.send(UiEvent.Message(r.message))
                is Outcome.Confirm -> _events.send(UiEvent.Message(r.message))
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (val r = repo.delete(memberId)) {
                Outcome.Ok -> _events.send(UiEvent.Deleted)
                is Outcome.Fail -> _events.send(UiEvent.Message(r.message))
                is Outcome.Confirm -> _events.send(UiEvent.Message(r.message))
            }
        }
    }
}
