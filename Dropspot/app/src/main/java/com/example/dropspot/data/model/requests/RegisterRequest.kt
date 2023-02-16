package com.example.dropspot.data.model.requests

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val password: String
)
