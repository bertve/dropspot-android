package com.example.dropspot.network

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.dropspot.utils.Variables

class NetworkMonitor
@RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
constructor(private val application: Application) {
    companion object {
        private const val TAG = "network_monitor"
    }

    fun startNetworkCallback() {
        Log.i(TAG, "starting callback")
        val cm: ConnectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder: NetworkRequest.Builder = NetworkRequest.Builder()

        cm.registerNetworkCallback(
            builder.build(),
            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    Log.i(TAG, "network available")
                    Variables.isNetworkConnected.postValue(true)
                }

                override fun onLost(network: Network) {
                    Log.i(TAG, "network conn lost")
                    Variables.isNetworkConnected.postValue(false)
                }
            }
        )
    }

    fun stopNetworkCallback() {
        Log.i(TAG, "stopping callback")
        val cm: ConnectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(ConnectivityManager.NetworkCallback())
    }
}
