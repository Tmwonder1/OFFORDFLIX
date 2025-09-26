package com.offordflix.ui.screens.profile;

import com.offordflix.data.repository.ProfileRepository;
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
public final class ProfileSelectionViewModel_Factory implements Factory<ProfileSelectionViewModel> {
  private final Provider<ProfileRepository> profileRepositoryProvider;

  public ProfileSelectionViewModel_Factory(Provider<ProfileRepository> profileRepositoryProvider) {
    this.profileRepositoryProvider = profileRepositoryProvider;
  }

  @Override
  public ProfileSelectionViewModel get() {
    return newInstance(profileRepositoryProvider.get());
  }

  public static ProfileSelectionViewModel_Factory create(
      Provider<ProfileRepository> profileRepositoryProvider) {
    return new ProfileSelectionViewModel_Factory(profileRepositoryProvider);
  }

  public static ProfileSelectionViewModel newInstance(ProfileRepository profileRepository) {
    return new ProfileSelectionViewModel(profileRepository);
  }
}
