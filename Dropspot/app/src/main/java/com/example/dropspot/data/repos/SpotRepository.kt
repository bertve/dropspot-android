package com.example.dropspot.data.repos

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.dropspot.data.dao.SpotDao
import com.example.dropspot.data.model.ParkCategory
import com.example.dropspot.data.model.Spot
import com.example.dropspot.data.model.requests.ParkSpotRequest
import com.example.dropspot.data.model.requests.StreetSpotRequest
import com.example.dropspot.network.SpotService
import com.example.dropspot.utils.Variables

class SpotRepository(
    private val spotDao: SpotDao,
    private val spotService: SpotService
) {
    companion object {
        private val TAG = "spot_repo"
    }

    private val _spots = spotDao.getAllSpots()
    val spots: LiveData<List<Spot>> get() = _spots

    suspend fun getSpotsInRadius(latitude: Double, longitude: Double, radius: Double) {
        if (Variables.isNetworkConnected.value!!) {
            try {
                val onlineSpots: List<Spot> =
                    spotService.getSpotsInRadius(latitude, longitude, radius)
                Log.i(TAG, "online_spots_insertion:\n" + onlineSpots.toString())
                spotDao.insertAll(onlineSpots)
            } catch (e: Exception) {
                Log.d(TAG, e.message ?: "something went wrong with getSpotsInRadius")
            }
        }
    }

    suspend fun addStreetSpot(name: String, latitude: Double, longitude: Double): Spot? {

        val request = StreetSpotRequest(name, latitude, longitude)
        Log.i(TAG, request.toString())

        try {
            val spotResponse = spotService.addStreetSpot(request)
            spotDao.insert(spotResponse)
            return spotResponse
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "something went wrong with addStreetSpot")
            return null
        }
    }

    suspend fun addParkSpot(
        name: String,
        latitude: Double,
        longitude: Double,
        street: String,
        houseNumber: String,
        city: String,
        postalCode: String,
        state: String,
        country: String,
        parkCategory: ParkCategory,
        fee: Double
    ): Spot? {

        val request = ParkSpotRequest(
            name,
            latitude,
            longitude,
            fee,
            parkCategory,
            street,
            houseNumber,
            postalCode,
            city,
            state,
            country
        )
        Log.i(TAG, request.toString())

        try {
            val spotResponse = spotService.addParkSpot(request)
            spotDao.insert(spotResponse)
            return spotResponse
        } catch (e: Exception) {
            Log.d("spot_response", e.message ?: "something went wrong with addParkSpot")
            return null
        }
    }
}
