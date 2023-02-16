package com.example.dropspot.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dropspot.data.model.Address
import com.example.dropspot.data.model.ParkCategory
import com.example.dropspot.data.model.SpotDetail
import com.example.dropspot.data.model.requests.ParkSpotUpdateRequest
import com.example.dropspot.data.model.requests.StreetSpotRequest
import com.example.dropspot.data.model.responses.MessageResponse
import com.example.dropspot.data.repos.SpotDetailRepository
import kotlinx.coroutines.launch

class EditSpotDetailViewModel(
    private val spotDetailRepository: SpotDetailRepository
) : ViewModel() {

    var spotDetail: SpotDetail? = null

    private val _updateSuccess = MutableLiveData<MessageResponse>()
    val updateSuccess: LiveData<MessageResponse>
        get() = _updateSuccess

    fun updateParkSpot(
        name: String,
        street: String,
        number: String,
        city: String,
        postal: String,
        state: String,
        country: String,
        cat: String,
        fee: Double
    ) {

        val parkCategory: ParkCategory = ParkCategory.parkCategoryFromString(cat)

        val request = ParkSpotUpdateRequest(
            name,
            spotDetail!!.latitude,
            spotDetail!!.longitude,
            Address(street, number, postal, city, state, country),
            fee,
            parkCategory
        )

        viewModelScope.launch {
            _updateSuccess.value = spotDetailRepository.updateParkSpot(request, spotDetail!!)
        }
    }

    fun updateStreetSpot(name: String) {

        val request = StreetSpotRequest(
            name,
            spotDetail!!.latitude,
            spotDetail!!.longitude
        )

        viewModelScope.launch {
            _updateSuccess.value = spotDetailRepository.updateStreetSpot(
                request,
                spotDetail!!
            )
        }
    }
}
