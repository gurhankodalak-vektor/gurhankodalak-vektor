package com.vektortelekom.android.vservice.ui.splash

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.MobileRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class SplashModule {

    @Provides
    internal fun provideSplashViewModel(mobileRepository: MobileRepository, userRepository: UserRepository, schedulerProvider: SchedulerProvider): SplashViewModel {
        return SplashViewModel(mobileRepository, userRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: SplashViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}