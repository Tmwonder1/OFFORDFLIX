package com.offordflix.data.ml;

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
public final class RecommendationEngine_Factory implements Factory<RecommendationEngine> {
  private final Provider<UserBehaviorTracker> userBehaviorTrackerProvider;

  public RecommendationEngine_Factory(Provider<UserBehaviorTracker> userBehaviorTrackerProvider) {
    this.userBehaviorTrackerProvider = userBehaviorTrackerProvider;
  }

  @Override
  public RecommendationEngine get() {
    return newInstance(userBehaviorTrackerProvider.get());
  }

  public static RecommendationEngine_Factory create(
      Provider<UserBehaviorTracker> userBehaviorTrackerProvider) {
    return new RecommendationEngine_Factory(userBehaviorTrackerProvider);
  }

  public static RecommendationEngine newInstance(UserBehaviorTracker userBehaviorTracker) {
    return new RecommendationEngine(userBehaviorTracker);
  }
}
