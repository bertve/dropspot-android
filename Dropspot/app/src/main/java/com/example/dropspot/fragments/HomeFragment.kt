package com.example.dropspot.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.dropspot.R
import com.example.dropspot.activities.MainActivity
import com.example.dropspot.data.model.ParkCategory
import com.example.dropspot.data.model.Spot
import com.example.dropspot.databinding.HomeFragmentBinding
import com.example.dropspot.utils.InputLayoutTextWatcher
import com.example.dropspot.utils.MyValidationListener
import com.example.dropspot.utils.Utils
import com.example.dropspot.utils.Variables
import com.example.dropspot.viewmodels.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Length
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.mobsandgeeks.saripaar.annotation.Order
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class HomeFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "home_frag"
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val DRAWABLE_SPOT_MARKER = R.drawable.ic_spot_marker_filled
        private const val DRAWABLE_NEW_SPOT_MARKER = R.drawable.ic_flag_secondary
        private const val DRAWABLE_NEW_SPOT_MARKER_ON_DRAG = R.drawable.ic_flag_thick
        private const val KEY_NEW_SPOT_MARKER_COORDS = "NEW_SPOT_MARKER_COORDS"
        private const val KEY_CAMERA_POS = "CAMERA_POS"
    }

    private val viewModel: HomeViewModel by viewModel()
    private lateinit var binding: HomeFragmentBinding

    // google maps api
    private var map: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private lateinit var gcd: Geocoder
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted: Boolean = false
    private var lastKnownLocation: Location? = null
    private val defaultLocation: LatLng = LatLng(0.0, 0.0)
    private var cameraPosition: CameraPosition? = null
    private var markerCache: MutableList<Marker> = mutableListOf()

    // new spot
    private var newSpotMarker: Marker? = null
    private var newSpotLatitude: Double? = null
    private var newSpotLongitude: Double? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        // sets coords if new spot marker was already added in session
        val coords = savedInstanceState?.getDoubleArray(KEY_NEW_SPOT_MARKER_COORDS)
        if (coords != null) {
            newSpotLatitude = coords[0]
            newSpotLongitude = coords[1]
        }

        // sets position of camera if already set in session
        if (cameraPosition == null) {
            val pos = savedInstanceState?.getParcelable<CameraPosition>(KEY_CAMERA_POS)
            if (pos != null) {
                cameraPosition = pos
            }
        }

        setupUI()
        setupViewModelObservers()
        return binding.root
    }

    private fun setupUI() {

        // fab
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener {
            fab.isExpanded = true
        }

        // collapse add spot view
        binding.collapse.setOnClickListener {
            fab.isExpanded = false
        }

        // add btn
        binding.btnAdd.setOnClickListener {
            if (binding.toggleSpotSort.checkedButtonId == R.id.toggle_street) {
                Log.i(TAG, "street validation")
                validator.validateBefore(inputStreet)
            } else {
                Log.i(TAG, "park validation")
                validator.validate()
            }
        }

        // spot sort toggle
        binding.toggleSpotSort.addOnButtonCheckedListener { _, checkedId, _ ->
            when (checkedId) {
                R.id.toggle_street -> binding.groupPark.visibility = View.GONE
                R.id.toggle_park -> binding.groupPark.visibility = View.VISIBLE
            }
        }
        binding.toggleSpotSort.check(R.id.toggle_street)

        // dropdown indoor/outdoor
        val parkCats = mutableListOf<String>()
        ParkCategory.values().forEach { parkCats.add(it.toString()) }

        val dropdownAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.simple_dropdown_item,
            parkCats
        )

        binding.dropdownParkCategory.setAdapter(dropdownAdapter)

        // fee slider
        binding.sliderFee.setLabelFormatter {
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 2
            format.currency = Currency.getInstance("EUR")
            format.format(it.toDouble())
        }

        // add spot fields
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

        // flag indicator
        binding.flag.setOnClickListener {
            newSpotMarker?.let {
                removeNewSpotMarker()
            }
        }
    }

    private fun setupViewModelObservers() {

        // add spot response handling
        viewModel.addParkSpotSuccess.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    handleAddSpotResponse(it)
                }
            }
        )

        viewModel.addStreetSpotSuccess.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    handleAddSpotResponse(it)
                }
            }
        )
    }

    private fun handleAddSpotResponse(success: Boolean) {
        if (success) {
            removeNewSpotMarker()
            clearFields()
            binding.fab.isExpanded = false
        } else {
            Snackbar.make(
                requireView(),
                resources.getString(R.string.failed_to_add_spot),
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(binding.addSpotContainer)
                .show()
        }
    }

    private fun clearFields() {
        inputName.setText("")
        inputStreet.setText("")
        inputNumber.setText("")
        inputCity.setText("")
        inputPostal.setText("")
        inputState.setText("")
        inputCountry.setText("")
        binding.dropdownParkCategory.setText("")
    }

    private fun removeNewSpotMarker() {
        newSpotMarker?.let {
            newSpotMarker!!.remove()
            newSpotMarker = null
            newSpotLatitude = null
            newSpotLongitude = null

            // flag indicator filled
            binding.flag.setImageResource(R.drawable.ic_flag_24px)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // validation setup
        validator.validationMode = Validator.Mode.IMMEDIATE
        validator.setValidationListener(
            object :
                MyValidationListener(this.requireContext(), this.requireView()) {
                override fun onValidationSucceeded() {
                    addSpot()
                }
            }
        )

        // maps init
        gcd = Geocoder(context, Locale.getDefault())
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.requireContext())
        initMap()
    }

    private fun drawMarker(latitude: Double, longitude: Double, name: String, iconId: Int): Marker {
        return map!!.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title(name)
                .icon(
                    Utils.bitmapDescriptorFromVector(
                        requireContext(),
                        iconId
                    )
                )

        )
    }

    private fun addSpot() {
        (activity as MainActivity).hideKeyboard(requireView())

        if (!Variables.isNetworkConnected.value!!) {
            Snackbar.make(
                requireView(),
                resources.getString(R.string.failed_to_add_spot) + ":" +
                    resources.getString(R.string.no_connection),
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(binding.addSpotContainer)
                .show()
            return
        }

        val markerIsSet: Boolean = this.newSpotLatitude != null && this.newSpotLongitude != null
        val categoryIsNotEmpty: Boolean = binding.dropdownParkCategory.text.isNotEmpty()
        val streetToggleIsSet: Boolean =
            binding.toggleSpotSort.checkedButtonId == R.id.toggle_street
        if (markerIsSet) {

            if (streetToggleIsSet) {

                viewModel.addStreetSpot(
                    inputName.text.toString().trim(),
                    this.newSpotLatitude!!,
                    this.newSpotLongitude!!
                )
            } else {

                if (categoryIsNotEmpty) {
                    viewModel.addParkSpot(
                        inputName.text.toString().trim(), this.newSpotLatitude!!, this.newSpotLongitude!!, inputStreet.text.toString().trim(), inputNumber.text.toString().trim(), inputCity.text.toString().trim(), inputPostal.text.toString().trim(), inputState.text.toString().trim(), inputCountry.text.toString().trim(), binding.dropdownParkCategory.text.toString().trim(), binding.sliderFee.value.toDouble()
                    )
                } else {
                    binding.layoutParkCategory.error =
                        resources.getString(R.string.no_park_cat_selected)
                }
            }
        } else {
            Snackbar.make(this.requireView(), R.string.mark_new_spot_please, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.addSpotContainer)
                .show()
        }
    }

    private fun initMap() {
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        if (mapFragment == null) {
            val fm = parentFragmentManager
            val ft = fm.beginTransaction()
            mapFragment = SupportMapFragment.newInstance()
            ft.replace(R.id.map, mapFragment as SupportMapFragment).commit()
        }

        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "map ready")
        map = googleMap

        // sets map type
        map!!.mapType = GoogleMap.MAP_TYPE_NORMAL

        // on click marker no toolbar gets shown
        map!!.uiSettings.isMapToolbarEnabled = false

        // sets markers if already created in session
        if (newSpotLatitude != null && newSpotLongitude != null) {
            Log.i(TAG, "redraw marker")
            drawNewSpotMarker(newSpotLatitude!!, newSpotLongitude!!)
        }

        // draw spots
        viewModel.spots.observe(
            viewLifecycleOwner,
            { inComingSpots ->
                Log.i(TAG, "Incoming spots: $inComingSpots")
                // if markerCache contains a spot that is not in incoming spot -> delete marker from map
                markerCache.forEach { marker ->
                    val inComingSpotsDoesNotContainMarker: Boolean =
                        !inComingSpots.any { inComingSpot ->
                            inComingSpot.id == (marker.tag as Spot).id
                        }
                    if (inComingSpotsDoesNotContainMarker) marker.remove()
                }
                markerCache.removeAll { true }
                inComingSpots?.forEach { incomingSpot ->
                    val newMarker = drawMarker(
                        incomingSpot.latitude,
                        incomingSpot.longitude,
                        incomingSpot.name,
                        DRAWABLE_SPOT_MARKER
                    )
                    newMarker.tag = incomingSpot
                    markerCache.add(newMarker)
                }
            }
        )

        // handle marker clicking
        map!!.setOnMarkerClickListener {
            if (it.tag != null) {
                val tag = it.tag
                if (tag is Spot) {
                    Navigation.findNavController(requireView())
                        .navigate(HomeFragmentDirections.actionHomeFragmentToSpotDetailFragment(tag.id))
                }
            }
            true
        }

        // handles marker dragging
        map!!.setOnMarkerDragListener(
            object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragEnd(m: Marker) {
                    m.setIcon(
                        Utils.bitmapDescriptorFromVector(
                            requireContext(),
                            DRAWABLE_NEW_SPOT_MARKER
                        )
                    )
                    updateNewSpotLocation(m.position.latitude, m.position.longitude)
                }

                override fun onMarkerDragStart(m: Marker) {
                    m.setIcon(
                        Utils.bitmapDescriptorFromVector(
                            requireContext(),
                            DRAWABLE_NEW_SPOT_MARKER_ON_DRAG
                        )
                    )
                }

                override fun onMarkerDrag(p0: Marker) {
                }
            }
        )

        // handles map clicks for new spot creation
        map!!.setOnMapClickListener {
            if (binding.fab.isExpanded && newSpotMarker == null) {
                Log.i(TAG, "marker creation")

                drawNewSpotMarker(it.latitude, it.longitude)
            }
        }

        // handles camera movement
        map!!.setOnCameraIdleListener {
            val visibleRegion = map!!.projection.visibleRegion

            val center = visibleRegion.latLngBounds.center
            val farLeft = visibleRegion.farLeft
            val nearRight = visibleRegion.nearRight

            val diagonalDistance = FloatArray(1)

            Location.distanceBetween(
                farLeft.latitude,
                farLeft.longitude,
                nearRight.latitude,
                nearRight.longitude,
                diagonalDistance
            )

            val radiusInMeter = diagonalDistance[0] / 2

            viewModel.setCameraCenterAndRadius(center, radiusInMeter.toDouble())
        }

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }

    private fun drawNewSpotMarker(latitude: Double, longitude: Double) {
        // updating fields
        updateNewSpotLocation(latitude, longitude)

        // drawing marker
        newSpotMarker = drawMarker(latitude, longitude, "New Spot", DRAWABLE_NEW_SPOT_MARKER)
        newSpotMarker!!.isDraggable = true

        Log.i(TAG, "new spot marker:($latitude,$longitude)")

        // flag indicator empty
        binding.flag.setImageResource(R.drawable.ic_outlined_flag_24px)
    }

    private fun updateNewSpotLocation(latitude: Double, longitude: Double) {
        var possibleAddresses: List<Address>?
        try {
            possibleAddresses = gcd.getFromLocation(latitude, longitude, 10)
        } catch (ex: IOException) {
            possibleAddresses = null
        }
        val mostPossibleAddress: Address? = possibleAddresses?.get(0)
        binding.inputStreet.setText(mostPossibleAddress?.thoroughfare ?: "")
        binding.inputHouseNumber.setText(mostPossibleAddress?.featureName ?: "")
        binding.inputCity.setText(mostPossibleAddress?.locality ?: "")
        binding.inputPostalCode.setText(mostPossibleAddress?.postalCode ?: "")
        binding.inputState.setText(mostPossibleAddress?.subAdminArea ?: "")
        binding.inputCountry.setText(mostPossibleAddress?.countryName ?: "")

        newSpotLatitude = latitude
        newSpotLongitude = longitude
    }

    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private fun getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this.requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        Log.i(TAG, "fetched current location: ${task.result}")
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            // checks if camera position is already in session
                            if (cameraPosition != null) {
                                Log.i(
                                    TAG,
                                    "camera position in session:" +
                                        " $cameraPosition"
                                )

                                map!!.moveCamera(
                                    CameraUpdateFactory.newCameraPosition(
                                        cameraPosition
                                    )
                                )
                            } else {
                                Log.i(
                                    TAG,
                                    "camera position not in session use fetched pos: " +
                                        "$lastKnownLocation"
                                )

                                map!!.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude
                                        ),
                                        DEFAULT_ZOOM.toFloat()
                                    )
                                )
                            }
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        map!!.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map!!.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    // state save
    override fun onStop() {
        super.onStop()
        map?.let {
            cameraPosition = map!!.cameraPosition
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // saves coords of new spot marker is present
        newSpotMarker?.let {
            outState.putDoubleArray(
                KEY_NEW_SPOT_MARKER_COORDS,
                doubleArrayOf(
                    it.position.latitude,
                    it.position.longitude
                )
            )
        }

        // save camera pos
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POS, map.cameraPosition)
        }
    }
}
