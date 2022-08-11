package com.vektortelekom.android.vservice.ui.calendar

import androidx.lifecycle.ViewModelProvider
import com.vektortelekom.android.vservice.data.repository.CalendarRepository
import com.vektortelekom.android.vservice.data.repository.ShuttleRepository
import com.vektortelekom.android.vservice.data.repository.UserRepository
import com.vektortelekom.android.vservice.di.factory.ViewModelProviderFactory
import com.vektortelekom.android.vservice.utils.rx.SchedulerProvider
import dagger.Module
import dagger.Provides

@Module
class CalendarModule
{

    @Provides
    internal fun provideCalendarViewModel(userRepository: UserRepository, shuttleRepository: ShuttleRepository, calendarRepository: CalendarRepository, schedulerProvider: SchedulerProvider): CalendarViewModel {
        return CalendarViewModel(userRepository, shuttleRepository, calendarRepository, schedulerProvider)
    }

    @Provides
    internal fun provideViewModelProvider(viewModel: CalendarViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }


}