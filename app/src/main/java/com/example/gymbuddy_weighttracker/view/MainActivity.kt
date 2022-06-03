package com.example.gymbuddy_weighttracker.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.gymbuddy_weighttracker.R
import com.example.gymbuddy_weighttracker.databinding.ActivityMainBinding
import com.example.gymbuddy_weighttracker.helpers.Helpers
import com.example.gymbuddy_weighttracker.helpers.Helpers.animateAlpha
import com.example.gymbuddy_weighttracker.helpers.Helpers.toggleVisibility
import com.example.gymbuddy_weighttracker.helpers.Utils
import com.example.gymbuddy_weighttracker.viewModel.MainViewModel
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var adLoader: AdLoader? = null
    private var shownFragment = 1
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val mainActivityTag = "MainActivity"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, R.string.pressAgain, Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymBuddyWeightTracker)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.getInstance(this).lastAdShown = 0
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        Helpers.showRatingUserInterface(this)
        Helpers.setupActionBar(resources.getString(R.string.gbWeightTracker), "", supportActionBar, this)
        MobileAds.initialize(this) { initializationStatus: InitializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Log.d(
                    "GB", String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status!!.description, status.latency
                    )
                )
            }
            adLoader = Helpers.handleNativeAds(binding.mainAdTemplate, this, Helpers.AD_ID_MAIN_NATIVE, null)
        }

        val navController = Navigation.findNavController(this, R.id.fragment)
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.nav_default_enter_anim)
            .setExitAnim(R.anim.nav_default_exit_anim)
            .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
            .setPopUpTo(navController.graph.startDestinationId, false)
            .build()

        val layoutParams = binding.fragmentParent.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.setMargins(0, 0, 0, binding.bottomAppBar.height)
        binding.addValueFAB.apply {
            setOnClickListener {
                viewModel.setAddValueClicked(true)
            }
            setOnLongClickListener {
                viewModel.setAddValueLongClicked(true)
                true
            }
        }
        viewModel.inputOngoing.observe(this) { inputOngoing ->
            binding.bottomNav.menu.apply {
                getItem(0).isEnabled = !inputOngoing
                getItem(2).isEnabled = !inputOngoing
            }
            when (inputOngoing) {
                true -> {
                    binding.addValueFAB.animateAlpha(0f)
                    binding.dimmer.animateAlpha(0.7f)}
                else -> {
                    binding.addValueFAB.animateAlpha(1f)
                    binding.dimmer.animateAlpha(0f)}
                    }
        }
        viewModel.hideFAB.observe(this) { hideFAB ->
            binding.addValueFAB.toggleVisibility(!hideFAB)
        }
        binding.fragmentParent.layoutParams = layoutParams
        binding.bottomNav.apply {
            labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_SELECTED
            background = null
            menu.getItem(1).isEnabled = false
            setOnItemSelectedListener { item: MenuItem ->
                binding.addValueFAB.toggleVisibility(true)
                when (item.itemId) {
                    R.id.weightFragment -> {
                        if (shownFragment != 1 && !viewModel.inputOngoing.value!!) {
                            navController.navigate(R.id.weightFragment, null, options)
                            shownFragment = 1
                            Helpers.handleNativeAds(binding.mainAdTemplate, this@MainActivity, Helpers.AD_ID_MAIN_NATIVE, adLoader)
                        }
                        return@setOnItemSelectedListener true
                    }
                    R.id.measurementsFragment -> {
                        if (shownFragment != 2 && !viewModel.inputOngoing.value!!) {
                            navController.navigate(R.id.measurementsFragment, null, options)
                            shownFragment = 2
                            Helpers.handleNativeAds(binding.mainAdTemplate, this@MainActivity, Helpers.AD_ID_MAIN_NATIVE, adLoader)
                        }
                        return@setOnItemSelectedListener true
                    }
                    else -> return@setOnItemSelectedListener false
                }
            }
        }


    }
}
