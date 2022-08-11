package com.vektortelekom.android.vservice.ui.pastuses

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.PoolCarRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.ui.home.HomeViewModel
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class PastUsesModule {

    @Provides
    internal fun providePastUsesViewModel(schedulerProvider: SchedulerProvider): PastUsesViewModel {
        return PastUsesViewModel(schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: PastUsesViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}