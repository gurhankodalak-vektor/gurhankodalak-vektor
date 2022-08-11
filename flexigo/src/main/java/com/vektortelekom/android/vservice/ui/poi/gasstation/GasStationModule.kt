package com.vektortelekom.android.vservice.ui.poi.gasstation

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class GasStationModule {

    @Provides
    internal fun provideCreditCardViewModel(poolCarRepository: PoolCarRepository, schedulerProvider: SchedulerProvider): GasStationViewModel {
        return GasStationViewModel(poolCarRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: GasStationViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}