package com.offordflix.di;

import com.offordflix.data.local.WatchlistDataStore;
import com.offordflix.data.repository.WatchlistRepository;
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
public final class NetworkModule_ProvideWatchlistRepositoryFactory implements Factory<WatchlistRepository> {
  private final Provider<WatchlistDataStore> watchlistDataStoreProvider;

  public NetworkModule_ProvideWatchlistRepositoryFactory(
      Provider<WatchlistDataStore> watchlistDataStoreProvider) {
    this.watchlistDataStoreProvider = watchlistDataStoreProvider;
  }

  @Override
  public WatchlistRepository get() {
    return provideWatchlistRepository(watchlistDataStoreProvider.get());
  }

  public static NetworkModule_ProvideWatchlistRepositoryFactory create(
      Provider<WatchlistDataStore> watchlistDataStoreProvider) {
    return new NetworkModule_ProvideWatchlistRepositoryFactory(watchlistDataStoreProvider);
  }

  public static WatchlistRepository provideWatchlistRepository(
      WatchlistDataStore watchlistDataStore) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideWatchlistRepository(watchlistDataStore));
  }
}
