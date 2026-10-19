package com.gym.manager.viewmodel

/** One-shot events from a ViewModel to its screen. */
sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
    data object Done : UiEvent
    data object Renewed : UiEvent
    data object Deleted : UiEvent
}
