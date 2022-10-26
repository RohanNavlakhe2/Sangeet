package com.yog.sangeet.sangeet_online.di

import android.content.Context
import com.yog.sangeet.sangeet_online.Constants.BASE_URL
import com.yog.sangeet.sangeet_online.data.network.SangeetApi
import com.yog.sangeet.sangeet_online.data.repository.SangeetRepositoryImpl
import com.yog.sangeet.sangeet_online.domain.repository.SangeetRepository
import com.yog.sangeet.sangeet_online.domain.usecases.GetVideoInfoUseCase
import com.yog.sangeet.sangeet_online.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SangeetModule {

    @Provides
    @Singleton
    fun getVideoInfoUseCase(sangeetRepository: SangeetRepository): GetVideoInfoUseCase = GetVideoInfoUseCase(sangeetRepository)


    @Provides
    @Singleton
    fun getSangeetRepository(sangeetApi: SangeetApi): SangeetRepository = SangeetRepositoryImpl(sangeetApi)

    @Provides
    @Singleton
    fun getRetrofit(okHttpClient: OkHttpClient): SangeetApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
        .create(SangeetApi::class.java)



    @Provides
    @Singleton
    fun getOkHttp(): OkHttpClient = OkHttpClient.Builder().run {
        addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
        return@run build()
    }

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)


}