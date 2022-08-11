package com.vektortelekom.android.vservice.ui.base.photo

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class TakePhotoModule {

    @Provides
    internal fun provideTakePhotoViewModel(): TakePhotoViewModel {
        return TakePhotoViewModel()
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: TakePhotoViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}