package com.example.dropspot.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dropspot.data.model.ParkCategory
import com.example.dropspot.data.model.Spot
import com.example.dropspot.data.repos.SpotRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class HomeViewModel(private val spotRepository: SpotRepository) : ViewModel() {

    val spots: LiveData<List<Spot>> = spotRepository.spots

    private val _addStreetSpotSuccess = MutableLiveData<Boolean?>()
    val addStreetSpotSuccess: LiveData<Boolean?> get() = _addStreetSpotSuccess

    private val _addParkSpotSuccess = MutableLiveData<Boolean?>()
    val addParkSpotSuccess: LiveData<Boolean?> get() = _addParkSpotSuccess

    fun addStreetSpot(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _addStreetSpotSuccess.value =
                spotRepository.addStreetSpot(name, latitude, longitude) != null
            _addStreetSpotSuccess.value = null
        }
    }

    fun addParkSpot(
        name: String,
        latitude: Double,
        longitude: Double,
        street: String,
        houseNumber: String,
        city: String,
        postalCode: String,
        state: String,
        country: String,
        category: String,
        fee: Double
    ) {
        val parkCategory: ParkCategory = ParkCategory.parkCategoryFromString(category)

        viewModelScope.launch {
            _addParkSpotSuccess.value = spotRepository.addParkSpot(
                name, latitude, longitude, street, houseNumber, city, postalCode, state, country, parkCategory, fee
            ) != null
            _addParkSpotSuccess.value = null
        }
    }

    // gets called after map is created
    fun setCameraCenterAndRadius(center: LatLng, radiusInMeter: Double) {
        viewModelScope.launch {
            spotRepository.getSpotsInRadius(center.latitude, center.longitude, radiusInMeter)
        }
    }
}
