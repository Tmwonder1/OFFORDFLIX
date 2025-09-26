package com.offordflix.ui.screens.home;

import com.offordflix.data.repository.ContentDiscoveryRepository;
import com.offordflix.data.repository.RecommendationRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider;

  private final Provider<WatchlistRepository> watchlistRepositoryProvider;

  private final Provider<RecommendationRepository> recommendationRepositoryProvider;

  public HomeViewModel_Factory(
      Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider,
      Provider<WatchlistRepository> watchlistRepositoryProvider,
      Provider<RecommendationRepository> recommendationRepositoryProvider) {
    this.contentDiscoveryRepositoryProvider = contentDiscoveryRepositoryProvider;
    this.watchlistRepositoryProvider = watchlistRepositoryProvider;
    this.recommendationRepositoryProvider = recommendationRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(contentDiscoveryRepositoryProvider.get(), watchlistRepositoryProvider.get(), recommendationRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<ContentDiscoveryRepository> contentDiscoveryRepositoryProvider,
      Provider<WatchlistRepository> watchlistRepositoryProvider,
      Provider<RecommendationRepository> recommendationRepositoryProvider) {
    return new HomeViewModel_Factory(contentDiscoveryRepositoryProvider, watchlistRepositoryProvider, recommendationRepositoryProvider);
  }

  public static HomeViewModel newInstance(ContentDiscoveryRepository contentDiscoveryRepository,
      WatchlistRepository watchlistRepository, RecommendationRepository recommendationRepository) {
    return new HomeViewModel(contentDiscoveryRepository, watchlistRepository, recommendationRepository);
  }
}
