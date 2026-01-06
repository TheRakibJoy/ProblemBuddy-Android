package com.rakibjoy.problembuddy.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.data.api.RetrofitInstance
import kotlinx.coroutines.launch

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    fun onEvent(event: TrainingEvents) {
        when (event) {
            is TrainingEvents.NewHandle -> {
                onNewHandle(event.handle)
            }
        }
    }

    private fun onNewHandle(handle: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.contestHistoryApi.getUserRating(handle.trim().lowercase().replace(" ", ""))
                if (response.status == "OK") {
                    Log.d("TrainingViewModel", "Success: ${response.result}")
                } else {
                    Log.e("TrainingViewModel", "Error: ${response.status}")
                }
            } catch (e: Exception) {
                Log.e("TrainingViewModel", "Exception: ${e.message}")
            }
        }
    }
}

sealed class TrainingEvents {
    data class NewHandle(val handle: String) : TrainingEvents()
}
