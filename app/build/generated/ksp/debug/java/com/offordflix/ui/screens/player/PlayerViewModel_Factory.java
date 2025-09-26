package com.offordflix.ui.screens.player;

import com.offordflix.data.repository.VideoPlayerRepository;
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
public final class PlayerViewModel_Factory implements Factory<PlayerViewModel> {
  private final Provider<VideoPlayerRepository> videoPlayerRepositoryProvider;

  public PlayerViewModel_Factory(Provider<VideoPlayerRepository> videoPlayerRepositoryProvider) {
    this.videoPlayerRepositoryProvider = videoPlayerRepositoryProvider;
  }

  @Override
  public PlayerViewModel get() {
    return newInstance(videoPlayerRepositoryProvider.get());
  }

  public static PlayerViewModel_Factory create(
      Provider<VideoPlayerRepository> videoPlayerRepositoryProvider) {
    return new PlayerViewModel_Factory(videoPlayerRepositoryProvider);
  }

  public static PlayerViewModel newInstance(VideoPlayerRepository videoPlayerRepository) {
    return new PlayerViewModel(videoPlayerRepository);
  }
}
