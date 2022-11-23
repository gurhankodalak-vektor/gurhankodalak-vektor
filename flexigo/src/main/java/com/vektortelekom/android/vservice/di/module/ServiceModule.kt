package com.vektortelekom.android.vservice.di.module

import com.vektor.ktx.data.remote.usermanagement.oauth.OAuthService
import com.vektor.ktx.data.remote.usermanagement.register.RegisterService
import com.vektor.ktx.di.annotation.RetrofitWithTokenJson
import com.vektor.ktx.di.annotation.RetrofitWithTokenJson2
import com.vektor.ktx.di.annotation.RetrofitWithoutTokenJson
import com.vektor.ktx.di.annotation.RetrofitWithoutTokenJson2
import com.vektor.vshare_api_ktx.service.*
import com.vektortelekom.android.vservice.data.remote.service.*
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
object ServiceModule {

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideOAuthService(@RetrofitWithTokenJson retrofit: Retrofit): OAuthService {
        return retrofit.create(OAuthService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideRegisterService(@RetrofitWithoutTokenJson retrofit: Retrofit): RegisterService {
        return retrofit.create(RegisterService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideMernisService(@RetrofitWithoutTokenJson retrofit: Retrofit): MernisService {
        return retrofit.create(MernisService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideParkService(@RetrofitWithoutTokenJson retrofit: Retrofit): ParkService {
        return retrofit.create(ParkService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideCarService(@RetrofitWithTokenJson retrofit: Retrofit): CarService {
        return retrofit.create(CarService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideCreditCardService(@RetrofitWithTokenJson retrofit: Retrofit): CreditCardService {
        return retrofit.create(CreditCardService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideRentalService(@RetrofitWithTokenJson retrofit: Retrofit): RentalService {
        return retrofit.create(RentalService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideCampaignService(@RetrofitWithoutTokenJson retrofit: Retrofit): CampaignService {
        return retrofit.create(CampaignService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideUploadImageService(@RetrofitWithTokenJson retrofit: Retrofit): UploadImageService {
        return retrofit.create(UploadImageService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideFaqService(@RetrofitWithoutTokenJson retrofit: Retrofit): FaqService {
        return retrofit.create(FaqService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideDocumentService(@RetrofitWithTokenJson2 retrofit: Retrofit): DocumentService {
        return retrofit.create(DocumentService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideNotificationService(@RetrofitWithTokenJson retrofit: Retrofit): NotificationService {
        return retrofit.create(NotificationService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideBalanceService(@RetrofitWithTokenJson retrofit: Retrofit): OfferService {
        return retrofit.create(OfferService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun providePoiService(@RetrofitWithTokenJson retrofit: Retrofit): PoiService {
        return retrofit.create(PoiService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideVersionService(@RetrofitWithoutTokenJson2 retrofit: Retrofit): VersionService {
        return retrofit.create(VersionService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideCustomerService(@RetrofitWithTokenJson retrofit: Retrofit): CustomerService {
        return retrofit.create(CustomerService::class.java)
    }

    @Provides
    @Singleton
    @JvmStatic
    internal fun provideConstantsService(@RetrofitWithoutTokenJson retrofit: Retrofit): ConstantsService {
        return retrofit.create(ConstantsService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideLoginService(@RetrofitWithTokenJson retrofit: Retrofit): LoginService {
        return retrofit.create(LoginService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideDashboardService(@RetrofitWithTokenJson retrofit: Retrofit): DashboardService {
        return retrofit.create(DashboardService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideRouteService(@RetrofitWithTokenJson retrofit: Retrofit): RouteService {
        return retrofit.create(RouteService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideUserService(@RetrofitWithTokenJson retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideTicketService(@RetrofitWithTokenJson retrofit: Retrofit): TicketService {
        return retrofit.create(TicketService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun providePoolCarService(@RetrofitWithTokenJson2 retrofit: Retrofit): PoolCarService {
        return retrofit.create(PoolCarService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideTaxiService(@RetrofitWithTokenJson retrofit: Retrofit): TaxiService {
        return retrofit.create(TaxiService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideFlexirideService(@RetrofitWithTokenJson2 retrofit: Retrofit): FlexirideService {
        return retrofit.create(FlexirideService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideCalendarService(@RetrofitWithTokenJson retrofit: Retrofit): CalendarService {
        return retrofit.create(CalendarService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideSurveyService(@RetrofitWithTokenJson retrofit: Retrofit): SurveyService {
        return retrofit.create(SurveyService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideRegistrationService(@RetrofitWithTokenJson retrofit: Retrofit): RegistrationService {
        return retrofit.create(RegistrationService::class.java)
    }

    @Singleton
    @Provides
    @JvmStatic
    internal fun provideCarPoolService(@RetrofitWithTokenJson retrofit: Retrofit): CarPoolService {
        return retrofit.create(CarPoolService::class.java)
    }

}