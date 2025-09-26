package com.offordflix.di;

import com.offordflix.data.local.ProfileDataStore;
import com.offordflix.data.repository.ProfileRepository;
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
public final class DataModule_ProvideProfileRepositoryFactory implements Factory<ProfileRepository> {
  private final Provider<ProfileDataStore> profileDataStoreProvider;

  public DataModule_ProvideProfileRepositoryFactory(
      Provider<ProfileDataStore> profileDataStoreProvider) {
    this.profileDataStoreProvider = profileDataStoreProvider;
  }

  @Override
  public ProfileRepository get() {
    return provideProfileRepository(profileDataStoreProvider.get());
  }

  public static DataModule_ProvideProfileRepositoryFactory create(
      Provider<ProfileDataStore> profileDataStoreProvider) {
    return new DataModule_ProvideProfileRepositoryFactory(profileDataStoreProvider);
  }

  public static ProfileRepository provideProfileRepository(ProfileDataStore profileDataStore) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideProfileRepository(profileDataStore));
  }
}
