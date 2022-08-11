package com.vektortelekom.android.vservice.ui.menu

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class MenuModule {

    @Provides
    internal fun provideMenuViewModel(userRepository: UserRepository, poolCarRepository: PoolCarRepository, schedulerProvider: SchedulerProvider): MenuViewModel {
        return MenuViewModel(userRepository, poolCarRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: MenuViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}