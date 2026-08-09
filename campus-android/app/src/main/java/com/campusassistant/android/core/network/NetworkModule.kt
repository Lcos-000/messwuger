package com.campusassistant.android.core.network

import com.campusassistant.android.BuildConfig
import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.data.api.AuthApi
import com.campusassistant.android.data.api.EmptyClassroomApi
import com.campusassistant.android.data.api.GradesApi
import com.campusassistant.android.data.api.PersonalizationApi
import com.campusassistant.android.data.api.ScheduleApi
import com.campusassistant.android.data.api.UserApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkModule(tokenDataStore: TokenDataStore) {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(BaseUrlOverrideInterceptor())
        .addInterceptor(AuthInterceptor(tokenDataStore))
        .addInterceptor(UnauthorizedInterceptor(tokenDataStore))
        .addInterceptor(NetworkLogger())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val scheduleApi: ScheduleApi = retrofit.create(ScheduleApi::class.java)
    val gradesApi: GradesApi = retrofit.create(GradesApi::class.java)
    val emptyClassroomApi: EmptyClassroomApi = retrofit.create(EmptyClassroomApi::class.java)
    val personalizationApi: PersonalizationApi = retrofit.create(PersonalizationApi::class.java)
}
