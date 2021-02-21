package com.dev_vlad.car_v

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dev_vlad.car_v.databinding.ActivityMainBinding
import com.dev_vlad.car_v.util.MyLogger

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val TAG = MainActivity::class.java.simpleName

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
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.welcomeFragment
            )
        )
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

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


    //toolbar handle back navigation
    override fun onSupportNavigateUp(): Boolean {
        MyLogger.logThis(TAG, "onSupportNavigateUp()", "called")
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}