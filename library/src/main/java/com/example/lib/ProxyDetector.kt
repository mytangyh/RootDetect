package com.example.lib

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.net.NetworkInterface
import java.net.SocketException


class ProxyDetector {
    companion object {
        fun isDetected(context: Context): Boolean {
            return checkHttpProxy()||isVpnConnected(context)||isDeviceInVPN()
        }

        fun checkHttpProxy(): Boolean {

            val proxyHost: String? = System.getProperty("http.proxyHost")
            val proxyPort: String? = System.getProperty("http.proxyPort")

            return !proxyHost.isNullOrEmpty() && !proxyPort.isNullOrEmpty()

        }

        fun isVpnConnected(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = cm.activeNetwork
                if (network != null) {
                    val capabilities = cm.getNetworkCapabilities(network)
                    if (capabilities != null) {
                        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    }
                }
            } else {
                val networkInfo = cm.activeNetworkInfo
                if (networkInfo != null) {
                    return networkInfo.type == ConnectivityManager.TYPE_VPN
                }
            }
            return false
        }

        fun isDeviceInVPN(): Boolean {
            try {
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                while (networkInterfaces.hasMoreElements()) {
                    val name = networkInterfaces.nextElement().name
                    if (name == "tun0" || name == "ppp0") {
                        return true
                    }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            }
            return false
        }
    }

}