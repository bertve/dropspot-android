package com.example.dropspot.utils

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.mobsandgeeks.saripaar.Rule
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator

abstract class MyValidationListener(private val context: Context, private val view: View) :
    Validator.ValidationListener {

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        for (error: ValidationError in errors!!.iterator()) {
            val v: View = error.view.parent.parent as TextInputLayout

            val Errors: List<*> = error.failedRules

            val rule = Errors[0]

            if (rule is Rule<*>) {
                val message: String = rule.getMessage(context)

                if (v is TextInputLayout) {
                    v.error = message
                } else {
                    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
