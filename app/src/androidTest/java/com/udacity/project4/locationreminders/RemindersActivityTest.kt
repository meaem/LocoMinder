package com.udacity.project4.locationreminders

import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.authentication.FakeFirebaseUser
import com.udacity.project4.authentication.FakeFirebaseUserLiveData
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.monitorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.koin.test.junit5.AutoCloseKoinTest

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var fakeFBUser: MutableLiveData<FirebaseUser?>
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    @Before
    fun init() {
        stopKoin()
//        TestingUtils.initKoin()

        val myModule = module {

            viewModel {
                RemindersListViewModel(
                    app = get(),
                    dataSource = get() as ReminderDataSource
                )
            }

            single<MutableLiveData<FirebaseUser?>> { FakeFirebaseUserLiveData(FakeFirebaseUser()) }
            viewModel {
                authenticationViewModel = AuthenticationViewModel(
                    get<MutableLiveData<FirebaseUser?>>() as LiveData<FirebaseUser?>, get()
                )
                authenticationViewModel
            }
            viewModel {
                saveReminderViewModel = SaveReminderViewModel(get(), get())
                saveReminderViewModel
            }
            single { LocalDB.createRemindersDao(get()) }

            single<ReminderDataSource> { RemindersLocalRepository(get(), Dispatchers.Main) }
//            single { RemindersListViewModel(get(),get()) }

        }

        startKoin {


            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(myModule))
        }

        repository = get()
        fakeFBUser = get()

    }

    @After
    fun tearDown() {
        stopKoin()
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
    fun clickSelectLocation_NavigatesTo_SelectLocationFragment() = runTest {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)


        //start on the list fragment
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        //go to save reminder fragment
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).perform(click())


        //go the select location fragment
        onView(withId(R.id.btnSelect)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSelect)).check(matches(not(isEnabled())))
        onView(withId(R.id.map)).check(matches(isDisplayed()))
//        assertThat(saveReminderViewModel.selectedPOI.value, `is`(nullValue()))
//        assertThat(saveReminderViewModel.mapReady.value, `is`(true))
//
        onView(withId(R.id.map)).perform(longClick())
//        assertThat(saveReminderViewModel.selectedPOI.value, `is`(not(nullValue())))
        onView(withId(R.id.btnSelect)).check(matches(isEnabled()))

//        Thread.sleep(5000)
        onView(withId(R.id.btnSelect)).perform(click())
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, `is`(not(nullValue())))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, `is`(not("")))
        assertThat(saveReminderViewModel.selectedPOI.value, `is`(not(nullValue())))


//        onView(withId(R.id.selectedLocation)).check(matches(isDisplayed()))


//        assertThat(saveReminderViewModel.test, `is`("1"))
//        assertThat(saveReminderViewModel.showErrorMessage.value, `is`("Please select a place by either click on any landmark or by long click on any desired location"))
//        assertThat(saveReminderViewModel.navigationCommand.value, `is`(NavigationCommand.Back))

//        Thread.sleep(10000)
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderTitle)).perform(typeText("Test Title"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Test Description"))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.saveReminder))
            .perform(click())


        activityScenario.close()
    }

}