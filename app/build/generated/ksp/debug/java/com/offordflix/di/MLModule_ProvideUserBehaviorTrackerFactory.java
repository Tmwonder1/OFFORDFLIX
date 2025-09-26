package com.offordflix.di;

import android.content.Context;
import com.offordflix.data.ml.UserBehaviorTracker;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MLModule_ProvideUserBehaviorTrackerFactory implements Factory<UserBehaviorTracker> {
  private final Provider<Context> contextProvider;

  public MLModule_ProvideUserBehaviorTrackerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UserBehaviorTracker get() {
    return provideUserBehaviorTracker(contextProvider.get());
  }

  public static MLModule_ProvideUserBehaviorTrackerFactory create(
      Provider<Context> contextProvider) {
    return new MLModule_ProvideUserBehaviorTrackerFactory(contextProvider);
  }

  public static UserBehaviorTracker provideUserBehaviorTracker(Context context) {
    return Preconditions.checkNotNullFromProvides(MLModule.INSTANCE.provideUserBehaviorTracker(context));
  }
}
