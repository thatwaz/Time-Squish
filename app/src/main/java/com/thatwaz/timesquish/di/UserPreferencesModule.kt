package com.thatwaz.timesquish.di

import android.content.Context
import com.thatwaz.timesquish.util.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserPreferencesModule {

    @Provides
    @Singleton
    fun provideUserPreferences(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context
    ): UserPreferences {
        return UserPreferences(context)
    }
}
