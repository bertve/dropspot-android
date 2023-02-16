package com.example.dropspot.network

import com.example.dropspot.data.model.AppUser
import com.example.dropspot.data.model.SpotDetail
import com.example.dropspot.data.model.responses.MessageResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {

    @GET("users/me")
    suspend fun getMe(): AppUser

    @GET("users/mySpots")
    suspend fun getMySpots(): List<SpotDetail>

    @GET("users/favorites")
    suspend fun getMyFavoriteSpots(): List<SpotDetail>

    @POST("users/favorites/{spotId}")
    suspend fun addFavoriteSpot(@Path("spotId") id: Long): MessageResponse

    @DELETE("users/favorites/{spotId}")
    suspend fun removeFavoriteSpot(@Path("spotId") id: Long): MessageResponse
}
