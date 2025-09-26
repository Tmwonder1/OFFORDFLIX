package com.offordflix.di;

import com.offordflix.data.repository.VideoPlayerRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class PlayerModule_ProvideVideoPlayerRepositoryFactory implements Factory<VideoPlayerRepository> {
  @Override
  public VideoPlayerRepository get() {
    return provideVideoPlayerRepository();
  }

  public static PlayerModule_ProvideVideoPlayerRepositoryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static VideoPlayerRepository provideVideoPlayerRepository() {
    return Preconditions.checkNotNullFromProvides(PlayerModule.INSTANCE.provideVideoPlayerRepository());
  }

  private static final class InstanceHolder {
    private static final PlayerModule_ProvideVideoPlayerRepositoryFactory INSTANCE = new PlayerModule_ProvideVideoPlayerRepositoryFactory();
  }
}
