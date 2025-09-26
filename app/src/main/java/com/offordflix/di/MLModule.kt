package com.offordflix.di

import android.content.Context
import com.offordflix.data.ml.RecommendationEngine
import com.offordflix.data.ml.UserBehaviorTracker
import com.offordflix.data.repository.RecommendationRepository
import com.offordflix.data.repository.ContentDiscoveryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for ML recommendation system dependencies.
 * 
 * Provides all components needed for intelligent content
 * recommendations and user behavior tracking.
 */
@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    /**
     * Provide UserBehaviorTracker for ML data collection.
     */
    @Provides
    @Singleton
    fun provideUserBehaviorTracker(
        @ApplicationContext context: Context
    ): UserBehaviorTracker {
        return UserBehaviorTracker(context)
    }

    /**
     * Provide RecommendationEngine for ML algorithms.
     */
    @Provides
    @Singleton
    fun provideRecommendationEngine(
        userBehaviorTracker: UserBehaviorTracker
    ): RecommendationEngine {
        return RecommendationEngine(userBehaviorTracker)
    }

    /**
     * Provide RecommendationRepository for recommendation services.
     */
    @Provides
    @Singleton
    fun provideRecommendationRepository(
        recommendationEngine: RecommendationEngine,
        userBehaviorTracker: UserBehaviorTracker,
        contentDiscoveryRepository: ContentDiscoveryRepository
    ): RecommendationRepository {
        return RecommendationRepository(
            recommendationEngine,
            userBehaviorTracker,
            contentDiscoveryRepository
        )
    }
}

