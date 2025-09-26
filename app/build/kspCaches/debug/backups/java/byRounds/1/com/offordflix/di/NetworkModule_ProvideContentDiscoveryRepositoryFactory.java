package com.offordflix.di;

import com.offordflix.data.api.TmdbApi;
import com.offordflix.data.repository.ContentDiscoveryRepository;
import com.offordflix.data.repository.ContentFilterRepository;
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
public final class NetworkModule_ProvideContentDiscoveryRepositoryFactory implements Factory<ContentDiscoveryRepository> {
  private final Provider<TmdbApi> tmdbApiProvider;

  private final Provider<ContentFilterRepository> contentFilterRepositoryProvider;

  public NetworkModule_ProvideContentDiscoveryRepositoryFactory(Provider<TmdbApi> tmdbApiProvider,
      Provider<ContentFilterRepository> contentFilterRepositoryProvider) {
    this.tmdbApiProvider = tmdbApiProvider;
    this.contentFilterRepositoryProvider = contentFilterRepositoryProvider;
  }

  @Override
  public ContentDiscoveryRepository get() {
    return provideContentDiscoveryRepository(tmdbApiProvider.get(), contentFilterRepositoryProvider.get());
  }

  public static NetworkModule_ProvideContentDiscoveryRepositoryFactory create(
      Provider<TmdbApi> tmdbApiProvider,
      Provider<ContentFilterRepository> contentFilterRepositoryProvider) {
    return new NetworkModule_ProvideContentDiscoveryRepositoryFactory(tmdbApiProvider, contentFilterRepositoryProvider);
  }

  public static ContentDiscoveryRepository provideContentDiscoveryRepository(TmdbApi tmdbApi,
      ContentFilterRepository contentFilterRepository) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideContentDiscoveryRepository(tmdbApi, contentFilterRepository));
  }
}
