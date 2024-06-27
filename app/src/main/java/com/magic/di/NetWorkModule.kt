package com.magic.di

import com.google.firebase.storage.FirebaseStorage
import com.magic.data.network.ChatBotService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetWorkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(20, TimeUnit.MINUTES)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    @Singleton
    @Provides
    fun provideParser(): GsonConverterFactory = GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideRetrofitBuilder(
        client: OkHttpClient,
        factory: GsonConverterFactory,
        @Named("baseUrl") baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(factory)
            .build()

    @Singleton
    @Provides
    @Named("baseUrl")
    fun provideBaseUrl(): String = "https://f1dc-102-41-52-217.ngrok-free.app/"

    @Singleton
    @Provides
    fun provideChatBotService(retrofit: Retrofit): ChatBotService =
        retrofit.create(ChatBotService::class.java)


    @Singleton
    @Provides
    fun provideFirebaseStorage() = FirebaseStorage.getInstance()
}