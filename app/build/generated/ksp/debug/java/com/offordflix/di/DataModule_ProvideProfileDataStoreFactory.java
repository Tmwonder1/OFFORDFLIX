package com.offordflix.di;

import android.content.Context;
import com.offordflix.data.local.ProfileDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DataModule_ProvideProfileDataStoreFactory implements Factory<ProfileDataStore> {
  private final Provider<Context> contextProvider;

  public DataModule_ProvideProfileDataStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ProfileDataStore get() {
    return provideProfileDataStore(contextProvider.get());
  }

  public static DataModule_ProvideProfileDataStoreFactory create(
      Provider<Context> contextProvider) {
    return new DataModule_ProvideProfileDataStoreFactory(contextProvider);
  }

  public static ProfileDataStore provideProfileDataStore(Context context) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideProfileDataStore(context));
  }
}
