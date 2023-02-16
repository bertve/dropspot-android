package com.example.dropspot.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dropspot.R
import com.example.dropspot.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)

        // actionbar provided with return action
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_auth) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.auth_nav)
        binding.toolbar.setupWithNavController(navController)
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
