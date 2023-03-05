package com.vektortelekom.android.vservice.ui.registration

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.RegistrationRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class RegistrationModule {

    @Provides
    internal fun provideLoginViewModel(registrationRepository: RegistrationRepository, userRepository: UserRepository, schedulerProvider: SchedulerProvider): RegistrationViewModel {
        return RegistrationViewModel(registrationRepository, userRepository ,schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: RegistrationViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}