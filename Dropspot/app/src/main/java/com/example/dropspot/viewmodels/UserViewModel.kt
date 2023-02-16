package com.example.dropspot.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dropspot.data.model.AppUser
import com.example.dropspot.data.repos.MeRepository
import com.example.dropspot.data.repos.SpotDetailRepository
import com.example.dropspot.network.AuthInterceptor
import kotlinx.coroutines.launch

class UserViewModel(
    private val meRepository: MeRepository,
    private val spotDetailRepository: SpotDetailRepository
) : ViewModel() {

    companion object {
        private const val TAG = "user_vm"
    }

    private val _currentUser = MutableLiveData<AppUser>()
    val currentUser: LiveData<AppUser> get() = _currentUser

    val isSessionExpired: LiveData<Boolean> = AuthInterceptor.isSessionExpired

    fun setSessionToken(token: String) {
        AuthInterceptor.setSessionToken(token)
    }

    fun setUser(loggedInUser: AppUser) {
        _currentUser.value = loggedInUser
        fetchUser()
        fetchSpotDetails()
    }

    fun fetchUser() {
        Log.i(TAG, "fetching user...")
        viewModelScope.launch {
            val response = meRepository.fetchMe()
            response?.let {
                _currentUser.value = it
            }
        }
    }

    private fun fetchSpotDetails() {
        Log.i(TAG, "fetching spotDetails...")
        viewModelScope.launch {
            spotDetailRepository.fetchAllSpotDetails()
        }
    }
}
