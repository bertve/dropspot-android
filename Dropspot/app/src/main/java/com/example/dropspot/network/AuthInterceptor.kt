package com.example.dropspot.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.Interceptor
import okhttp3.Response

object AuthInterceptor : Interceptor {

    private var sessionToken: String = ""
    private val AUTH_NAME = "authorization"
    private val _isSessionExpired = MutableLiveData<Boolean>()
    val isSessionExpired: LiveData<Boolean> get() = _isSessionExpired

    /**
     * sets the session token
     * */
    fun setSessionToken(sessionToken: String) {
        this.sessionToken = sessionToken
    }

    /**
     * Clears the session token
     * */
    fun clearSessionToken() {
        this.sessionToken = ""
    }

    /**
     * Gets the raw session token
     * */
    fun getToken(): String? {
        return sessionToken
    }

    /**
     * intercepts the request and if it does not already contains
     * a auth header it adds the current token properly formatted
     *
     * */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        if (request.header(AUTH_NAME) == null) {
            requestBuilder.addHeader(AUTH_NAME, generateFormattedToken())
        }
        val response: Response = chain.proceed(requestBuilder.build())

        // handle 401: Unauthorized
        if (response.code() == 401) {
            _isSessionExpired.postValue(true)
        } else {
            _isSessionExpired.postValue(false)
        }

        return response
    }

    /**
     * Formats the token in the way the api will accept it
     * */
    fun generateFormattedToken(): String {
        return "Bearer $sessionToken"
    }
}
