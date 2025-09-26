package com.offordflix.data.repository;

import com.offordflix.data.ml.RecommendationEngine;
import com.offordflix.data.ml.UserBehaviorTracker;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class RecommendationRepository_Factory implements Factory<RecommendationRepository> {
  private final Provider<RecommendationEngine> recommendationEngineProvider;

  private final Provider<UserBehaviorTracker> userBehaviorTrackerProvider;

  private final Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider;

  public RecommendationRepository_Factory(
      Provider<RecommendationEngine> recommendationEngineProvider,
      Provider<UserBehaviorTracker> userBehaviorTrackerProvider,
      Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider) {
    this.recommendationEngineProvider = recommendationEngineProvider;
    this.userBehaviorTrackerProvider = userBehaviorTrackerProvider;
    this.contentDiscoveryRepositoryProvider = contentDiscoveryRepositoryProvider;
  }

  @Override
  public RecommendationRepository get() {
    return newInstance(recommendationEngineProvider.get(), userBehaviorTrackerProvider.get(), contentDiscoveryRepositoryProvider.get());
  }

  public static RecommendationRepository_Factory create(
      Provider<RecommendationEngine> recommendationEngineProvider,
      Provider<UserBehaviorTracker> userBehaviorTrackerProvider,
      Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider) {
    return new RecommendationRepository_Factory(recommendationEngineProvider, userBehaviorTrackerProvider, contentDiscoveryRepositoryProvider);
  }

  public static RecommendationRepository newInstance(RecommendationEngine recommendationEngine,
      UserBehaviorTracker userBehaviorTracker,
      ContentDiscoveryRepository contentDiscoveryRepository) {
    return new RecommendationRepository(recommendationEngine, userBehaviorTracker, contentDiscoveryRepository);
  }
}
