package com.vektortelekom.android.vservice.ui.landing

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class LandingModule {

    @Provides
    internal fun provideLoginViewModel(): LandingViewModel {
        return LandingViewModel()
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: LandingViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}