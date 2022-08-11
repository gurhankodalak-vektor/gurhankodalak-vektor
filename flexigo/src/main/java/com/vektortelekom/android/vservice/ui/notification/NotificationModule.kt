package com.vektortelekom.android.vservice.ui.notification

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class NotificationModule {

    @Provides
    internal fun provideLoginViewModel(userRepository: UserRepository, schedulerProvider: SchedulerProvider): NotificationViewModel {
        return NotificationViewModel(userRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: NotificationViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}