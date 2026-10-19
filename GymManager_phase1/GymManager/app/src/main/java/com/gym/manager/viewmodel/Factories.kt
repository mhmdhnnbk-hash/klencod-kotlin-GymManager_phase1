package com.gym.manager.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gym.manager.GymApp

private fun CreationExtras.gymApp(): GymApp = this[APPLICATION_KEY] as GymApp

object Factories {
    val dashboard = viewModelFactory {
        initializer { DashboardViewModel(gymApp().container.members) }
    }
    val members = viewModelFactory {
        initializer { MembersViewModel(gymApp().container.members) }
    }

    fun memberDetail(id: String) = viewModelFactory {
        initializer { MemberDetailViewModel(gymApp().container.members, id) }
    }

    fun memberForm(editId: String?) = viewModelFactory {
        initializer { MemberFormViewModel(gymApp().container.members, editId) }
    }
}
