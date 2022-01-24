package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.repository.SignRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

class SignInViewModel : ViewModel() {

    private val repository = SignRepositoryImpl()

    private var _loginSuccess = SingleLiveEvent<Boolean>()

    val loginSuccess: LiveData<Boolean>
        get() = _loginSuccess


    fun updateUser(login: String, password: String) = viewModelScope.launch {
        try {
            repository.updateUser(login, password)
            _loginSuccess.value = true
        } catch (e: Exception) {
            _loginSuccess.value = false
        }
    }
}