package com.vektortelekom.android.vservice.ui.poolcar

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class PoolCarModule {

    @Provides
    internal fun providePoolCarViewModel(poolCarRepository: PoolCarRepository, schedulerProvider: SchedulerProvider): PoolCarViewModel {
        return PoolCarViewModel(poolCarRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: PoolCarViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}