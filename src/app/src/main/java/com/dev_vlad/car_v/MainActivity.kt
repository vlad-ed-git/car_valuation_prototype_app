package com.dev_vlad.car_v

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dev_vlad.car_v.databinding.ActivityMainBinding
import com.dev_vlad.car_v.util.MyLogger
import com.dev_vlad.car_v.view_models.activity_vm.MainActViewModel
import com.dev_vlad.car_v.view_models.activity_vm.MainActViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val TAG = MainActivity::class.java.simpleName

    private val mainActViewModel: MainActViewModel by viewModels {
        val carVApp = (application as CarVApp)
        MainActViewModelFactory(carVApp.userRepo, carVApp.carRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        //specify home fragments
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.sellersHomeFragment,
                R.id.dealersHomeFragment,
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.welcomeFragment
            )
        )
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)
        observeAuthState()

        //hide and show menus depending on fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.splashFragment,
                R.id.loginFragment,
                R.id.welcomeFragment -> {
                    binding.toolbar.isVisible = false
                    binding.bottomNav.isVisible = false
                }

                R.id.sellersHomeFragment -> {
                    binding.toolbar.title = getString(R.string.my_cars_txt)
                    binding.toolbar.isVisible = true
                    binding.bottomNav.isVisible = true
                }

                R.id.receivedOffersFragment -> {
                    binding.toolbar.title = getString(R.string.fragment_received_offers_lbl)
                    binding.toolbar.isVisible = true
                    binding.bottomNav.isVisible = true
                }

                R.id.dealersHomeFragment -> {
                    binding.toolbar.title = getString(R.string.cars_txt)
                    binding.toolbar.isVisible = true
                    binding.bottomNav.isVisible = true
                }

                R.id.addOrEditCarFragment -> {
                    binding.toolbar.title = getString(R.string.my_car_txt)
                    binding.toolbar.isVisible = true
                    binding.bottomNav.isVisible = false
                }

                R.id.myCarDetailsFragment -> {
                    binding.toolbar.title = getString(R.string.my_car_txt)
                    binding.toolbar.isVisible = true
                    binding.bottomNav.isVisible = false
                }

                R.id.carDetailsFragment -> {
                    binding.toolbar.title = getString(R.string.fragment_dealer_car_details_lbl)
                    binding.toolbar.isVisible = true
                    binding.bottomNav.isVisible = false
                }

                R.id.chatFragment -> {
                    binding.toolbar.title = getString(R.string.fragment_chat_lbl)
                    binding.toolbar.isVisible = true
                    binding.bottomNav.isVisible = false
                }

                R.id.addCarImagesFragment -> {
                    binding.toolbar.title = getString(R.string.my_car_photos_txt)
                    binding.toolbar.isVisible = true
                    binding.bottomNav.isVisible = false
                }

                else -> {
                    binding.toolbar.isVisible = true
                    binding.bottomNav.isVisible = true
                }

            }
        }

    }

    private fun observeAuthState() {
        mainActViewModel.userState.observe(this, Observer {
            //set bottom menu
            if (!it.isNullOrEmpty()) {
                if (it[0].isSeller) {
                    binding.bottomNav.menu.clear() //clear old inflated items.
                    binding.bottomNav.inflateMenu(R.menu.sellers_bottom_nav)
                } else if (it[0].isDealer) {
                    binding.bottomNav.menu.clear() //clear old inflated items.
                    binding.bottomNav.inflateMenu(R.menu.dealers_bottom_nav)

                }
            }
        })
    }


    /**TODO real time listeners? private fun observeDealersData(){
    mainActViewModel.listenToCarUpdates()
    mainActViewModel.getCarUpdates().observe(this, Observer {
    updates ->
    if (updates != null) {
    when (updates.state) {
    DataState.DELETE -> {
    mainActViewModel.deleteCar(updates.car)
    }
    DataState.UPDATE -> {
    mainActViewModel.updateCar(updates.car)
    }
    DataState.ADD -> {
    mainActViewModel.addCar(updates.car)
    }
    }
    }
    })

    }*************/


    //toolbar handle back navigation
    override fun onSupportNavigateUp(): Boolean {
        MyLogger.logThis(TAG, "onSupportNavigateUp()", "called")
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}