package com.offordflix.ui.screens.search;

import com.offordflix.data.repository.ContentDiscoveryRepository;
import com.offordflix.data.repository.WatchlistRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider;

  private final Provider<WatchlistRepository> watchlistRepositoryProvider;

  public SearchViewModel_Factory(
      Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider,
      Provider<WatchlistRepository> watchlistRepositoryProvider) {
    this.contentDiscoveryRepositoryProvider = contentDiscoveryRepositoryProvider;
    this.watchlistRepositoryProvider = watchlistRepositoryProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(contentDiscoveryRepositoryProvider.get(), watchlistRepositoryProvider.get());
  }

  public static SearchViewModel_Factory create(
      Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider,
      Provider<WatchlistRepository> watchlistRepositoryProvider) {
    return new SearchViewModel_Factory(contentDiscoveryRepositoryProvider, watchlistRepositoryProvider);
  }

  public static SearchViewModel newInstance(ContentDiscoveryRepository contentDiscoveryRepository,
      WatchlistRepository watchlistRepository) {
    return new SearchViewModel(contentDiscoveryRepository, watchlistRepository);
  }
}
