package com.example.dropspot.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dropspot.R
import com.example.dropspot.activities.AuthActivity
import com.example.dropspot.databinding.FragmentRegisterBinding
import com.example.dropspot.utils.InputLayoutTextWatcher
import com.example.dropspot.utils.MyValidationListener
import com.example.dropspot.utils.Variables
import com.example.dropspot.viewmodels.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.Length
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Password
import com.mobsandgeeks.saripaar.annotation.Pattern
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment() {

    private val registerViewModel: RegisterViewModel by viewModel()
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var buttonRegister: Button
    private val validator = Validator(this)

    @NotEmpty(messageResId = R.string.first_name_req)
    @Length(max = 50, messageResId = R.string.first_name_max_length)
    private lateinit var inputFirstName: EditText

    @NotEmpty(messageResId = R.string.last_name_req)
    @Length(max = 50, messageResId = R.string.last_name_max_length)
    private lateinit var inputLastName: EditText

    @NotEmpty(messageResId = R.string.user_name_req)
    @Length(min = 5, max = 25, messageResId = R.string.user_name_length)
    @Pattern(regex = "^[A-Za-z]\\w{4,24}$", messageResId = R.string.user_name_wrong_format)
    private lateinit var inputUsername: EditText

    @Email(messageResId = R.string.email_wrong_format)
    @NotEmpty(messageResId = R.string.email_req)
    @Length(max = 100, messageResId = R.string.email_max_length)
    private lateinit var inputEmail: EditText

    @NotEmpty(messageResId = R.string.password_req)
    @Password(min = 6, messageResId = R.string.password_min_length)
    private lateinit var inputPassword: EditText

    @NotEmpty(messageResId = R.string.password_confirmation_req)
    @ConfirmPassword
    private lateinit var inputPasswordConfirm: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.vm = registerViewModel
        binding.lifecycleOwner = this
        inputFirstName = binding.inputFirstname
        inputFirstName.addTextChangedListener(InputLayoutTextWatcher(binding.fieldFirstname))
        inputLastName = binding.inputLastname
        inputLastName.addTextChangedListener(InputLayoutTextWatcher(binding.fieldLastname))
        inputEmail = binding.inputEmail
        inputEmail.addTextChangedListener(InputLayoutTextWatcher(binding.fieldEmail))
        inputUsername = binding.inputUsername
        inputUsername.addTextChangedListener(InputLayoutTextWatcher(binding.fieldUsername))
        inputPassword = binding.inputPassword
        inputPassword.addTextChangedListener(InputLayoutTextWatcher(binding.fieldPassword))
        inputPasswordConfirm = binding.inputPasswordConfirm
        inputPasswordConfirm.addTextChangedListener(InputLayoutTextWatcher(binding.fieldPasswordConfirm))
        buttonRegister = binding.buttonRegister

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        setupListenersObservers()
        validator.setValidationListener(
            object :
                MyValidationListener(this.requireContext(), this.requireView()) {
                override fun onValidationSucceeded() {
                    register()
                }
            }
        )
    }

    private fun setupListenersObservers() {
        buttonRegister.setOnClickListener {
            validator.validate()
        }

        inputPasswordConfirm.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    validator.validate()
                    (this.activity as AuthActivity).hideKeyboard(this.requireView())
                }
            }
            false
        }

        registerViewModel.registerResponse.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    registerViewModel.resetResponses()
                    if (it.success) navigateToLogin() else showErrorMessage(it.message)
                }
            }
        )
    }

    private fun showErrorMessage(extraMessage: String) {

        Snackbar.make(
            this.requireView(),
            resources.getString(R.string.register_failed) +
                extraMessage,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun navigateToLogin() {

        findNavController().navigate(
            RegisterFragmentDirections.actionRegisterFragmentToLoginFragment(
                inputUsername.text.toString().trim(),
                inputPassword.text.toString().trim()
            )
        )
    }

    private fun register() {

        if (Variables.isNetworkConnected.value!!) {
            registerViewModel.register(
                inputFirstName.text.toString().trim(),
                inputLastName.text.toString().trim(),
                inputUsername.text.toString().trim(),
                inputEmail.text.toString().trim(),
                inputPassword.text.toString().trim()
            )
        } else {
            showErrorMessage(resources.getString(R.string.no_connection))
        }
    }
}
