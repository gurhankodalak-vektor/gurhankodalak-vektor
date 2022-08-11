package com.vektortelekom.android.vservice

import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import com.vektortelekom.android.vservice.ui.login.LoginActivity
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher


class TestUtils {

    companion object {

        fun login() {
            // open Login Screen
            val activityScenario = ActivityScenario.launch(LoginActivity::class.java)

            // fill form and send to server
            Espresso.onView(ViewMatchers.withId(R.id.edit_text_email)).perform(ViewActions.replaceText("efe.aydin@vektortelekom.com"))
            Espresso.onView(ViewMatchers.withId(R.id.edit_text_password)).perform(ViewActions.replaceText("Ea123456"))
            Espresso.onView(ViewMatchers.withId(R.id.button_sign_in)).perform(ViewActions.click())

            // give time to request
            Thread.sleep(5000)

            // Navigated to Home Screen
            Espresso.onView(ViewMatchers.withId(R.id.card_view_rent_a_car)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))

            skipTutorialsIfExists()
        }

        fun skipTutorialsIfExists() {
            Thread.sleep(500)
            var isDisplayed: Boolean
            try {
                Espresso.onView(ViewMatchers.withText(R.string.skip)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                isDisplayed = true
            }
            catch (e: Exception) {
                isDisplayed = false
            }
            if(isDisplayed) {
                Espresso.onView(ViewMatchers.withText(R.string.skip)).perform(ViewActions.click())
                skipTutorialsIfExists()
            }
        }

        fun swipe(startLocation: GeneralLocation, endLocation: GeneralLocation): ViewAction {
            /*return GeneralSwipeAction(Swipe.FAST,
                    CoordinatesProvider {
                        floatArrayOf(1f, 2f)
                    },
                    CoordinatesProvider {
                        floatArrayOf(100f, 20f)
                    },
                    Press.FINGER)*/
            return GeneralSwipeAction(Swipe.FAST, startLocation,
                    endLocation, Press.FINGER)
        }

        fun getText(matcher: Matcher<View?>?): String? {
            val stringHolder = arrayOf<String?>(null)
            onView(matcher).perform(object : ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return isAssignableFrom(TextView::class.java)
                }

                override fun getDescription(): String {
                    return "getting text from a TextView"
                }

                override fun perform(uiController: UiController?, view: View) {
                    val tv = view as TextView //Save, because of check in getConstraints()
                    stringHolder[0] = tv.text.toString()
                }
            })
            return stringHolder[0]
        }

    }

}