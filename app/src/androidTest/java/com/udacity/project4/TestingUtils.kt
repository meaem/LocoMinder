package com.udacity.project4

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.authentication.FakeFirebaseUser
import com.udacity.project4.authentication.FakeFirebaseUserLiveData
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersFakeLocalRepository
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

object TestingUtils {

    lateinit var saveReminderViewModel: SaveReminderViewModel
    lateinit var authenticationViewModel: AuthenticationViewModel
    val module1 = module {

        viewModel {
            RemindersListViewModel(
                app = get(),
                dataSource = get()
            )
        }



        single<ReminderDataSource>() { RemindersFakeLocalRepository(mutableListOf()) as ReminderDataSource }


    }


    val module2 = module {

        viewModel() {
            RemindersListViewModel(
                app = get(),
                dataSource = get()
            )
        }

        viewModel {
            saveReminderViewModel = SaveReminderViewModel(get(), get())
            saveReminderViewModel
        }
        viewModel {
//                authenticationViewModel = AuthenticationViewModel(
            AuthenticationViewModel(
                get<MutableLiveData<FirebaseUser?>>() as LiveData<FirebaseUser?>, get()
            )

        }

        single<MutableLiveData<FirebaseUser?>> { FakeFirebaseUserLiveData(FakeFirebaseUser()) }

        single { LocalDB.createRemindersDao(get()) }

        single<ReminderDataSource> { RemindersLocalRepository(get(), Dispatchers.Main) }
//            single { RemindersListViewModel(get(),get()) }

    }


    val module3 = module {

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
            SaveReminderViewModel(get(), get())

        }
        single { LocalDB.createRemindersDao(get()) }

        single<ReminderDataSource> { RemindersLocalRepository(get(), Dispatchers.Main) }
//            single { RemindersListViewModel(get(),get()) }

    }


    val moduleMap = mapOf(
        "module1" to module1,
        "module2" to module2,
        "module3" to module3
    )

    init {


    }

    fun initKoin(moduleName: String) {

        releaseKoin()





        startKoin {


            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(moduleMap.get(moduleName)!!))
        }

//        ,myModule2

    }

    fun releaseKoin() {
        stopKoin()
    }
}