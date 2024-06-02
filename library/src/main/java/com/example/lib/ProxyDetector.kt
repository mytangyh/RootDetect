package com.example.lib

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresPermission
import java.net.NetworkInterface
import java.net.SocketException

/**
 * 这个类用于检测设备是否正在使用代理。
 */
class ProxyDetector {
    companion object {
        /**
         * 检查设备是否检测到代理。
         *
         * @param context 应用程序上下文。
         * @return 如果检测到代理则返回true，否则返回false。
         */
        @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)

        fun isDetected(context: Context): Boolean {
            LogUtil.d("checkHttpProxy:${checkHttpProxy()}\nisVpnConnected${isVpnConnected(context)}\nisDeviceInVPN:${isDeviceInVPN()}")
            return checkHttpProxy() || isVpnConnected(context) || isDeviceInVPN()
        }

        /**
         * 检查设备是否设置了HTTP代理。
         *
         * @return 如果设置了HTTP代理则返回true，否则返回false。
         */
        fun checkHttpProxy() = System.getProperty("http.proxyHost")?.isNotEmpty() == true && System.getProperty("http.proxyPort")?.isNotEmpty() == true

        /**
         * 检查设备是否连接了VPN。
         *
         * @param context 应用程序上下文。
         * @return 如果连接了VPN则返回true，否则返回false。
         */
        @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)

        fun isVpnConnected(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm.activeNetwork?.let { network ->
                    cm.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
                } ?: false
            } else {
                cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_VPN
            }
        }


        /**
         * 检查设备是否在VPN中。
         *
         * @return 如果设备在VPN中则返回true，否则返回false。
         */
        fun isDeviceInVPN() = try {
            NetworkInterface.getNetworkInterfaces()?.asSequence()?.any { it.name in listOf("tun0", "ppp0") }==true
        } catch (e: SocketException) {
            false
        }
    }
}