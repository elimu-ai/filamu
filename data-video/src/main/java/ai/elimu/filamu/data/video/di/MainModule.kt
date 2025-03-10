package ai.elimu.filamu.data.video.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
object MainModule {
    @Singleton
    @Provides
    @ApplicationContext
    fun provideContext(application: Application): Context = application
}