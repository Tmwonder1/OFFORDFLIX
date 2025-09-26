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
public final class ContentFilterRepository_Factory implements Factory<ContentFilterRepository> {
  @Override
  public ContentFilterRepository get() {
    return newInstance();
  }

  public static ContentFilterRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ContentFilterRepository newInstance() {
    return new ContentFilterRepository();
  }

  private static final class InstanceHolder {
    private static final ContentFilterRepository_Factory INSTANCE = new ContentFilterRepository_Factory();
  }
}
