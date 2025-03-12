
package ai.elimu.filamu.data.video.di

import ai.elimu.filamu.data.video.data.repository.VideoRepository
import ai.elimu.filamu.data.video.data.repository.VideoRepositoryImpl
import ai.elimu.filamu.data.video.data.repository.local.LocalVideoDataSource
import ai.elimu.filamu.data.video.data.repository.local.LocalVideoDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

// Tells Dagger this is a Dagger module
@Module
@InstallIn(ViewModelComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindVideoRepository(repo: VideoRepositoryImpl): VideoRepository

    @Binds
    abstract fun bindLocalVideoDataSource(dataSource: LocalVideoDataSourceImpl): LocalVideoDataSource

}
