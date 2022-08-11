package com.vektortelekom.android.vservice.ui.flexiride

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.FlexirideRepository
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.data.repository.TicketRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class FlexirideModule {

    @Provides
    internal fun provideFlexirideViewModel(ticketRepository: TicketRepository, flexirideRepository: FlexirideRepository, poolCarRepository: PoolCarRepository, userRepository: UserRepository, schedulerProvider: SchedulerProvider): FlexirideViewModel {
        return FlexirideViewModel(ticketRepository, flexirideRepository, poolCarRepository, userRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: FlexirideViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}