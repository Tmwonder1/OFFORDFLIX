package com.offordflix.data.analytics;

import android.content.Context;
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
public final class RecommendationAnalytics_Factory implements Factory<RecommendationAnalytics> {
  private final Provider<Context> contextProvider;

  public RecommendationAnalytics_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public RecommendationAnalytics get() {
    return newInstance(contextProvider.get());
  }

  public static RecommendationAnalytics_Factory create(Provider<Context> contextProvider) {
    return new RecommendationAnalytics_Factory(contextProvider);
  }

  public static RecommendationAnalytics newInstance(Context context) {
    return new RecommendationAnalytics(context);
  }
}
