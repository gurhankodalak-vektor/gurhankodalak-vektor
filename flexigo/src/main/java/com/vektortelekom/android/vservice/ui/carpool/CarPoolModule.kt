package com.vektortelekom.android.vservice.ui.carpool

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.RegistrationRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class CarPoolModule {

    @Provides
    internal fun provideLoginViewModel(registrationRepository: RegistrationRepository, schedulerProvider: SchedulerProvider): CarPoolViewModel {
        return CarPoolViewModel(registrationRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: CarPoolViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}