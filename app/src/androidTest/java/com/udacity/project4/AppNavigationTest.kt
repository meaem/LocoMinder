package com.udacity.project4

//import androidx.test.espresso.contrib.DrawerMatchers.isClosed


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.authentication.FakeFirebaseUser
import com.udacity.project4.authentication.FakeFirebaseUserLiveData
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.koin.test.junit5.AutoCloseKoinTest

@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest : AutoCloseKoinTest() {
    //
    private lateinit var repository: ReminderDataSource


    private lateinit var fakeFBUser: MutableLiveData<FirebaseUser?>
    private lateinit var authenticationViewModel: AuthenticationViewModel
    private lateinit var saveReminderViewModel: SaveReminderViewModel

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


            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }

        repository = get()
        fakeFBUser = get()

    }

    @After
    fun tearDown() {
        stopKoin()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun logout_navigatesTo_LoginActivity() = runTest {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        authenticationViewModel.setLogoutLogic {
            fakeFBUser.postValue(null)
        }

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