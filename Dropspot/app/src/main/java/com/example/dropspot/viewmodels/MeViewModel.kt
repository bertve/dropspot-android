package com.example.dropspot.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dropspot.data.model.AppUser
import com.example.dropspot.data.model.SpotDetail
import com.example.dropspot.data.repos.MeRepository
import kotlinx.coroutines.launch

class MeViewModel(private val meRepository: MeRepository) : ViewModel() {

    private var user: AppUser? = null

    val favoriteSpots: LiveData<List<SpotDetail>> = meRepository.getMyFavoriteSpots()

    fun getMySpots(): LiveData<List<SpotDetail>> {
        return meRepository.getMySpots(user!!.userId)
    }

    fun setUser(user: AppUser) {
        this.user = user
        fetchMySpots()
        fetchMyFavoriteSpots()
    }

    private fun fetchMySpots() {
        viewModelScope.launch {
            meRepository.fetchMySpots()
        }
    }

    private fun fetchMyFavoriteSpots() {
        viewModelScope.launch {
            meRepository.fetchMyFavoriteSpots()
        }
    }
}
