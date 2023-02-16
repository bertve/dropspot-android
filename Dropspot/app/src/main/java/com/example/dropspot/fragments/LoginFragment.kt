package com.example.dropspot.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.dropspot.R
import com.example.dropspot.activities.AuthActivity
import com.example.dropspot.activities.MainActivity
import com.example.dropspot.data.model.AppUser
import com.example.dropspot.databinding.FragmentLoginBinding
import com.example.dropspot.utils.Constants.AUTH_ENC_SHARED_PREF_KEY
import com.example.dropspot.utils.Constants.TOKEN_KEY
import com.example.dropspot.utils.Constants.USER_KEY
import com.example.dropspot.utils.InputLayoutTextWatcher
import com.example.dropspot.utils.MyValidationListener
import com.example.dropspot.utils.Variables
import com.example.dropspot.viewmodels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "login_fragment"
    }

    private val loginViewModel: LoginViewModel by viewModel()
    private lateinit var binding: FragmentLoginBinding
    private val validator = Validator(this)
    private val args: LoginFragmentArgs by navArgs()
    private lateinit var sharedPreferences: SharedPreferences

    // UI components
    private lateinit var buttonRegister: Button
    private lateinit var buttonLogin: Button

    @NotEmpty(messageResId = R.string.email_or_username_req)
    private lateinit var inputEmail: EditText

    @NotEmpty(messageResId = R.string.password_req)
    private lateinit var inputPassword: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.vm = loginViewModel
        binding.lifecycleOwner = this
        inputEmail = binding.inputEmail
        inputEmail.addTextChangedListener(InputLayoutTextWatcher(binding.fieldEmail))
        inputPassword = binding.inputPassword
        inputPassword.addTextChangedListener(InputLayoutTextWatcher(binding.fieldPassword))
        buttonLogin = binding.buttonLogin
        buttonRegister = binding.buttonRegister

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSharedPref()
        checkIfLoggedIn()
        setupListenersObservers()
        setupUI()
        validator.setValidationListener(
            object :
                MyValidationListener(this.requireContext(), this.requireView()) {
                override fun onValidationSucceeded() {
                    login()
                }
            }
        )
    }

    private fun setupSharedPref() {

        val spec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
            .build()

        val masterKey = MasterKey.Builder(requireContext())
            .setKeyGenParameterSpec(spec)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            requireContext(),
            AUTH_ENC_SHARED_PREF_KEY,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun setupListenersObservers() {
        buttonLogin.setOnClickListener {
            validator.validate()
        }

        buttonRegister.setOnClickListener {
            // nav to register
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        inputPassword.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    validator.validate()
                    (this.activity as AuthActivity).hideKeyboard(this.requireView())
                }
            }
            false
        }

        loginViewModel.loginResponse.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    loginViewModel.resetResponses()
                    if (it.success) {
                        Log.i(TAG, "-------------- $it")
                        val token = it.token
                        val user = it.user
                        saveSharedPref(token, user!!)
                        startMainActivity(token, user)
                    } else {
                        showErrorMessage(it.message)
                    }
                }
            }
        )
    }

    private fun showErrorMessage(extraMessage: String) {
        Snackbar.make(
            this.requireView(),
            resources.getString(R.string.login_failed) + extraMessage,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun saveSharedPref(token: String, appUser: AppUser) {
        Log.i(TAG, "user saved to SP:$appUser")

        sharedPreferences
            .edit()
            .putString(TOKEN_KEY, token)
            .putString(USER_KEY, Gson().toJson(appUser))
            .apply()
    }

    private fun setupUI() {
        // handling successful registration
        if (args.emailOrUsername.isNotBlank() && args.password.isNotBlank()) {
            binding.inputEmail.setText(args.emailOrUsername)
            binding.inputPassword.setText(args.password)
        }
    }

    private fun checkIfLoggedIn() {
        val token = sharedPreferences.getString(TOKEN_KEY, null)
        val userString = sharedPreferences.getString(USER_KEY, null)
        Log.i(TAG, "get user from SP: $userString")

        userString?.let { uString ->
            val user: AppUser = Gson().fromJson(uString, AppUser::class.java)

            token?.let { token ->
                startMainActivity(
                    token,
                    user
                )
            }
        }
    }

    private fun startMainActivity(token: String, user: AppUser) {
        val intent = Intent(this.context, MainActivity::class.java)
        intent.putExtra(TOKEN_KEY, token)
        intent.putExtra(USER_KEY, user)

        startActivity(intent)
        this.activity!!.finish()
    }

    private fun login() {
        if (Variables.isNetworkConnected.value!!) {
            loginViewModel.login(
                inputEmail.text.toString().trim(),
                inputPassword.text.toString().trim()
            )
        } else {
            showErrorMessage(resources.getString(R.string.no_connection))
        }
    }
}
