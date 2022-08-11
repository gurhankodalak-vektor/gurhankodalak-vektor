package com.vektortelekom.android.vservice.ui.taxi

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.TaxiRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class TaxiModule {

    @Provides
    internal fun provideTaxiViewModel(taxiRepository: TaxiRepository, userRepository: UserRepository, schedulerProvider: SchedulerProvider): TaxiViewModel {
        return TaxiViewModel(taxiRepository, userRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: TaxiViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}