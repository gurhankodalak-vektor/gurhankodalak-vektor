package com.vektortelekom.android.vservice.ui.poolcar.reservation

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class PoolCarReservationModule {

    @Provides
    internal fun providePoolCarReservationViewModel(poolCarRepository: PoolCarRepository, userRepository: UserRepository, schedulerProvider: SchedulerProvider): PoolCarReservationViewModel {
        return PoolCarReservationViewModel(poolCarRepository, userRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: PoolCarReservationViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}