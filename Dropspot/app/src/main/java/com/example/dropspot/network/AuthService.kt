package com.example.dropspot.network

import com.example.dropspot.data.model.requests.LoginRequest
import com.example.dropspot.data.model.requests.RegisterRequest
import com.example.dropspot.data.model.responses.JwtResponse
import com.example.dropspot.data.model.responses.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("auth/signup/user")
    suspend fun register(@Body signUpRequest: RegisterRequest): Response<MessageResponse>

    @POST("auth/signin")
    suspend fun login(@Body loginRequest: LoginRequest): Response<JwtResponse>
}
