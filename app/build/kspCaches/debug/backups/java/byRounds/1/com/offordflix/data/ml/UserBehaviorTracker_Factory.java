package com.offordflix.data.ml;

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
public final class UserBehaviorTracker_Factory implements Factory<UserBehaviorTracker> {
  private final Provider<Context> contextProvider;

  public UserBehaviorTracker_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UserBehaviorTracker get() {
    return newInstance(contextProvider.get());
  }

  public static UserBehaviorTracker_Factory create(Provider<Context> contextProvider) {
    return new UserBehaviorTracker_Factory(contextProvider);
  }

  public static UserBehaviorTracker newInstance(Context context) {
    return new UserBehaviorTracker(context);
  }
}
