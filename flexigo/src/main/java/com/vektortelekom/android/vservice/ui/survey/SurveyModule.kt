package com.vektortelekom.android.vservice.ui.survey

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.SurveyRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class SurveyModule {

    @Provides
    internal fun provideSurveyViewModel(surveyRepository: SurveyRepository, schedulerProvider: SchedulerProvider): SurveyViewModel {
        return SurveyViewModel(surveyRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: SurveyViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}