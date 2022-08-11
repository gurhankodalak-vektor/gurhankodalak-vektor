package com.vektortelekom.android.vservice.ui.home

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class HomeModule {

    @Provides
    internal fun provideHomeViewModel(userRepository: UserRepository, poolCarRepository: PoolCarRepository, schedulerProvider: SchedulerProvider): HomeViewModel {
        return HomeViewModel(userRepository, poolCarRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: HomeViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}