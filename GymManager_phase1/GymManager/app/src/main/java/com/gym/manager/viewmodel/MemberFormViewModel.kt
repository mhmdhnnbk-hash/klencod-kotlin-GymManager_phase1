package com.gym.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.manager.data.repository.MemberRepository
import com.gym.manager.data.repository.Outcome
import com.gym.manager.util.DateUtils
import com.gym.manager.util.Validators
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FormState(
    val name: String = "",
    val memberId: String = "",
    val phone: String = "",
    val days: String = "30",
    val price: String = "",
    val date: LocalDate = DateUtils.today(),
    val method: String = "cash",
    val busy: Boolean = false,
    val error: String? = null,
    val confirm: String? = null,
    val loaded: Boolean = false
)

/** Used for both "add member" (editId == null) and "edit member". */
class MemberFormViewModel(
    private val repo: MemberRepository,
    val editId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(FormState())
    val state = _state.asStateFlow()
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (editId == null) {
            _state.update { it.copy(loaded = true) }
        } else {
            viewModelScope.launch {
                val m = repo.get(editId)
                if (m == null) {
                    _events.send(UiEvent.Done)
                } else {
                    _state.update {
                        it.copy(name = m.name, memberId = m.memberId, phone = m.phone, loaded = true)
                    }
                }
            }
        }
    }

    fun update(block: (FormState) -> FormState) = _state.update { block(it).copy(error = null) }
    fun dismissConfirm() = _state.update { it.copy(confirm = null) }

    fun submit(confirmDuplicate: Boolean = false) {
        val s = _state.value
        if (s.busy) return
        val days = Validators.parseInt(s.days)
        val price = Validators.parseLong(s.price)
        if (editId == null && (days == null || price == null)) {
            _state.update { it.copy(error = "أدخل عدد أيام وسعرًا صحيحين") }
            return
        }
        _state.update { it.copy(busy = true, error = null, confirm = null) }
        viewModelScope.launch {
            val outcome = if (editId == null) {
                repo.addMember(
                    MemberRepository.NewMember(
                        name = s.name, memberId = s.memberId, phone = s.phone,
                        subscribeDate = s.date, days = days!!, price = price!!, method = s.method
                    ),
                    confirmDuplicate
                )
            } else {
                repo.updateContact(editId, s.name, s.phone)
            }
            when (outcome) {
                Outcome.Ok -> {
                    _state.update { it.copy(busy = false) }
                    _events.send(UiEvent.Done)
                }
                is Outcome.Fail -> _state.update { it.copy(busy = false, error = outcome.message) }
                is Outcome.Confirm -> _state.update { it.copy(busy = false, confirm = outcome.message) }
            }
        }
    }
}
