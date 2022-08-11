package com.vektortelekom.android.vservice.ui.login

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class LoginModule {

    @Provides
    internal fun provideLoginViewModel(userRepository: UserRepository, schedulerProvider: SchedulerProvider): LoginViewModel {
        return LoginViewModel(userRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: LoginViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}