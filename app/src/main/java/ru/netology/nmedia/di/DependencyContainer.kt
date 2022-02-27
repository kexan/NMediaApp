package ru.netology.nmedia.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.repository.SignRepository
import ru.netology.nmedia.repository.SignRepositoryImpl
import ru.netology.nmedia.viewmodel.ViewModelFactory

class DependencyContainer(context: Context) {

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: DependencyContainer? = null

        fun getInstance(): DependencyContainer = requireNotNull(INSTANCE)

        fun init(context: Context) {
            INSTANCE = DependencyContainer(context)
        }
    }


    private val logging = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val authPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val okhttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            authPrefs.getString(AppAuth.tokenKey, null)?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okhttp)
        .build()

    private val apiService: ApiService = retrofit.create()

    private val appDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    val appAuth = AppAuth(apiService, authPrefs)

    val postRepository: PostRepository = PostRepositoryImpl(
        dao = appDb.postDao(),
        api = apiService
    )

    val signRepository: SignRepository = SignRepositoryImpl(appAuth = appAuth, api = apiService)

    val viewModelFactory = ViewModelFactory(postRepository, signRepository, appAuth)

}