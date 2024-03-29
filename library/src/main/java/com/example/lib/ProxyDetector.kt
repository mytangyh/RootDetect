package com.example.lib

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
        fun isDetected(context: Context): Boolean {
            return checkHttpProxy() || isVpnConnected(context) || isDeviceInVPN()
        }

        /**
         * 检查设备是否设置了HTTP代理。
         *
         * @return 如果设置了HTTP代理则返回true，否则返回false。
         */
        private fun checkHttpProxy() = System.getProperty("http.proxyHost")?.isNotEmpty() == true && System.getProperty("http.proxyPort")?.isNotEmpty() == true

        /**
         * 检查设备是否连接了VPN。
         *
         * @param context 应用程序上下文。
         * @return 如果连接了VPN则返回true，否则返回false。
         */
        private fun isVpnConnected(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        }

        /**
         * 检查设备是否在VPN中。
         *
         * @return 如果设备在VPN中则返回true，否则返回false。
         */
        private fun isDeviceInVPN() = try {
            NetworkInterface.getNetworkInterfaces().asSequence().any { it.name in listOf("tun0", "ppp0") }
        } catch (e: SocketException) {
            false
        }
    }
}