package com.rakibjoy.problembuddy.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakibjoy.problembuddy.data.model.*
import com.rakibjoy.problembuddy.data.repository.CodeforcesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CodeforcesRepository
) : ViewModel() {
    val TAG = "TEST"
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _recommendationState = MutableStateFlow(RecommendationState())
    val recommendationState: StateFlow<RecommendationState> = _recommendationState.asStateFlow()

    fun login(handle: String) {
        viewModelScope.launch {
            Log.d(TAG, "Login started for handle: $handle")
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                val profileResult = repository.getUserProfile(handle)
                profileResult.fold(
                    onSuccess = { profile ->
                        Log.d(TAG, "Login success for $handle")
                        _authState.value = AuthState(
                            isLoggedIn = true,
                            handle = handle,
                            isLoading = false
                        )
                        _profileState.value = ProfileState(
                            userProfile = profile,
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        Log.d(TAG, "Login failed for $handle: ${exception.message}")
                        _authState.value = AuthState(
                            isLoggedIn = false,
                            isLoading = false,
                            error = exception.message ?: "Failed to login"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.d(TAG, "Login exception for $handle: ${e.message}")
                _authState.value = AuthState(
                    isLoggedIn = false,
                    isLoading = false,
                    error = e.message ?: "Network error"
                )
            }
        }
    }

    fun logout() {
        Log.d(TAG, "Logout called")
        _authState.value = AuthState()
        _profileState.value = ProfileState()
        _recommendationState.value = RecommendationState()
    }

    fun loadProfile(handle: String) {
        viewModelScope.launch {
            Log.d(TAG, "Loading profile for handle: $handle")
            _profileState.value = _profileState.value.copy(isLoading = true, error = null)

            try {
                val weakAreasResult = repository.getWeakAreas(handle)
                weakAreasResult.fold(
                    onSuccess = { weakAreas ->
                        Log.d(TAG, "Loaded weak areas for $handle: ${weakAreas.size} found")
                        _profileState.value = _profileState.value.copy(
                            weakAreas = weakAreas,
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        Log.d(TAG, "Failed to load weak areas for $handle: ${exception.message}")
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load weak areas"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.d(TAG, "Exception loading profile for $handle: ${e.message}")
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Network error"
                )
            }
        }
    }

    fun getRecommendations(handle: String) {
        viewModelScope.launch {
            Log.d(TAG, "Getting recommendations for handle: $handle")
            _recommendationState.value = _recommendationState.value.copy(isLoading = true, error = null)

            try {
                // First get weak areas
                val weakAreasResult = repository.getWeakAreas(handle)
                weakAreasResult.fold(
                    onSuccess = { weakAreas ->
                        Log.d(TAG, "Weak areas for recommendations: ${weakAreas.map { it.tag }}")
                        val weakTags = weakAreas.map { it.tag }
                        val recommendationsResult = repository.getRecommendedProblems(handle, weakTags)

                        recommendationsResult.fold(
                            onSuccess = { problems ->
                                Log.d(TAG, "Recommendations loaded: ${problems.size} problems")
                                _recommendationState.value = RecommendationState(
                                    problems = problems,
                                    isLoading = false
                                )
                            },
                            onFailure = { exception ->
                                Log.d(TAG, "Failed to get recommendations: ${exception.message}")
                                _recommendationState.value = RecommendationState(
                                    isLoading = false,
                                    error = exception.message ?: "Failed to get recommendations"
                                )
                            }
                        )
                    },
                    onFailure = { exception ->
                        Log.d(TAG, "Failed to analyze weak areas: ${exception.message}")
                        _recommendationState.value = RecommendationState(
                            isLoading = false,
                            error = exception.message ?: "Failed to analyze weak areas"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.d(TAG, "Exception getting recommendations: ${e.message}")
                _recommendationState.value = RecommendationState(
                    isLoading = false,
                    error = e.message ?: "Network error"
                )
            }
        }
    }

    fun clearError() {
        Log.d(TAG, "Clearing errors in ViewModel")
        _authState.value = _authState.value.copy(error = null)
        _profileState.value = _profileState.value.copy(error = null)
        _recommendationState.value = _recommendationState.value.copy(error = null)
    }
}