package com.example.automation

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.automation.databinding.ActivityMainBinding
import com.example.automation.ui.AppViewModelFactory
import com.example.automation.ui.ThemeViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val themeViewModel: ThemeViewModel by viewModels { AppViewModelFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeViewModel.themeMode.observe(this) { mode ->
            if (AppCompatDelegate.getDefaultNightMode() != mode) {
                AppCompatDelegate.setDefaultNightMode(mode)
            }
            updateSystemBars(mode)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val controller = navHost.navController
        binding.bottomNav.setupWithNavController(controller)
        NavigationUI.setupWithNavController(binding.navRail, controller)

        onBackPressedDispatcher.addCallback(this) {
            when (controller.currentDestination?.id) {
                R.id.learningListFragment -> {
                    val returnedToDashboard = controller.popBackStack(R.id.dashboardFragment, false)
                    if (!returnedToDashboard) {
                        selectDestination(R.id.dashboardFragment)
                    }
                }
                else -> {
                    if (!controller.popBackStack()) {
                        finish()
                    }
                }
            }
        }

    }

    private fun updateSystemBars(mode: Int) {
        val isDark = when (mode) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
    }

    private fun selectDestination(destinationId: Int) {
        if (binding.bottomNav.visibility == View.VISIBLE) {
            binding.bottomNav.selectedItemId = destinationId
        }
        if (binding.navRail.visibility == View.VISIBLE) {
            binding.navRail.selectedItemId = destinationId
        }
    }
}
