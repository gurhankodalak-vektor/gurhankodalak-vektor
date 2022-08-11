package com.vektortelekom.android.vservice.ui.poolcar.intercity

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class PoolCarIntercityModule {

    @Provides
    internal fun providePoolCarIntercityViewModel(poolCarRepository: PoolCarRepository, schedulerProvider: SchedulerProvider): PoolCarIntercityViewModel {
        return PoolCarIntercityViewModel(poolCarRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: PoolCarIntercityViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}