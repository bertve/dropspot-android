package com.example.dropspot.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dropspot.R
import com.example.dropspot.data.model.ParkCategory
import com.example.dropspot.databinding.EditSpotDetailFragmentBinding
import com.example.dropspot.utils.InputLayoutTextWatcher
import com.example.dropspot.utils.MyValidationListener
import com.example.dropspot.utils.Variables
import com.example.dropspot.viewmodels.EditSpotDetailViewModel
import com.google.android.material.snackbar.Snackbar
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Length
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Order
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditSpotDetailFragment : Fragment() {

    private val editSpotDetailViewModel: EditSpotDetailViewModel by viewModel()
    private val args: EditSpotDetailFragmentArgs by navArgs()
    private lateinit var binding: EditSpotDetailFragmentBinding

    // validation
    private val validator = Validator(this)

    // street val
    @Order(1)
    @NotEmpty(messageResId = R.string.spot_name_req)
    @Length(max = 25, messageResId = R.string.spot_name_max_length)
    private lateinit var inputName: EditText

    // park val
    @Order(2)
    @NotEmpty(messageResId = R.string.street_req)
    private lateinit var inputStreet: EditText

    @Order(3)
    @NotEmpty(messageResId = R.string.house_number_req)
    private lateinit var inputNumber: EditText

    @Order(4)
    @NotEmpty(messageResId = R.string.city_req)
    private lateinit var inputCity: EditText

    @Order(5)
    @NotEmpty(messageResId = R.string.postal_code_req)
    private lateinit var inputPostal: EditText

    @Order(6)
    @NotEmpty(messageResId = R.string.state_req)
    private lateinit var inputState: EditText

    @Order(7)
    @NotEmpty(messageResId = R.string.country_req)
    private lateinit var inputCountry: EditText

    companion object {
        private const val TAG = "edit_spot_detail_frag"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditSpotDetailFragmentBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.vm = editSpotDetailViewModel

        // set spotDetail
        val spotDetail = args.spotDetail
        editSpotDetailViewModel.spotDetail = spotDetail
        binding.spotDetail = spotDetail

        // bind fields
        inputName = binding.inputName
        inputStreet = binding.inputStreet
        inputNumber = binding.inputHouseNumber
        inputCity = binding.inputCity
        inputPostal = binding.inputPostalCode
        inputState = binding.inputState
        inputCountry = binding.inputCountry
        inputName.addTextChangedListener(InputLayoutTextWatcher(binding.layoutName))
        inputStreet.addTextChangedListener(InputLayoutTextWatcher(binding.layoutStreet))
        inputNumber.addTextChangedListener(InputLayoutTextWatcher(binding.layoutHouseNumber))
        inputCity.addTextChangedListener(InputLayoutTextWatcher(binding.layoutCity))
        inputPostal.addTextChangedListener(InputLayoutTextWatcher(binding.layoutPostalCode))
        inputState.addTextChangedListener(InputLayoutTextWatcher(binding.layoutState))
        inputCountry.addTextChangedListener(InputLayoutTextWatcher(binding.layoutCountry))
        binding.dropdownParkCategory.addTextChangedListener(InputLayoutTextWatcher(binding.layoutParkCategory))

        // set cat dropdown
        val parkCats = mutableListOf<String>()
        ParkCategory.values().forEach { parkCats.add(it.toString()) }

        val dropdownAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.simple_dropdown_item,
            parkCats
        )

        binding.dropdownParkCategory.setAdapter(dropdownAdapter)

        binding.dropdownParkCategory.setText(spotDetail.parkCategory.toString(), false)

        editSpotDetailViewModel.updateSuccess.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    if (it.success) findNavController().navigateUp()
                    else Snackbar.make(
                        requireView(),
                        resources.getString(R.string.update_failed) +
                            it.message,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // validation setup
        validator.validationMode = Validator.Mode.IMMEDIATE
        validator.setValidationListener(
            object :
                MyValidationListener(this.requireContext(), this.requireView()) {
                override fun onValidationSucceeded() {
                    updateSpot()
                }
            }
        )

        binding.btnUpdate.setOnClickListener {
            editSpotDetailViewModel.spotDetail?.let {
                if (!editSpotDetailViewModel.spotDetail!!.isPark()) {
                    validator.validateBefore(inputStreet)
                } else {
                    validator.validate()
                }
            }
        }
    }

    private fun updateSpot() {
        if (Variables.isNetworkConnected.value!!) {
            if (editSpotDetailViewModel.spotDetail!!.isPark()) {
                editSpotDetailViewModel.updateParkSpot(
                    inputName.text.toString().trim(), inputStreet.text.toString().trim(), inputNumber.text.toString().trim(), inputCity.text.toString().trim(), inputPostal.text.toString().trim(), inputState.text.toString().trim(), inputCountry.text.toString().trim(), binding.dropdownParkCategory.text.toString().trim(), binding.sliderFee.value.toDouble()
                )
            } else {
                editSpotDetailViewModel.updateStreetSpot(inputName.text.toString().trim())
            }
        } else {
            Snackbar.make(
                requireView(),
                resources.getString(R.string.update_failed) +
                    resources.getString(R.string.no_connection),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}
