package com.offordflix.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class VideoPlayerRepository_Factory implements Factory<VideoPlayerRepository> {
  @Override
  public VideoPlayerRepository get() {
    return newInstance();
  }

  public static VideoPlayerRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static VideoPlayerRepository newInstance() {
    return new VideoPlayerRepository();
  }

  private static final class InstanceHolder {
    private static final VideoPlayerRepository_Factory INSTANCE = new VideoPlayerRepository_Factory();
  }
}
