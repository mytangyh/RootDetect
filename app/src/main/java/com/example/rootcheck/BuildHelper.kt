package com.example.rootcheck

import android.os.Build

class BuildHelper {
    companion object {
        fun getBuildInfo(): String {
            val deviceInfo = """
        |Board: ${Build.BOARD}
        |Brand: ${Build.BRAND}
        |Device: ${Build.DEVICE}
        |Display: ${Build.DISPLAY}
        |Fingerprint: ${Build.FINGERPRINT}
        |Hardware: ${Build.HARDWARE}
        |Host: ${Build.HOST}
        |ID: ${Build.ID}
        |Manufacturer: ${Build.MANUFACTURER}
        |Model: ${Build.MODEL}
        |Product: ${Build.PRODUCT}
        |Serial: ${Build.SERIAL}
        |SDK Version: ${Build.VERSION.SDK_INT}
        |Release: ${Build.VERSION.RELEASE}
    """.trimMargin()
            return deviceInfo
        }
    }

}
