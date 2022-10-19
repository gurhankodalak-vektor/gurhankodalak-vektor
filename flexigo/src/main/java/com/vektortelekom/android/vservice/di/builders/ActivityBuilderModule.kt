package com.vektortelekom.android.vservice.di.builders

import com.vektortelekom.android.vservice.ui.base.photo.TakePhotoActivity
import com.vektortelekom.android.vservice.ui.base.photo.TakePhotoFragment
import com.vektortelekom.android.vservice.ui.base.photo.TakePhotoModule
import com.vektortelekom.android.vservice.ui.base.photo.TakePhotoPreviewFragment
import com.vektortelekom.android.vservice.ui.calendar.CalendarActivity
import com.vektortelekom.android.vservice.ui.calendar.CalendarModule
import com.vektortelekom.android.vservice.ui.calendar.fragment.CalendarAccountsFragment
import com.vektortelekom.android.vservice.ui.calendar.fragment.CalendarMainFragment
import com.vektortelekom.android.vservice.ui.comments.CommentsActivity
import com.vektortelekom.android.vservice.ui.comments.CommentsModule
import com.vektortelekom.android.vservice.ui.comments.fragment.CommentsAddFragment
import com.vektortelekom.android.vservice.ui.comments.fragment.CommentsMainFragment
import com.vektortelekom.android.vservice.ui.comments.fragment.CommentsPhotoPreviewFragment
import com.vektortelekom.android.vservice.ui.flexiride.FlexirideActivity
import com.vektortelekom.android.vservice.ui.flexiride.FlexirideModule
import com.vektortelekom.android.vservice.ui.flexiride.fragment.*
import com.vektortelekom.android.vservice.ui.home.HomeActivity
import com.vektortelekom.android.vservice.ui.home.HomeModule
import com.vektortelekom.android.vservice.ui.login.LoginActivity
import com.vektortelekom.android.vservice.ui.login.LoginModule
import com.vektortelekom.android.vservice.ui.login.fragment.ForgotPasswordFragment
import com.vektortelekom.android.vservice.ui.login.fragment.LoginFragment
import com.vektortelekom.android.vservice.ui.menu.MenuActivity
import com.vektortelekom.android.vservice.ui.menu.MenuModule
import com.vektortelekom.android.vservice.ui.menu.fragment.*
import com.vektortelekom.android.vservice.ui.notification.NotificationActivity
import com.vektortelekom.android.vservice.ui.notification.NotificationModule
import com.vektortelekom.android.vservice.ui.pastuses.PastUsesActivity
import com.vektortelekom.android.vservice.ui.pastuses.PastUsesModule
import com.vektortelekom.android.vservice.ui.pastuses.fragment.PastUsesMainFragment
import com.vektortelekom.android.vservice.ui.poi.gasstation.GasStationActivity
import com.vektortelekom.android.vservice.ui.poi.gasstation.GasStationModule
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarActivity
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarModule
import com.vektortelekom.android.vservice.ui.poolcar.fragment.*
import com.vektortelekom.android.vservice.ui.poolcar.intercity.PoolCarIntercityActivity
import com.vektortelekom.android.vservice.ui.poolcar.intercity.PoolCarIntercityModule
import com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment.PoolCarIntercityBeforeStartFragment
import com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment.PoolCarIntercityFinishFragment
import com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment.PoolCarIntercityRentalFragment
import com.vektortelekom.android.vservice.ui.poolcar.intercity.fragment.PoolCarIntercityStartFragment
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationActivity
import com.vektortelekom.android.vservice.ui.poolcar.reservation.PoolCarReservationModule
import com.vektortelekom.android.vservice.ui.poolcar.reservation.fragment.*
import com.vektortelekom.android.vservice.ui.registration.RegistrationActivity
import com.vektortelekom.android.vservice.ui.registration.fragment.RegistrationFragment
import com.vektortelekom.android.vservice.ui.registration.RegistrationModule
import com.vektortelekom.android.vservice.ui.registration.fragment.CompanyCodeFragment
import com.vektortelekom.android.vservice.ui.registration.fragment.EmailCodeFragment
import com.vektortelekom.android.vservice.ui.registration.fragment.SelectCampusFragment
import com.vektortelekom.android.vservice.ui.route.*
import com.vektortelekom.android.vservice.ui.route.bottomsheet.BottomSheetRouteSearchLocation
import com.vektortelekom.android.vservice.ui.route.bottomsheet.BottomSheetSelectRoutes
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleActivity
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleModule
import com.vektortelekom.android.vservice.ui.shuttle.bottomsheet.*
import com.vektortelekom.android.vservice.ui.shuttle.fragment.*
import com.vektortelekom.android.vservice.ui.splash.SplashActivity
import com.vektortelekom.android.vservice.ui.splash.SplashModule
import com.vektortelekom.android.vservice.ui.survey.SurveyActivity
import com.vektortelekom.android.vservice.ui.survey.SurveyModule
import com.vektortelekom.android.vservice.ui.survey.bottomsheet.BottomSheetCommuteOptions
import com.vektortelekom.android.vservice.ui.route.bottomsheet.BottomSheetSingleDateCalendar
import com.vektortelekom.android.vservice.ui.route.search.*
import com.vektortelekom.android.vservice.ui.shuttle.ShuttleReservationViewFragment
import com.vektortelekom.android.vservice.ui.survey.fragment.SurveyFragment
import com.vektortelekom.android.vservice.ui.survey.fragment.VanPoolLocationPermissionFragment
import com.vektortelekom.android.vservice.ui.taxi.TaxiActivity
import com.vektortelekom.android.vservice.ui.taxi.TaxiModule
import com.vektortelekom.android.vservice.ui.taxi.fragment.*
import com.vektortelekom.android.vservice.ui.vanpool.VanPoolDriverActivity
import com.vektortelekom.android.vservice.ui.vanpool.fragment.VanpoolDriverApprovalFragment
import com.vektortelekom.android.vservice.ui.vanpool.fragment.VanpoolDriverStationsFragment
import com.vektortelekom.android.vservice.ui.vanpool.fragment.VanpoolPassengerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = [SplashModule::class])
    internal abstract fun contributeSplashActivity(): SplashActivity

    @ContributesAndroidInjector(modules = [LoginModule::class])
    internal abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [LoginModule::class])
    internal abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector(modules = [LoginModule::class])
    internal abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment

    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeHomeActivity(): HomeActivity

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleActivity(): ShuttleActivity

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleMainFragment(): ShuttleMainFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleServicePlanningFragment(): ShuttleServicePlanningFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleServicePlanningReservationFragment(): ShuttleServicePlanningReservationFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleInformationFragment(): ShuttleInformationFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleQrCodeFragment(): ShuttleQrCodeFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleQrReaderFragment(): ShuttleQrReaderFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleRouteSearchFromToFragment(): ShuttleRouteSearchFromToFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleFromToMapFragment(): ShuttleFromToMapFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeBottomSheetEditShuttle(): BottomSheetEditShuttle

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeBottomSheetRoutePreview(): BottomSheetRoutePreview

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeBottomSheetFromWhere(): BottomSheetFromWhere

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeBottomSheetSearchRoute(): BottomSheetSearchRoute

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeBottomSheetRoutes(): BottomSheetRoutes

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeBottomSheetMakeReservation(): BottomSheetMakeReservation

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuActivity(): MenuActivity

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuMainFragment(): MenuMainFragment

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuEditProfileFragment(): MenuEditProfileFragment

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuChangePasswordFragment(): MenuChangePasswordFragment

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuAddAddressFragment(): MenuAddAddressFragment

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuSettingsFragment(): MenuSettingsFragment

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuQuestionnaireFragment(): MenuQuestionnaireFragment

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuDrivingLicenseFragment(): MenuDrivingLicenseFragment

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuDrivingLicencePreviewFragment(): MenuDrivingLicencePreviewFragment

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuPdfViewerFragment(): MenuPdfViewerFragment

    @ContributesAndroidInjector(modules = [MenuModule::class])
    internal abstract fun contributeMenuProfilePhotoPreviewFragment(): MenuProfilePhotoPreviewFragment

    @ContributesAndroidInjector(modules = [CommentsModule::class])
    internal abstract fun contributeCommentsActivity(): CommentsActivity

    @ContributesAndroidInjector(modules = [CommentsModule::class])
    internal abstract fun contributeCommentsMainFragment(): CommentsMainFragment

    @ContributesAndroidInjector(modules = [CommentsModule::class])
    internal abstract fun contributeCommentsAddFragment(): CommentsAddFragment

    @ContributesAndroidInjector(modules = [CommentsModule::class])
    internal abstract fun contributeCommentsPhotoPreviewFragment(): CommentsPhotoPreviewFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarActivity(): PoolCarActivity

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarMainFragment(): PoolCarMainFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarParkFragment(): PoolCarParkFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarVehicleFragment(): PoolCarVehicleFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarFindCarFragment(): PoolCarFindCarFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarExternalDamageControlFragment(): PoolCarExternalDamageControlFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarInternalDamageControlFragment(): PoolCarInternalDamageControlFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarRentalFragment(): PoolCarRentalFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarAssistanceFragment(): PoolCarAssistanceFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarAddNewDamageFragment(): PoolCarAddNewDamageFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarAddNewInternalDamageFragment(): PoolCarAddNewInternalDamageFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarAddNewDamagePreviewFragment(): PoolCarAddNewDamagePreviewFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolDirectorRentalStartFragment(): PoolCarDirectorRentalStartRentalFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolDirectorRentalFinishFragment(): PoolCarFinishDirectorRentalFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarRentalFinishControlFragment(): PoolCarRentalFinishControlFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarRentalFinishParkInfoFragment(): PoolCarRentalFinishParkInfoFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarRentalFinishParkPhotoPreviewFragment(): PoolCarRentalFinishParkPhotoPreviewFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarSatisfactionSurveyFragment(): PoolCarSatisfactionSurveyFragment

    @ContributesAndroidInjector(modules = [PoolCarModule::class])
    internal abstract fun contributePoolCarRentalQrCodeReaderFragment(): PoolCarRentalQrCodeReaderFragment

    @ContributesAndroidInjector(modules = [PoolCarReservationModule::class])
    internal abstract fun contributePoolCarReservationActivity(): PoolCarReservationActivity

    @ContributesAndroidInjector(modules = [PoolCarReservationModule::class])
    internal abstract fun contributePoolCarReservationsFragment(): PoolCarReservationsFragment

    @ContributesAndroidInjector(modules = [PoolCarReservationModule::class])
    internal abstract fun contributePoolCarAddReservationFragment(): PoolCarAddReservationFragment

    @ContributesAndroidInjector(modules = [PoolCarReservationModule::class])
    internal abstract fun contributePoolCarReservationSelectToFragment(): PoolCarReservationSelectToFragment

    @ContributesAndroidInjector(modules = [PoolCarReservationModule::class])
    internal abstract fun contributePoolCarSelectPoiFragment(): PoolCarSelectPoiFragment

    @ContributesAndroidInjector(modules = [PoolCarReservationModule::class])
    internal abstract fun contributePoolCarReservationQrFragment(): PoolCarReservationQrFragment

    @ContributesAndroidInjector(modules = [PoolCarIntercityModule::class])
    internal abstract fun contributePoolCarIntercityActivity(): PoolCarIntercityActivity

    @ContributesAndroidInjector(modules = [PoolCarIntercityModule::class])
    internal abstract fun contributePoolCarIntercityStartFragment(): PoolCarIntercityStartFragment

    @ContributesAndroidInjector(modules = [PoolCarIntercityModule::class])
    internal abstract fun contributePoolCarIntercityBeforeStartFragment(): PoolCarIntercityBeforeStartFragment

    @ContributesAndroidInjector(modules = [PoolCarIntercityModule::class])
    internal abstract fun contributePoolCarIntercityFinishFragment(): PoolCarIntercityFinishFragment

    @ContributesAndroidInjector(modules = [PoolCarIntercityModule::class])
    internal abstract fun contributePoolCarIntercityRentalFragment(): PoolCarIntercityRentalFragment

    @ContributesAndroidInjector(modules = [NotificationModule::class])
    internal abstract fun contributeNotificationActivity(): NotificationActivity

    @ContributesAndroidInjector(modules = [TaxiModule::class])
    internal abstract fun contributeTaxiActivity(): TaxiActivity

    @ContributesAndroidInjector(modules = [TaxiModule::class])
    internal abstract fun contributeTaxiStartFragment(): TaxiStartFragment

    @ContributesAndroidInjector(modules = [TaxiModule::class])
    internal abstract fun contributeTaxiFinishFragment(): TaxiFinishFragment

    @ContributesAndroidInjector(modules = [TaxiModule::class])
    internal abstract fun contributeTaxiReportFragment(): TaxiReportFragment

    @ContributesAndroidInjector(modules = [TaxiModule::class])
    internal abstract fun contributeTaxiFindLocationFragment(): TaxiFindLocationFragment

    @ContributesAndroidInjector(modules = [TaxiModule::class])
    internal abstract fun contributeTaxiListFragment(): TaxiListFragment

    @ContributesAndroidInjector(modules = [FlexirideModule::class])
    internal abstract fun contributeFlexirideActivity(): FlexirideActivity

    @ContributesAndroidInjector(modules = [FlexirideModule::class])
    internal abstract fun contributeFlexirideFromFragment(): FlexirideFromFragment

    @ContributesAndroidInjector(modules = [FlexirideModule::class])
    internal abstract fun contributeFlexirideListFragment(): FlexirideListFragment

    @ContributesAndroidInjector(modules = [FlexirideModule::class])
    internal abstract fun contributeFlexirideSearchFromFragment(): FlexirideSearchFromFragment

    @ContributesAndroidInjector(modules = [FlexirideModule::class])
    internal abstract fun contributeFlexirideSearchToFragment(): FlexirideSearchToFragment

    @ContributesAndroidInjector(modules = [FlexirideModule::class])
    internal abstract fun contributeFlexiridePlannedFragment(): FlexiridePlannedFragment

    @ContributesAndroidInjector(modules = [FlexirideModule::class])
    internal abstract fun contributeFlexirideSurveyFragment(): FlexirideSurveyFragment

    @ContributesAndroidInjector(modules = [GasStationModule::class])
    internal abstract fun contributeGasStationActivity(): GasStationActivity

    @ContributesAndroidInjector(modules = [CalendarModule::class])
    internal abstract fun contributeCalendarActivity(): CalendarActivity

    @ContributesAndroidInjector(modules = [CalendarModule::class])
    internal abstract fun contributeCalendarMainFragment(): CalendarMainFragment

    @ContributesAndroidInjector(modules = [CalendarModule::class])
    internal abstract fun contributeCalendarAccountsFragment(): CalendarAccountsFragment

    @ContributesAndroidInjector(modules = [PastUsesModule::class])
    internal abstract fun contributePastUsesActivity(): PastUsesActivity

    @ContributesAndroidInjector(modules = [PastUsesModule::class])
    internal abstract fun contributePastUsesMainFragment(): PastUsesMainFragment

    @ContributesAndroidInjector(modules = [TakePhotoModule::class])
    internal abstract fun contributeTakePhotoActivity(): TakePhotoActivity

    @ContributesAndroidInjector(modules = [TakePhotoModule::class])
    internal abstract fun contributeTakePhotoFragment(): TakePhotoFragment

    @ContributesAndroidInjector(modules = [TakePhotoModule::class])
    internal abstract fun contributeTakePhotoPreviewFragment(): TakePhotoPreviewFragment

    @ContributesAndroidInjector(modules = [SurveyModule::class])
    internal abstract fun contributeSurveyActivity(): SurveyActivity

    @ContributesAndroidInjector(modules = [SurveyModule::class])
    internal abstract fun contributeSurveyFragment(): SurveyFragment

    @ContributesAndroidInjector(modules = [SurveyModule::class])
    internal abstract fun contributeVanPoolLocationPermissionFragment(): VanPoolLocationPermissionFragment

    @ContributesAndroidInjector(modules = [SurveyModule::class])
    internal abstract fun contributeBottomSheetCommuteOptions(): BottomSheetCommuteOptions

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeRouteSelectionFragment(): RouteSelectionFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeStopSelectionFragment(): StopSelectionFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeRouteSelectionActivity(): RouteSelectionActivity

    @ContributesAndroidInjector(modules = [RouteModule::class])
    internal abstract fun contributeRouteSearchActivity(): RouteSearchActivity

    @ContributesAndroidInjector(modules = [RouteModule::class])
    internal abstract fun contributeRouteSearchFragment(): RouteSearchFragment

    @ContributesAndroidInjector(modules = [RouteModule::class])
    internal abstract fun contributeRouteSearchReservationFragment(): RouteSearchReservationFragment

    @ContributesAndroidInjector(modules = [RouteModule::class])
    internal abstract fun contributeReservationViewFragment(): ReservationViewFragment

    @ContributesAndroidInjector(modules = [RouteModule::class])
    internal abstract fun contributeRouteSearchToFragment(): BottomSheetRouteSearchLocation

    @ContributesAndroidInjector(modules = [RouteModule::class])
    internal abstract fun contributeRouteSearchTimeSelectionFragment(): RouteSearchTimeSelectionFragment

    @ContributesAndroidInjector(modules = [RouteModule::class])
    internal abstract fun contributeRoutePreview(): RoutePreview

    @ContributesAndroidInjector(modules = [RouteModule::class])
    internal abstract fun contributeBottomSheetRoutesSearchResult(): RouteSearchResultFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeBottomSheetSearchRoutes(): BottomSheetSelectRoutes

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeShuttleReservationViewFragment(): ShuttleReservationViewFragment

    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeVanPoolDriverActivity(): VanPoolDriverActivity

    @ContributesAndroidInjector(modules = [HomeModule::class])
    internal abstract fun contributeVanpoolFragment(): VanpoolDriverApprovalFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeBottomSheetCalendar(): BottomSheetCalendar

    @ContributesAndroidInjector(modules = [RouteModule::class])
    internal abstract fun contributeBottomSheetSingleDateCalendar(): BottomSheetSingleDateCalendar

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeVanpoolDriverStationsFragment(): VanpoolDriverStationsFragment

    @ContributesAndroidInjector(modules = [ShuttleModule::class])
    internal abstract fun contributeVanpoolPassengerFragment(): VanpoolPassengerFragment

    @ContributesAndroidInjector(modules = [RegistrationModule::class])
    internal abstract fun contributeRegistrationActivity(): RegistrationActivity

    @ContributesAndroidInjector(modules = [RegistrationModule::class])
    internal abstract fun contributeRegistrationFragment(): RegistrationFragment

    @ContributesAndroidInjector(modules = [RegistrationModule::class])
    internal abstract fun contributeEmailCodeFragment(): EmailCodeFragment

    @ContributesAndroidInjector(modules = [RegistrationModule::class])
    internal abstract fun contributeSelectCampusFragment(): SelectCampusFragment

    @ContributesAndroidInjector(modules = [RegistrationModule::class])
    internal abstract fun contributeCompanyCodeFragment(): CompanyCodeFragment

}