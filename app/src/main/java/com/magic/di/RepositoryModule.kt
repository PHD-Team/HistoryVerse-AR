package com.magic.di

import com.magic.data.repositories.ChatRepository
import com.magic.data.repositories.ChatRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun provideChatRepository(chatRepositoryImpl: ChatRepositoryImpl): ChatRepository

}