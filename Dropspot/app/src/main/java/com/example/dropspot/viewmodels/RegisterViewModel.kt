package com.example.dropspot.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dropspot.data.model.requests.RegisterRequest
import com.example.dropspot.data.model.responses.MessageResponse
import com.example.dropspot.network.AuthService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class RegisterViewModel(
    private val authService: AuthService,
    private val gson: Gson
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _registerResponse = MutableLiveData<MessageResponse>()
    val registerResponse: LiveData<MessageResponse> get() = _registerResponse

    fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String
    ) {
        // start wheel
        _isLoading.postValue(true)
        val request =
            RegisterRequest(firstName, lastName, username, email, password)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = authService.register(request)

                // handle bad request
                if (response.code() == 200) {
                    _registerResponse.postValue(response.body())
                } else {
                    if (response.code() == 400) {
                        _registerResponse.postValue(
                            gson.fromJson(
                                response.errorBody()!!.string(),
                                MessageResponse::class.java
                            )
                        )
                    }
                }

                _isLoading.postValue(false)
            } catch (e: SocketTimeoutException) {
                register(firstName, lastName, username, email, password)
                Log.i("register_req", "socket timeout")
            } catch (e: Exception) {
                e.message?.let {
                    _registerResponse.postValue(MessageResponse(false, it))
                } ?: run {
                    _registerResponse.postValue(MessageResponse())
                }
                _isLoading.postValue(false)
            }
        }
    }

    fun resetResponses() {
        _registerResponse.value = null
    }
}
