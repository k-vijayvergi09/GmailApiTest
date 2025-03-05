package com.example.gmailapitest

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel: ViewModel() {
    private val _messages = MutableStateFlow<List<Map<String, String>>>(emptyList())
    val messages: StateFlow<List<Map<String, String>>> get() = _messages

    private val _userAuthenticated: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val userAuthenticated: StateFlow<Boolean> get() = _userAuthenticated

    fun updateMessages(messagesDetails: List<Map<String, String>>) {
        _messages.value = messagesDetails
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading
    
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setAuthenticated(isAuthenticated: Boolean) {
        _userAuthenticated.value = isAuthenticated
    }
}