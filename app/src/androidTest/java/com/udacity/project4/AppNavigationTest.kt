package com.udacity.project4

//import androidx.test.espresso.contrib.DrawerMatchers.isClosed


import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.get
import org.koin.test.junit5.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest : AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
//    private lateinit var fakeFBUser: MutableLiveData<FirebaseUser?>

    @Before
    fun init() {
        TestingUtils.initKoin("module3")
        repository = get()

    }

    @After
    fun tearDown() {
        TestingUtils.releaseKoin()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun logout_navigatesTo_LoginActivity() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)


        onView(withId(R.id.logout)).perform(click())

        onView(withId(R.id.login_button))
            .check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun NavigateToLocationSelectorThenBackWithUpButton() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        //start on the list fragment
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))

        //go to save reminder fragment
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))


        //go the select location fragment
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.btnSelect)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSelect)).check(matches(CoreMatchers.not(isEnabled())))
        onView(withId(R.id.map)).check(matches(isDisplayed()))

        // navigate up
        onView(withContentDescription("Navigate up")).perform(click())

        //returns to save reminder fragment
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))

        //navigate up
        onView(withContentDescription("Navigate up")).perform(click())

        //return to list fragment
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))


        activityScenario.close()

    }

    @Test
    fun NavigateToLocationSelectorThenBackWithBackButton() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        //start on the list fragment
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))

        //go to save reminder fragment
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))


        //go the select location fragment
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.btnSelect)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSelect)).check(matches(CoreMatchers.not(isEnabled())))
        onView(withId(R.id.map)).check(matches(isDisplayed()))

        // press back button
        pressBack()

        //returns to save reminder fragment
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))

        //press back
        pressBack()

        //return to list fragment
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))

        //press back
        pressBackUnconditionally()

        // activity closed and app is killed
        assertThat(activityScenario.state, `is`(Lifecycle.State.DESTROYED))

        activityScenario.close()

    }
}