package com.vektortelekom.android.vservice.ui.shuttle

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.ShuttleRepository
import com.vektortelekom.android.vservice.data.repository.TicketRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class ShuttleModule {

    @Provides
    internal fun provideShuttleViewModel(shuttleRepository: ShuttleRepository, userRepository: UserRepository, ticketRepository: TicketRepository, schedulerProvider: SchedulerProvider): ShuttleViewModel {
        return ShuttleViewModel(shuttleRepository, userRepository, ticketRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: ShuttleViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}