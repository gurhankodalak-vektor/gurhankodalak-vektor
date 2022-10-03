package com.vektortelekom.android.vservice.di.module

import com.vektor.ktx.data.remote.usermanagement.oauth.OAuthService
import com.vektor.vshare_api_ktx.service.ConstantsService
import com.vektor.vshare_api_ktx.service.DocumentService
import com.vektor.vshare_api_ktx.service.VersionService


import com.vektortelekom.android.vservice.data.remote.service.*
import com.vektortelekom.android.vservice.data.repository.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object RepositoryModule {

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideUserRepository(loginService: LoginService, dashboardService: DashboardService, userService: UserService, documentService: DocumentService, oAuthService: OAuthService): UserRepository {
        return UserRepository(
                loginService, dashboardService, userService, documentService, oAuthService
        )
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideMobileRepository(versionService: VersionService, constantsService: ConstantsService): MobileRepository {
        return MobileRepository(
                versionService, constantsService
        )
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideShuttleRepository(routeService: RouteService): ShuttleRepository {
        return ShuttleRepository(
                routeService
        )
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideTicketRepository(ticketService: TicketService, userService: UserService): TicketRepository {
        return TicketRepository(
                ticketService, userService
        )
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun providePoolCarRepository(poolCarService: PoolCarService): PoolCarRepository {
        return PoolCarRepository(
                poolCarService
        )
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideTaxiRepository(taxiService: TaxiService): TaxiRepository {
        return TaxiRepository(
                taxiService
        )
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideFlexirideRepository(flexirideService: FlexirideService): FlexirideRepository {
        return FlexirideRepository(
                flexirideService
        )
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideCalendarRepository(calendarService: CalendarService): CalendarRepository {
        return CalendarRepository(
                calendarService
        )
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideSurveyRepository(surveyService: SurveyService): SurveyRepository {
        return SurveyRepository(
                surveyService
        )
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideRegistrationRepository(registrationService: RegistrationService): RegistrationRepository {
        return RegistrationRepository(
            registrationService
        )
    }

}