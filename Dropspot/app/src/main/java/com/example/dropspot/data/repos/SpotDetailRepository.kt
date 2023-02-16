package com.example.dropspot.data.repos

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.dropspot.data.dao.SpotDao
import com.example.dropspot.data.dao.SpotDetailDao
import com.example.dropspot.data.model.Spot
import com.example.dropspot.data.model.SpotDetail
import com.example.dropspot.data.model.requests.ParkSpotUpdateRequest
import com.example.dropspot.data.model.requests.StreetSpotRequest
import com.example.dropspot.data.model.requests.VoteRequest
import com.example.dropspot.data.model.responses.MessageResponse
import com.example.dropspot.network.SpotService
import com.example.dropspot.network.UserService

class SpotDetailRepository(
    private val spotService: SpotService,
    private val spotDetailDao: SpotDetailDao,
    private val userService: UserService,
    private val spotDao: SpotDao
) {

    companion object {
        private val TAG = "spot_detail_repo"
    }

    fun getSpotDetailBySpotId(id: Long): LiveData<SpotDetail> {
        return spotDetailDao.getSpotDetailById(id)
    }

    suspend fun fetchAllSpotDetails() {
        try {
            val response = spotService.getSpotDetails()
            Log.i(TAG, "response getAll: $response")
            spotDetailDao.insertAll(response)
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Something went wrong with getSpotDetail")
        }
    }

    suspend fun fetchSpotDetailBySpotId(id: Long) {
        try {
            val response: SpotDetail = spotService.getSpotDetailById(id)
            Log.i(TAG, "response getId: $response")
            spotDetailDao.insert(response)
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Something went wrong with getSpotDetail")
        }
    }

    suspend fun vote(spotId: Long, criterionId: Long, voteRequest: VoteRequest): MessageResponse {
        try {
            return spotService.voteForSpot(voteRequest, spotId, criterionId)
        } catch (e: Exception) {
            return handleException(e)
        }
    }

    suspend fun favoriteSpot(spotDetail: SpotDetail): MessageResponse {
        try {
            val response: MessageResponse =
                userService.addFavoriteSpot(spotDetail.spotId)
            spotDetail.liked = true
            spotDetailDao.insert(spotDetail)
            return response
        } catch (e: Exception) {
            return handleException(e)
        }
    }

    suspend fun unFavoriteSpot(spotDetail: SpotDetail): MessageResponse {
        try {
            val response: MessageResponse =
                userService.removeFavoriteSpot(spotDetail.spotId)
            spotDetail.liked = false
            spotDetailDao.insert(spotDetail)
            return response
        } catch (e: Exception) {
            return handleException(e)
        }
    }

    suspend fun deleteSpot(spotDetail: SpotDetail): MessageResponse {
        try {
            val response: MessageResponse =
                spotService.deleteSpot(spotDetail.spotId)
            if (response.success) removeInCache(spotDetail)
            return response
        } catch (e: Exception) {
            return handleException(e)
        }
    }

    suspend fun updateParkSpot(
        request: ParkSpotUpdateRequest,
        spotDetail: SpotDetail
    ): MessageResponse {
        try {

            val response: SpotDetail = spotService.updateParkSpot(
                request,
                spotDetail.spotId
            )

            saveInCache(response)

            return MessageResponse(true)
        } catch (e: Exception) {
            return handleException(e)
        }
    }

    suspend fun updateStreetSpot(
        request: StreetSpotRequest,
        spotDetail: SpotDetail
    ): MessageResponse {
        try {
            val response: SpotDetail = spotService.updateStreetSpot(
                request,
                spotDetail.spotId
            )

            saveInCache(response)

            return MessageResponse(true)
        } catch (e: Exception) {
            return handleException(e)
        }
    }

    private fun handleException(e: Exception): MessageResponse {
        e.message?.let {
            return MessageResponse(false, it)
        }
        return MessageResponse()
    }

    private suspend fun saveInCache(response: SpotDetail) {
        spotDao.insert(
            Spot(
                response.spotId,
                response.creatorId,
                response.spotName,
                response.latitude,
                response.longitude
            )
        )

        spotDetailDao.insert(response)
    }

    private suspend fun removeInCache(response: SpotDetail) {
        spotDao.delete(
            Spot(
                response.spotId,
                response.creatorId,
                response.spotName,
                response.latitude,
                response.longitude
            )
        )

        spotDetailDao.delete(response)
    }
}
