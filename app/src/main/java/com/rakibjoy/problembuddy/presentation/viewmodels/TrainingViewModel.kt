package com.rakibjoy.problembuddy.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    fun onEvent(event: TrainingEvents) {
        when (event) {
            is TrainingEvents.NewHandle -> {
                onNewHandle(event.handle)
            }
        }
    }

    private fun onNewHandle(handle: String) {

    }
}

sealed class TrainingEvents {
    data class NewHandle(val handle: String) : TrainingEvents()
}