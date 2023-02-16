package com.example.dropspot.data.repos

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.dropspot.data.dao.SpotDetailDao
import com.example.dropspot.data.model.AppUser
import com.example.dropspot.data.model.SpotDetail
import com.example.dropspot.network.UserService
import com.example.dropspot.utils.Variables
import java.net.SocketTimeoutException

class MeRepository(private val userService: UserService, private val spotDetailDao: SpotDetailDao) {

    companion object {
        private const val TAG = "me_repo"
    }

    fun getMySpots(userId: Long): LiveData<List<SpotDetail>> =
        spotDetailDao.getSpotDetailByCreatorId(userId)

    fun getMyFavoriteSpots(): LiveData<List<SpotDetail>> =
        spotDetailDao.getSpotLikedSpotDetails()

    suspend fun fetchMySpots() {
        if (Variables.isNetworkConnected.value!!) {
            try {
                val response = userService.getMySpots()
                Log.i(TAG, "Response fetchMySpots: $response")
                spotDetailDao.insertAll(response)
            } catch (e: Exception) {
                Log.d(TAG, e.message ?: "Something went wrong with fetchMySpots")
            }
        }
    }

    suspend fun fetchMyFavoriteSpots() {
        if (Variables.isNetworkConnected.value!!) {
            try {
                val response = userService.getMyFavoriteSpots()
                Log.i(TAG, "Response fetchMyFavoriteSpots: $response")
                spotDetailDao.insertAll(response)
            } catch (e: Exception) {
                Log.d(TAG, e.message ?: "Something went wrong with fetchMyFavoriteSpots")
            }
        }
    }

    suspend fun fetchMe(): AppUser? {
        try {
            val response = userService.getMe()
            Log.i(TAG, "fetched: $response")

            return response
        } catch (e: SocketTimeoutException) {
            fetchMe()
        } catch (e: Throwable) {
            Log.i("TAG", e.message ?: "fetch me failed")
        }
        return null
    }
}
