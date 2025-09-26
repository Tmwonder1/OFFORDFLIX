package com.offordflix.di;

import com.offordflix.data.api.TmdbApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideTmdbApiFactory implements Factory<TmdbApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideTmdbApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public TmdbApi get() {
    return provideTmdbApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideTmdbApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideTmdbApiFactory(retrofitProvider);
  }

  public static TmdbApi provideTmdbApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideTmdbApi(retrofit));
  }
}
