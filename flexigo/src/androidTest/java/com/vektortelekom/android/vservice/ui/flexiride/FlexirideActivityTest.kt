package com.vektortelekom.android.vservice.ui.flexiride

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.GrantPermissionRule
import com.vektortelekom.android.vservice.R
import com.vektortelekom.android.vservice.TestUtils
import com.vektortelekom.android.vservice.ui.home.adapter.DashboardAdapter
import org.hamcrest.CoreMatchers.not
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class FlexirideActivityTest {

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        TestUtils.login()
    }

    @Test
    fun testFlexiride() {

        // Click to Flexiride
        onView(withId(R.id.recycler_view_dashboard)).perform(RecyclerViewActions.actionOnItemAtPosition<DashboardAdapter.DashboardViewHolder>(3, ViewActions.click()))

        Thread.sleep(1000)

        // Check Flexiride Action list visibility (request Flexiride, list Flexiride)
        onView(withId(R.id.card_view_rent_a_car)).check(matches(isDisplayed()))

        // Click request Flexiride
        onView(withId(R.id.card_view_rent_a_car)).perform(ViewActions.click())

        Thread.sleep(5000)

        // from text must be visible
        onView(withId(R.id.text_view_from_info)).check(matches(isDisplayed()))
        // to text must be invisible
        onView(withId(R.id.text_view_to_info)).check(matches(not(isDisplayed())))



        val initialText = TestUtils.getText(withId(R.id.text_view_from))

        //swipe map to left x2
        onView(withId(R.id.map_view)).perform(TestUtils.swipe(GeneralLocation.CENTER_LEFT, GeneralLocation.CENTER_RIGHT))

        Thread.sleep(2000)

        val swipeLeftText = TestUtils.getText(withId(R.id.text_view_from))
        if(initialText.isNullOrEmpty().not()) {
            assertNotEquals("Adres bilgisi değişmiyor", initialText, swipeLeftText)
        }

        //swipe map to right
        onView(withId(R.id.map_view)).perform(TestUtils.swipe(GeneralLocation.CENTER, GeneralLocation.CENTER_LEFT))

        Thread.sleep(1000)

        //swipe map to right
        onView(withId(R.id.map_view)).perform(TestUtils.swipe(GeneralLocation.CENTER, GeneralLocation.CENTER_LEFT))

        Thread.sleep(1000)

        val swipeInitialAgain = TestUtils.getText(withId(R.id.text_view_from))

        if(initialText.isNullOrEmpty().not() && swipeInitialAgain.isNullOrEmpty().not()) {
            assertEquals("Adres bilgisi eşleşmiyor", initialText, swipeInitialAgain)
        }

        onView(withId(R.id.button_submit)).check(matches(isDisplayed())).perform(click())

        // to text must be visible
        onView(withId(R.id.text_view_to_info)).check(matches(isDisplayed()))

        val toBeforeSwipe = TestUtils.getText(withId(R.id.text_view_to))

        assertEquals("Nereye boş değil", toBeforeSwipe, "")

        // swipe again
        onView(withId(R.id.map_view)).perform(TestUtils.swipe(GeneralLocation.CENTER, GeneralLocation.BOTTOM_CENTER))

        Thread.sleep(1000)

        val fromAfterSwipeForTo = TestUtils.getText(withId(R.id.text_view_from))
        val toAfterSwipe = TestUtils.getText(withId(R.id.text_view_to))

        if(fromAfterSwipeForTo.isNullOrEmpty().not() && swipeInitialAgain.isNullOrEmpty().not()) {
            assertEquals("Nereden adresi değişmemeliydi", fromAfterSwipeForTo, swipeInitialAgain)
        }


        Thread.sleep(3000)






    }

}