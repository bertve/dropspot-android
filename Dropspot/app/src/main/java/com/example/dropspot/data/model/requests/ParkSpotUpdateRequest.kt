package com.example.dropspot.data.model.requests

import com.example.dropspot.data.model.Address
import com.example.dropspot.data.model.ParkCategory

data class ParkSpotUpdateRequest(
    private val name: String,
    private val latitude: Double,
    private val longitude: Double,
    private val address: Address,
    private val entranceFee: Double,
    private val parkCategory: ParkCategory
)
