package com.example.dropspot.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dropspot.data.model.requests.LoginRequest
import com.example.dropspot.data.model.responses.JwtResponse
import com.example.dropspot.network.AuthService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class LoginViewModel(
    private val authService: AuthService,
    private val gson: Gson
) : ViewModel() {

    private val _loginResponse = MutableLiveData<JwtResponse>()
    val loginResponse: LiveData<JwtResponse> get() = _loginResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun login(emailOrUsername: String, password: String) {
        // start wheel
        _isLoading.postValue(true)

        val request = LoginRequest(emailOrUsername, password)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = authService.login(request)
                if (response.code() == 200) {
                    _loginResponse.postValue(response.body())
                } else {
                    if (response.code() == 400) {
                        _loginResponse.postValue(
                            gson.fromJson(
                                response.errorBody()!!.string(),
                                JwtResponse::class.java
                            )
                        )
                    }
                }

                _isLoading.postValue(false)
            } catch (e: SocketTimeoutException) {
                login(emailOrUsername, password)
                Log.i("login_req", "socket timeout")
            } catch (e: Exception) {
                e.message?.let {
                    _loginResponse.postValue(JwtResponse(it))
                } ?: run {
                    _loginResponse.postValue(JwtResponse())
                }

                _isLoading.postValue(false)
            }
        }
    }

    fun resetResponses() {
        _loginResponse.value = null
    }
}
