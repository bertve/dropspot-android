package com.example.dropspot.data.model.responses

import com.example.dropspot.data.model.AppUser

data class JwtResponse(
    var message: String = "No message",
    val success: Boolean = false,
    val token: String = "",
    val user: AppUser? = null
) {
    private val type = "Bearer"
}
