package com.offordflix.di

import android.content.Context
import com.offordflix.data.local.ProfileDataStore
import com.offordflix.data.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for data layer dependencies.
 * 
 * Provides repository and data store instances for dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    /**
     * Provide ProfileDataStore singleton.
     */
    @Provides
    @Singleton
    fun provideProfileDataStore(
        @ApplicationContext context: Context
    ): ProfileDataStore {
        return ProfileDataStore(context)
    }
    
    /**
     * Provide ProfileRepository singleton.
     */
    @Provides
    @Singleton
    fun provideProfileRepository(
        profileDataStore: ProfileDataStore
    ): ProfileRepository {
        return ProfileRepository(profileDataStore)
    }
}

