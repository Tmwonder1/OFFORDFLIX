package com.offordflix.data.repository;

import com.offordflix.data.api.TmdbApi;
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
public final class ContentDiscoveryRepository_Factory implements Factory<ContentDiscoveryRepository> {
  private final Provider<TmdbApi> tmdbApiProvider;

  private final Provider<ContentFilterRepository> contentFilterRepositoryProvider;

  public ContentDiscoveryRepository_Factory(Provider<TmdbApi> tmdbApiProvider,
      Provider<ContentFilterRepository> contentFilterRepositoryProvider) {
    this.tmdbApiProvider = tmdbApiProvider;
    this.contentFilterRepositoryProvider = contentFilterRepositoryProvider;
  }

  @Override
  public ContentDiscoveryRepository get() {
    return newInstance(tmdbApiProvider.get(), contentFilterRepositoryProvider.get());
  }

  public static ContentDiscoveryRepository_Factory create(Provider<TmdbApi> tmdbApiProvider,
      Provider<ContentFilterRepository> contentFilterRepositoryProvider) {
    return new ContentDiscoveryRepository_Factory(tmdbApiProvider, contentFilterRepositoryProvider);
  }

  public static ContentDiscoveryRepository newInstance(TmdbApi tmdbApi,
      ContentFilterRepository contentFilterRepository) {
    return new ContentDiscoveryRepository(tmdbApi, contentFilterRepository);
  }
}
