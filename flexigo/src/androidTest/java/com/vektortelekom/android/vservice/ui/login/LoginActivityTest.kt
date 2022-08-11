package com.vektortelekom.android.vservice.ui.login

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.intent.IntentCallback
import androidx.test.runner.intent.IntentMonitorRegistry
import com.vektor.share.api.ktx.model.DoorStatus
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.TestUtils
import com.vektortelekom.android.vservice.data.model.DeviceType
import com.vektortelekom.android.vservice.ui.home.adapter.DashboardAdapter
import com.vektortelekom.android.vservice.ui.poolcar.PoolCarActivity
import com.vektortelekom.android.vservice.ui.poolcar.adapter.ParkingLotsAdapter
import kotlinx.android.synthetic.main.pool_car_odometer_dialog.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.Exception

@RunWith(AndroidJUnit4ClassRunner::class)
class LoginActivityTest {

    @Rule
    @JvmField
    val activityScenarioRule = IntentsTestRule(PoolCarActivity::class.java)

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Rule
    @JvmField
    val grantPermissionRule2: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Before
    fun setUp() {
        TestUtils.login()
    }

    // starting from login, navigate to Pool Car Activity
    // when Pool Car Activity started, there are 3 candidate page to be show
    // 1. Parking Loots Fragment (no rent)
    // 2. Find Car Fragment (rent is created)
    // 3. Rent Main Fragment (rent is started)
    @Test
    fun isActivityVisible() {

        // Click to Pool Car
        onView(withId(R.id.recycler_view_dashboard)).perform(RecyclerViewActions.actionOnItemAtPosition<DashboardAdapter.DashboardViewHolder>(1, click()))

        Thread.sleep(1000)

        // Check Pool Car Action list visibility (rent a car, make a reservation, reservation list)
        onView(withId(R.id.card_view_rent_a_car)).check(matches(isDisplayed()))

        // Click rent a car
        onView(withId(R.id.card_view_rent_a_car)).perform(click())

        Thread.sleep(3000)

        // Pool Car Activity is started
        onView(withId(R.id.text_view_tool_bar)).check(matches(withText(R.string.pool_car)))

        Thread.sleep(1500)

        var rentState: RentState

        try{
            // Check if there is a rent
            onView(withId(R.id.text_view_parking_lots)).check(matches(isDisplayed()))

            rentState = RentState.NO_RENT

        }
        catch (e: Exception) {
            try{
                // check find car page is shown
                // if it is shown, rental is created but not started
                onView(withId(R.id.pool_car_find_car_fragment)).check(matches(isDisplayed()))

                rentState = RentState.CREATED


            }
            catch (e: Exception) {

                rentState = RentState.STARTED
            }

        }

        when(rentState) {
            RentState.NO_RENT -> {
                // If there is no rent, continue with it
                continueWithNoRent()
            }
            RentState.CREATED -> {
                // If there is a created rent, continue with it
                continueWithCreateRent()
            }
            RentState.STARTED -> {
                continueWithAlreadyStartedRent()
            }
        }


    }

    fun continueWithNoRent() {
        // select parking loot if exists
        onView(withId(R.id.recycler_view_parking_lots)).check(matches(hasMinimumChildCount(1)))
        onView(withId(R.id.recycler_view_parking_lots)).perform(RecyclerViewActions.actionOnItemAtPosition<ParkingLotsAdapter.ParkingLotsViewHolder>(0, click()))
        Thread.sleep(3000)
        // check selected park details are shown
        onView(withId(R.id.pool_car_park_fragment)).check(matches(isDisplayed()))

        // select car from parking loot if exists
        onView(withId(R.id.recycler_view_car_models)).check(matches(hasMinimumChildCount(1)))
        onView(withId(R.id.recycler_view_car_models)).perform(RecyclerViewActions.actionOnItemAtPosition<ParkingLotsAdapter.ParkingLotsViewHolder>(0, click()))

        // check selected car details are shown
        onView(withId(R.id.pool_car_vehicle_fragment)).check(matches(isDisplayed()))

        Thread.sleep(1000)

        // create rental
        onView(withId(R.id.start_personal_rental)).check(matches(isDisplayed())).perform(click())

        Thread.sleep(5000)

        continueWithCreateRent()


    }


    fun continueWithCreateRent() {

        // navigate external car damage page
        onView(withId(R.id.button_start_control)).check(matches(isDisplayed())).perform(click())

        Thread.sleep(500)


        // check external car damage page is shown
        onView(withId(R.id.pool_car_external_damage_control_fragment)).check(matches(isDisplayed()))

        val deviceType = activityScenarioRule.activity.getSelectedDeviceType()

        // start rental
        onView(withId(R.id.button_start_rental)).check(matches(isDisplayed())).perform(click())



        if(deviceType == DeviceType.NONE) {
            onView(withText(R.string.odometer_text_start)).check(matches(isDisplayed()))
            // add km value
            onView(withId(R.id.edit_text_km)).perform(replaceText("123"))
            onView(withId(R.id.button_submit_odometer)).check(matches(isDisplayed())).perform(click())
        }

        Thread.sleep(3000)
        checkVehicleDoorsOpen(deviceType)



    }

    // check door opening dialog
    // it is recursive because check door status is a recursive service, status is checked periodically until status is door opened
    fun checkVehicleDoorsOpen(deviceType: DeviceType?) {
        var isOk: Boolean
        try {
            when(deviceType) {
                DeviceType.REMOTE_DOOR -> {
                    onView(withText(R.string.doors_opening_info)).check(matches(isDisplayed()))
                }
                else -> {
                    onView(withText(R.string.doors_opening_no_device_text1)).check(matches(isDisplayed()))
                }
            }
            isOk = true
        }
        catch (e: Exception) {
            isOk = false
        }

        if(isOk) {
            Thread.sleep(3000)
            checkVehicleDoorsOpen(deviceType)
        }
        else {
            continueWithAlreadyStartedRent()
        }
    }

    fun continueWithAlreadyStartedRent() {

        // check pool car rental fragment is shown
        onView(withId(R.id.pool_car_rental_fragment)).check(matches(isDisplayed()))

        // finish rental
        onView(withId(R.id.button_finish_rental)).check(matches(isDisplayed())).perform(click())

        Thread.sleep(500)

        // check pool car rental finish control fragment is shown
        onView(withId(R.id.pool_car_rental_finish_control_fragment)).check(matches(isDisplayed()))

        // continue button shold be disabled
        onView(withId(R.id.button_continue)).check(matches(not(isEnabled())))

        // check control statements
        onView(withId(R.id.check_box_1)).check(matches(isNotChecked())).perform(scrollTo(), click())
        onView(withId(R.id.check_box_2)).check(matches(isNotChecked())).perform(scrollTo(), click())
        onView(withId(R.id.check_box_3)).check(matches(isNotChecked())).perform(scrollTo(), click())
        onView(withId(R.id.check_box_confirm)).check(matches(isNotChecked())).perform(scrollTo(), click())

        // continue button shold be enabled. If it is, click it
        onView(withId(R.id.button_continue)).check(matches(isEnabled())).perform(click())

        Thread.sleep(500)

        // check pool car rental finish park info is shown
        onView(withId(R.id.pool_car_rental_finish_park_info_fragment)).check(matches(isDisplayed()))

        onView(withId(R.id.button_finish_rental_park_info)).check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.rental_finish_fields_empty)).check(matches(isDisplayed()))

        Thread.sleep(500)

        onView(withId(R.id.ok_button)).check(matches(isDisplayed())).perform(click())


        // CAMERA EMULATE START

        val expectedIntent: Matcher<Intent> = hasAction(MediaStore.ACTION_IMAGE_CAPTURE)

        val activityResult = createImageCaptureActivityResultStub(activityScenarioRule.activity)
        intending(expectedIntent).respondWith(activityResult)

        val cameraIntentCallback = intentCallback()
        IntentMonitorRegistry.getInstance().addIntentCallback(cameraIntentCallback)

        onView(withId(R.id.card_view_add_photo)).check(matches(isDisplayed())).perform(click())
        intended(expectedIntent)

        IntentMonitorRegistry.getInstance().removeIntentCallback(cameraIntentCallback)

        // CAMERA EMULATE END

        Thread.sleep(500)

        // check the photo preview fragment visible with photo and accept photo

        onView(withId(R.id.pool_car_rental_finish_park_photo_preview_fragment)).check(matches(isDisplayed()))

        onView(withId(R.id.button_submit)).check(matches(isDisplayed())).perform(click())

        Thread.sleep(500)

        // check pool car rental finish park info fragment finish and perform click of submit

        onView(withId(R.id.button_finish_rental_park_info)).check(matches(isDisplayed())).perform(click())

        // because all the fields are not filled, error dialog should be shown

        onView(withText(R.string.rental_finish_fields_empty)).check(matches(isDisplayed()))

        Thread.sleep(500)

        // skip the error dialog

        onView(withId(R.id.ok_button)).check(matches(isDisplayed())).perform(click())

        // add some description
        onView(withId(R.id.edit_text_description_park_info)).perform(replaceText("some description"))
        // click to check box
        onView(withId(R.id.check_box_confirm_park_info)).check(matches(isNotChecked())).perform(scrollTo(), click())

        Thread.sleep(1000)

        // reclick the submit button
        onView(withId(R.id.button_finish_rental_park_info)).check(matches(isDisplayed())).perform(click())

        val deviceType = activityScenarioRule.activity.getSelectedDeviceType()

        if(deviceType == DeviceType.NONE) {
            onView(withText(R.string.odometer_text_end)).check(matches(isDisplayed()))
            // add km value
            onView(withId(R.id.edit_text_km)).perform(replaceText("123"))
            onView(withId(R.id.button_submit_odometer)).check(matches(isDisplayed())).perform(click())
        }

        Thread.sleep(3000)

        checkVehicleDoorsClose(deviceType)

    }

    // check door closing dialog
    // it is recursive because check door status is a recursive service, status is checked periodically until status is door closed
    fun checkVehicleDoorsClose(deviceType: DeviceType?) {
        var isOk: Boolean
        try {
            when(deviceType) {
                DeviceType.REMOTE_DOOR -> {
                    onView(withText(R.string.doors_closing_info)).check(matches(isDisplayed()))
                }
                else -> {
                    onView(withText(R.string.doors_locking_no_device_text1)).check(matches(isDisplayed()))
                }
            }
            isOk = true

        }
        catch (e: Exception) {
            isOk = false

        }

        if(isOk) {
            Thread.sleep(3000)
            checkVehicleDoorsClose(deviceType)
        }
        else {
            continueAfterDoorsClosed()
        }

    }

    fun continueAfterDoorsClosed() {
        // check the satisfaction survey visible
        onView(withId(R.id.pool_car_satisfaction_survey_fragment)).check(matches(isDisplayed()))

        // skip the satisfaction survey
        onView(withId(R.id.button_submit_pool_car_satisfaction_survey)).check(matches(isDisplayed())).perform(click())

        Thread.sleep(500)

        // return to home page
        onView(withId(R.id.layout_root_home)).check(matches(isDisplayed()))

        Thread.sleep(5000)

    }

    private fun createCameraActivityResultStub(): ActivityResult {
        val resources = InstrumentationRegistry.getInstrumentation().context.resources
        val imageUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        resources.getResourcePackageName(R.drawable.calendar_1) + "/" +
                        resources.getResourceTypeName(R.drawable.calendar_1) + "/" +
                        resources.getResourceEntryName(R.drawable.calendar_1)
        )

        val resultIntent = Intent()
        resultIntent.data = imageUri

        return ActivityResult(Activity.RESULT_OK, resultIntent)

    }

    fun createImageCaptureActivityResultStub(activity: Activity): ActivityResult {
        val bundle = Bundle()
        // Create the Intent that will include the bundle.
        val resultData = Intent()
        resultData.putExtras(bundle)

        // Create the ActivityResult with the Intent.
        return ActivityResult(Activity.RESULT_OK, resultData)
    }

    private var imageName = "No Image Name"

    private fun intentCallback(resourceId : Int = R.mipmap.ic_launcher) :IntentCallback  {
        return IntentCallback {
            if (it.action == MediaStore.ACTION_IMAGE_CAPTURE) {
                it.extras?.getParcelable<Uri>(MediaStore.EXTRA_OUTPUT).run {
                    imageName = File(it.getParcelableExtra<Parcelable>(MediaStore.EXTRA_OUTPUT).toString()).name
                    val context : Context = InstrumentationRegistry.getInstrumentation().targetContext
                    this?.let {
                        val outStream = context.contentResolver.openOutputStream(this)
                        val bitmap : Bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                    }

                }
            }
        }
    }

    enum class RentState {
            NO_RENT,
            CREATED,
            STARTED
    }


    /*val scenario = launchFragmentInContainer<LoginFragment>(factory = LoginFragmentFactory(), themeResId = R.style.AppTheme)

        onView(withId(R.id.edit_text_email)).perform(replaceText("efeaaaxxx.aydin@vektortelekom.com"))
        onView(withId(R.id.edit_text_password)).perform(replaceText("Ea123456"))
        onView(withId(R.id.button_forgot_password)).check(matches(isDisplayed()))
        //onView(withId(R.id.button_forgot_password)).perform(click())

        val scenario2 = launchFragmentInContainer<ForgotPasswordFragment>(factory = LoginFragmentFactory(), themeResId = R.style.AppTheme)

        onView(withId(R.id.text_view_forgot_password_info)).check(matches(isDisplayed()))*/

}