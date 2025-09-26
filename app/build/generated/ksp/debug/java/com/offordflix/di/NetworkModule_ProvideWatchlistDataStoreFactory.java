package com.offordflix.di;

import android.content.Context;
import com.offordflix.data.local.WatchlistDataStore;
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
public final class NetworkModule_ProvideWatchlistDataStoreFactory implements Factory<WatchlistDataStore> {
  private final Provider<Context> contextProvider;

  public NetworkModule_ProvideWatchlistDataStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WatchlistDataStore get() {
    return provideWatchlistDataStore(contextProvider.get());
  }

  public static NetworkModule_ProvideWatchlistDataStoreFactory create(
      Provider<Context> contextProvider) {
    return new NetworkModule_ProvideWatchlistDataStoreFactory(contextProvider);
  }

  public static WatchlistDataStore provideWatchlistDataStore(Context context) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideWatchlistDataStore(context));
  }
}
