package com.offordflix.di;

import com.offordflix.data.ml.RecommendationEngine;
import com.offordflix.data.ml.UserBehaviorTracker;
import com.offordflix.data.repository.ContentDiscoveryRepository;
import com.offordflix.data.repository.RecommendationRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class MLModule_ProvideRecommendationRepositoryFactory implements Factory<RecommendationRepository> {
  private final Provider<RecommendationEngine> recommendationEngineProvider;

  private final Provider<UserBehaviorTracker> userBehaviorTrackerProvider;

  private final Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider;

  public MLModule_ProvideRecommendationRepositoryFactory(
      Provider<RecommendationEngine> recommendationEngineProvider,
      Provider<UserBehaviorTracker> userBehaviorTrackerProvider,
      Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider) {
    this.recommendationEngineProvider = recommendationEngineProvider;
    this.userBehaviorTrackerProvider = userBehaviorTrackerProvider;
    this.contentDiscoveryRepositoryProvider = contentDiscoveryRepositoryProvider;
  }

  @Override
  public RecommendationRepository get() {
    return provideRecommendationRepository(recommendationEngineProvider.get(), userBehaviorTrackerProvider.get(), contentDiscoveryRepositoryProvider.get());
  }

  public static MLModule_ProvideRecommendationRepositoryFactory create(
      Provider<RecommendationEngine> recommendationEngineProvider,
      Provider<UserBehaviorTracker> userBehaviorTrackerProvider,
      Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider) {
    return new MLModule_ProvideRecommendationRepositoryFactory(recommendationEngineProvider, userBehaviorTrackerProvider, contentDiscoveryRepositoryProvider);
  }

  public static RecommendationRepository provideRecommendationRepository(
      RecommendationEngine recommendationEngine, UserBehaviorTracker userBehaviorTracker,
      ContentDiscoveryRepository contentDiscoveryRepository) {
    return Preconditions.checkNotNullFromProvides(MLModule.INSTANCE.provideRecommendationRepository(recommendationEngine, userBehaviorTracker, contentDiscoveryRepository));
  }
}
