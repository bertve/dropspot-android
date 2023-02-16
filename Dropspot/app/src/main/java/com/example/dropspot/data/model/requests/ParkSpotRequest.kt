package com.example.dropspot.data.model.requests

import com.example.dropspot.data.model.ParkCategory

data class ParkSpotRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val entranceFee: Double,
    val parkCategory: ParkCategory,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val state: String,
    val country: String
)
