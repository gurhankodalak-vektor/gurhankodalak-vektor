package com.vektortelekom.android.vservice.ui.route

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.ShuttleRepository
import com.vektortelekom.android.vservice.data.repository.TicketRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.ui.route.search.RouteSearchViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class RouteModule {

    @Provides
    internal fun provideShuttleViewModel(shuttleRepository: ShuttleRepository, ticketRepository: TicketRepository, schedulerProvider: SchedulerProvider): RouteSearchViewModel {
        return RouteSearchViewModel(shuttleRepository, ticketRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: RouteSearchViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}