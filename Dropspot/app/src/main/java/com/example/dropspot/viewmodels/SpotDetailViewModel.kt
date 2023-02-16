package com.example.dropspot.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dropspot.data.model.SpotDetail
import com.example.dropspot.data.model.requests.VoteRequest
import com.example.dropspot.data.model.responses.MessageResponse
import com.example.dropspot.data.repos.SpotDetailRepository
import kotlinx.coroutines.launch

class SpotDetailViewModel(private val spotDetailRepository: SpotDetailRepository) : ViewModel() {

    companion object {
        private const val TAG = "spot_detail_vm"
    }

    private var spotId: Long? = null

    private val _voteSuccess = MutableLiveData<MessageResponse>()
    val voteSuccess: LiveData<MessageResponse?>
        get() = _voteSuccess

    private val _favoriteSuccess = MutableLiveData<MessageResponse>()
    val favoriteSuccess: LiveData<MessageResponse>
        get() = _favoriteSuccess

    private val _unFavoriteSuccess = MutableLiveData<MessageResponse>()
    val unFavoriteSuccess: LiveData<MessageResponse>
        get() = _favoriteSuccess

    private val _deleteSuccess = MutableLiveData<MessageResponse>()
    val deleteSuccess: LiveData<MessageResponse>
        get() = _deleteSuccess

    fun getSpotDetail(): LiveData<SpotDetail> {
        return spotDetailRepository.getSpotDetailBySpotId(this.spotId!!)
    }

    fun setSpotId(id: Long) {
        this.spotId = id
        viewModelScope.launch {
            spotDetailRepository.fetchSpotDetailBySpotId(id)
        }
    }

    fun vote(criterionId: Long, value: Double) {
        Log.i(TAG, "VOTE spotId:$spotId criterionId:$criterionId val:$value")
        val spotId = this.spotId
        viewModelScope.launch {
            _voteSuccess.value = spotDetailRepository.vote(
                spotId!!,
                criterionId,
                VoteRequest(value)
            )
            _voteSuccess.value = null
        }
    }

    fun favoriteSpot(spotDetail: SpotDetail) {
        viewModelScope.launch {
            _favoriteSuccess.value = spotDetailRepository.favoriteSpot(spotDetail)
            _favoriteSuccess.value = null
        }
    }

    fun unFavoriteSpot(spotDetail: SpotDetail) {
        viewModelScope.launch {
            _unFavoriteSuccess.value = spotDetailRepository.unFavoriteSpot(spotDetail)
            _unFavoriteSuccess.value = null
        }
    }

    fun deleteSpot(spotDetail: SpotDetail) {
        Log.i(TAG, "delete spot: $spotDetail")
        viewModelScope.launch {
            _deleteSuccess.value = spotDetailRepository.deleteSpot(spotDetail)
            _deleteSuccess.value = null
        }
    }
}
