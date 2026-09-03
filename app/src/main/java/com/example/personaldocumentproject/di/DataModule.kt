package com.example.personaldocumentproject.di

import com.example.personaldocumentproject.data.local.SettingsLocalDataSource
import com.example.personaldocumentproject.data.local.dataStore
import com.example.personaldocumentproject.data.repository.SettingRepositoryImpl
import com.example.personaldocumentproject.domain.repository.SettingRepository
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSettingsLocalDataSource(
        @ApplicationContext context: Context
    ): SettingsLocalDataSource {
        return SettingsLocalDataSource(context.dataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        localDataSource: SettingsLocalDataSource
    ): SettingRepository {
        return SettingRepositoryImpl(localDataSource)
    }
}