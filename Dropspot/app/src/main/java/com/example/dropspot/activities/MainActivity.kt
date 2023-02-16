package com.example.dropspot.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.dropspot.R
import com.example.dropspot.databinding.ActivityMainBinding
import com.example.dropspot.fragments.HomeFragmentDirections
import com.example.dropspot.network.AuthInterceptor
import com.example.dropspot.utils.Constants.AUTH_ENC_SHARED_PREF_KEY
import com.example.dropspot.utils.Constants.TOKEN_KEY
import com.example.dropspot.utils.Constants.USER_KEY
import com.example.dropspot.utils.Variables
import com.example.dropspot.viewmodels.UserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var sessionExpiredDialog: AlertDialog? = null
    private val userViewModel: UserViewModel by viewModel()
    lateinit var binding: ActivityMainBinding

    // nav
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    companion object {
        private const val TAG = "main_activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        setupLoggedInUser()
        setupNav()
        setupObservers()
    }

    private fun setupLoggedInUser() {
        userViewModel.setSessionToken(this.intent.getStringExtra(TOKEN_KEY)!!)
        userViewModel.setUser(this.intent.getParcelableExtra(USER_KEY)!!)
    }

    private fun setupObservers() {
        // update side nav username
        userViewModel.currentUser.observe(
            this,
            Observer {
                if (it != null) {
                    Log.i(TAG, "fetched user: $it")
                    binding.loggedInUser = it
                }
            }
        )

        userViewModel.isSessionExpired.observe(
            this,
            Observer {
                if (it) showSessionExpiredAndLogout()
            }
        )

        // handles connection response
        Variables.isNetworkConnected.observe(
            this,
            Observer {
                if (it) {
                    if (userViewModel.currentUser.value == null) {
                        userViewModel.fetchUser()
                    }
                    binding.animNoConnection.visibility = View.INVISIBLE
                    binding.animNoConnection.pauseAnimation()
                } else {
                    binding.animNoConnection.playAnimation()
                    binding.animNoConnection.visibility = View.VISIBLE
                    binding.animNoConnection.bringToFront()
                }
            }
        )
    }

    private fun setupNav() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.navigation)
        drawerLayout = binding.drawerLayout
        val toolbar = binding.toolbar
        val navView = binding.navView
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        // prevent nav gesture if not on start destination
        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, _ ->
            if (nd.id == nc.graph.startDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        // set this activity as the NavigationView.OnNavigationItemSelectedListener for navView
        navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                logout()
            }
            R.id.meFragment -> {
                if (userViewModel.currentUser.value != null) {
                    navController.navigate(
                        HomeFragmentDirections.actionHomeFragmentToMeFragment(
                            userViewModel.currentUser.value!!
                        )
                    )
                    // no toolbar elevation
                    binding.toolbarLayout.elevation = 0F
                } else {
                    Snackbar
                        .make(
                            binding.root,
                            getString(R.string.user_not_loaded),
                            Snackbar.LENGTH_SHORT
                        )
                        .show()
                }
            }
            else -> {

                // sets toolbar elevation to default
                val defaultDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    4.toFloat(),
                    this.resources.displayMetrics
                )
                binding.toolbarLayout.elevation = defaultDp
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showSessionExpiredAndLogout() {
        sessionExpiredDialog = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.alert_dialog_session_expired_title))
            .setMessage(resources.getString(R.string.alert_dialog_session_expired_message))
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                logout()
            }
            .setCancelable(false)
            .show()
    }

    private fun logout() {
        val spec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
            .build()

        val masterKey = MasterKey.Builder(this)
            .setKeyGenParameterSpec(spec)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            this,
            AUTH_ENC_SHARED_PREF_KEY,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ).edit()

        sharedPreferences.remove(TOKEN_KEY)
        sharedPreferences.remove(USER_KEY)
        sharedPreferences.apply()

        AuthInterceptor.clearSessionToken()

        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionExpiredDialog?.dismiss()
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
