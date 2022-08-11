package com.vektortelekom.android.vservice.ui.comments

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.TicketRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class CommentsModule {

    @Provides
    internal fun provideCommentsViewModel(ticketRepository: TicketRepository, userRepository: UserRepository, schedulerProvider: SchedulerProvider): CommentsViewModel {
        return CommentsViewModel(ticketRepository, userRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: CommentsViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}