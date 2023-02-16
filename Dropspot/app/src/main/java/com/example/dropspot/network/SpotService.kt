package com.example.dropspot.network

import com.example.dropspot.data.model.Spot
import com.example.dropspot.data.model.SpotDetail
import com.example.dropspot.data.model.requests.ParkSpotRequest
import com.example.dropspot.data.model.requests.ParkSpotUpdateRequest
import com.example.dropspot.data.model.requests.StreetSpotRequest
import com.example.dropspot.data.model.requests.VoteRequest
import com.example.dropspot.data.model.responses.MessageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SpotService {

    @GET("spots/{spotId}/detail")
    suspend fun getSpotDetailById(@Path("spotId") id: Long): SpotDetail

    @DELETE("spots/{spotId}")
    suspend fun deleteSpot(@Path("spotId") id: Long): MessageResponse

    @PUT("spots/street/{spotId}")
    suspend fun updateStreetSpot(
        @Body spot: StreetSpotRequest,
        @Path("spotId") id: Long
    ): SpotDetail

    @PUT("spots/park/{spotId}")
    suspend fun updateParkSpot(
        @Body spot: ParkSpotUpdateRequest,
        @Path("spotId") id: Long
    ): SpotDetail

    @POST("spots/{spotId}/criteria/{criterionId}/vote")
    suspend fun voteForSpot(
        @Body voteRequest: VoteRequest,
        @Path("spotId") spotId: Long,
        @Path("criterionId") criterionId: Long
    ): MessageResponse

    @POST("spots/street")
    suspend fun addStreetSpot(@Body spot: StreetSpotRequest): Spot

    @POST("spots/park")
    suspend fun addParkSpot(@Body spot: ParkSpotRequest): Spot

    @GET("spots/getByRadius/{lat}/{long}/{radius}")
    suspend fun getSpotsInRadius(
        @Path("lat") latitude: Double,
        @Path("long") longitude: Double,
        @Path("radius") radius: Double
    ): List<Spot>

    @GET("spots/detail")
    suspend fun getSpotDetails(): List<SpotDetail>
}
