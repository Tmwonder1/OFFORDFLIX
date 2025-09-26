package com.offordflix.data.repository;

import com.offordflix.data.local.ProfileDataStore;
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
public final class ProfileRepository_Factory implements Factory<ProfileRepository> {
  private final Provider<ProfileDataStore> profileDataStoreProvider;

  public ProfileRepository_Factory(Provider<ProfileDataStore> profileDataStoreProvider) {
    this.profileDataStoreProvider = profileDataStoreProvider;
  }

  @Override
  public ProfileRepository get() {
    return newInstance(profileDataStoreProvider.get());
  }

  public static ProfileRepository_Factory create(
      Provider<ProfileDataStore> profileDataStoreProvider) {
    return new ProfileRepository_Factory(profileDataStoreProvider);
  }

  public static ProfileRepository newInstance(ProfileDataStore profileDataStore) {
    return new ProfileRepository(profileDataStore);
  }
}
