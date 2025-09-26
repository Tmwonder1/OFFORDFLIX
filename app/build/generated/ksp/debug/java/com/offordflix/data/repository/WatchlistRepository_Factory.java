package com.offordflix.data.repository;

import com.offordflix.data.local.WatchlistDataStore;
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
public final class WatchlistRepository_Factory implements Factory<WatchlistRepository> {
  private final Provider<WatchlistDataStore> watchlistDataStoreProvider;

  public WatchlistRepository_Factory(Provider<WatchlistDataStore> watchlistDataStoreProvider) {
    this.watchlistDataStoreProvider = watchlistDataStoreProvider;
  }

  @Override
  public WatchlistRepository get() {
    return newInstance(watchlistDataStoreProvider.get());
  }

  public static WatchlistRepository_Factory create(
      Provider<WatchlistDataStore> watchlistDataStoreProvider) {
    return new WatchlistRepository_Factory(watchlistDataStoreProvider);
  }

  public static WatchlistRepository newInstance(WatchlistDataStore watchlistDataStore) {
    return new WatchlistRepository(watchlistDataStore);
  }
}
