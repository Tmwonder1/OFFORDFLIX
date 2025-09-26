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
public final class WatchlistDataStore_Factory implements Factory<WatchlistDataStore> {
  private final Provider<Context> contextProvider;

  public WatchlistDataStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WatchlistDataStore get() {
    return newInstance(contextProvider.get());
  }

  public static WatchlistDataStore_Factory create(Provider<Context> contextProvider) {
    return new WatchlistDataStore_Factory(contextProvider);
  }

  public static WatchlistDataStore newInstance(Context context) {
    return new WatchlistDataStore(context);
  }
}
