package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload


interface SignRepository {
    suspend fun updateUser(login: String, password: String)
    suspend fun registerUser(login: String, password: String, name: String)
    suspend fun registerUserWithPhoto(
        login: String,
        password: String,
        name: String,
        photo: MediaUpload
    )
    suspend fun upload(upload: MediaUpload): Media
}