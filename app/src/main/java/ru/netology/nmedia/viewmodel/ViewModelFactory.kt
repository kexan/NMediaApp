package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.SignRepository

class ViewModelFactory(
    private val postRepository: PostRepository,
    private val signRepository: SignRepository,
    private val appAuth: AppAuth,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(PostViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                PostViewModel(postRepository, appAuth) as T
            }
            modelClass.isAssignableFrom(SignInViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SignInViewModel(signRepository) as T
            }
            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SignUpViewModel(signRepository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuthViewModel(appAuth) as T
            }
            else -> error("Unknown class $modelClass")
        }
}