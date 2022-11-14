package com.udacity.project4.locationreminders

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.R
import com.udacity.project4.TestingUtils
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.utils.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.get
import org.koin.test.junit5.AutoCloseKoinTest
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var fakeFBUser: MutableLiveData<FirebaseUser?>

    private lateinit var decorView: View


    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    @Before
    fun init() {


        TestingUtils.initKoin("module2")



        repository = get()
        fakeFBUser = get()
    }

    @After
    fun tearDown() {
        TestingUtils.releaseKoin()
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun remiderList_has_LogoutMenuItem() {

        // Start up Remider List activity.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.logout))
            .check(matches(isDisplayed()))

        activityScenario.close()

    }


    @Test
    fun notLoggedInUser_CanNotAccessRemindersActivity() {
        fakeFBUser.postValue(null)
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        assertThat(activityScenario.state, `is`(DESTROYED))
        activityScenario.close()
    }

    @Test
    fun clickAddFab_NavigatesTo_SaveReminderFragment() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        activityScenario.close()

    }

    @Test
    fun clickSaveRemider_WithEmptyReminderTitle_ShowSnakbar() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()

    }

    @Test
    fun clickSelectLocation_NavigatesTo_SelectLocationFragment() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        val context: Context = ApplicationProvider.getApplicationContext()

        activityScenario.onActivity {
            decorView = it.getWindow().getDecorView();
        }


        //start on the list fragment
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())


        //go to save reminder fragment
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(isEnabled()))
        onView(withId(R.id.selectLocation)).perform(click())


        val toastStr = context.getString(
            R.string.select_location_educational_msg,
            context.getString(R.string.btn_select_location).uppercase()
        )

        //show Toast
        onView(withText(toastStr))
            .inRoot(withDecorView(not(decorView)))
            .check(matches(isDisplayed()))



        onView(withId(R.id.btnSelect)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSelect)).check(matches(not(isEnabled())))

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun AddCorrectReminder() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        val context: Context = ApplicationProvider.getApplicationContext()

        activityScenario.onActivity {
            decorView = it.getWindow().getDecorView();
        }


        //start on the list fragment
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())


        //go to save reminder fragment
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(isEnabled()))

        onView(withId(R.id.selectLocation)).perform(click())


        val toastStr = context.getString(
            R.string.select_location_educational_msg,
            context.getString(R.string.btn_select_location).uppercase()
        )

        //show Toast
        onView(withText(toastStr))
            .inRoot(withDecorView(not(decorView)))
            .check(matches(isDisplayed()))



        onView(withId(R.id.btnSelect)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSelect)).check(matches(not(isEnabled())))

        onView(withId(R.id.map)).check(matches(isDisplayed()))

        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.btnSelect)).check(matches(isEnabled()))

        onView(withId(R.id.btnSelect)).perform(click())

        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        val nn = Random.nextInt()
        val title = "Test Title $nn"
        onView(withId(R.id.reminderTitle)).perform(typeText(title))
        onView(withId(R.id.reminderDescription)).perform(typeText("Test Description $nn"))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.saveReminder))
            .perform(click())

        val toastStr2 = context.getString(R.string.reminder_saved)
        //go the select location fragment
        onView(withText(toastStr2))
            .inRoot(withDecorView(not(decorView)))
            .check(matches(isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText(title))))

        activityScenario.close()
    }
}