package com.offordflix.di;

import com.offordflix.data.ml.RecommendationEngine;
import com.offordflix.data.ml.UserBehaviorTracker;
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
public final class MLModule_ProvideRecommendationEngineFactory implements Factory<RecommendationEngine> {
  private final Provider<UserBehaviorTracker> userBehaviorTrackerProvider;

  public MLModule_ProvideRecommendationEngineFactory(
      Provider<UserBehaviorTracker> userBehaviorTrackerProvider) {
    this.userBehaviorTrackerProvider = userBehaviorTrackerProvider;
  }

  @Override
  public RecommendationEngine get() {
    return provideRecommendationEngine(userBehaviorTrackerProvider.get());
  }

  public static MLModule_ProvideRecommendationEngineFactory create(
      Provider<UserBehaviorTracker> userBehaviorTrackerProvider) {
    return new MLModule_ProvideRecommendationEngineFactory(userBehaviorTrackerProvider);
  }

  public static RecommendationEngine provideRecommendationEngine(
      UserBehaviorTracker userBehaviorTracker) {
    return Preconditions.checkNotNullFromProvides(MLModule.INSTANCE.provideRecommendationEngine(userBehaviorTracker));
  }
}
