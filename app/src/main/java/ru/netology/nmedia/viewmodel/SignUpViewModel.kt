package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.SignRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(private val repository: SignRepository) : ViewModel() {

    private var _registrationSuccess = SingleLiveEvent<Boolean>()
    val registrationSuccess: LiveData<Boolean>
        get() = _registrationSuccess

    private val noPhoto = PhotoModel()
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun registerUser(login: String, password: String, name: String) = viewModelScope.launch {
        try {
            when (_photo.value) {
                noPhoto -> repository.registerUser(login, password, name)

                else -> _photo.value?.file?.let { file ->
                    repository.registerUserWithPhoto(login, password, name, MediaUpload(file))
                }
            }
            _registrationSuccess.value = true
        } catch (e: Exception) {
            _registrationSuccess.value = false
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }
}