package com.vektortelekom.android.vservice.ui.carpool

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.CarPoolRepository
import com.vektortelekom.android.vservice.data.repository.RegistrationRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.ui.carpool.CarPoolViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class CarPoolModule {

    @Provides
    internal fun provideLoginViewModel(carPoolRepository: CarPoolRepository, schedulerProvider: SchedulerProvider): CarPoolViewModel {
        return CarPoolViewModel(carPoolRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: CarPoolViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}