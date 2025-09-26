package com.offordflix.data.local;

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
public final class ProfileDataStore_Factory implements Factory<ProfileDataStore> {
  private final Provider<Context> contextProvider;

  public ProfileDataStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ProfileDataStore get() {
    return newInstance(contextProvider.get());
  }

  public static ProfileDataStore_Factory create(Provider<Context> contextProvider) {
    return new ProfileDataStore_Factory(contextProvider);
  }

  public static ProfileDataStore newInstance(Context context) {
    return new ProfileDataStore(context);
  }
}
