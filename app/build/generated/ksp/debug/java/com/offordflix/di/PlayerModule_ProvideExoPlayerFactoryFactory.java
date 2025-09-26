package com.offordflix.di;

import android.content.Context;
import androidx.media3.exoplayer.ExoPlayer;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlin.jvm.functions.Function0;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PlayerModule_ProvideExoPlayerFactoryFactory implements Factory<Function0<ExoPlayer>> {
  private final Provider<Context> contextProvider;

  public PlayerModule_ProvideExoPlayerFactoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public Function0<ExoPlayer> get() {
    return provideExoPlayerFactory(contextProvider.get());
  }

  public static PlayerModule_ProvideExoPlayerFactoryFactory create(
      Provider<Context> contextProvider) {
    return new PlayerModule_ProvideExoPlayerFactoryFactory(contextProvider);
  }

  public static Function0<ExoPlayer> provideExoPlayerFactory(Context context) {
    return Preconditions.checkNotNullFromProvides(PlayerModule.INSTANCE.provideExoPlayerFactory(context));
  }
}
