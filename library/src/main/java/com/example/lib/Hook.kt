package com.example.lib

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

class Hook:IDetection {

    fun detectXposedModules(context: Context, lspatch: Boolean): String {
        val pm = context.packageManager
        val metaKey = if (lspatch) "lspatch" else "xposedminversion"
        val metaKey2 = if (lspatch) "jshook" else "xposeddescription"

        // Check installed applications
        val applications = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (pkg in applications) {
                Log.d("TAG", "detectXposedModules: ${pkg.name}")
                val metaData = pkg.metaData
                if (metaData?.get(metaKey) != null || metaData?.get(metaKey2) != null) {
                    return pm.getApplicationLabel(pkg) as String
            }
        }

        // Check activities
        val activities = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN), PackageManager.GET_META_DATA)
        for (pkg in activities) {
            val ainfo = pkg.activityInfo.applicationInfo
            if (lspatch && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ainfo.appComponentFactory?.contains("lsposed") == true) {
                    return pm.getApplicationLabel(ainfo) as String
                }
            }

            val metaData = ainfo.metaData
            if (metaData?.get(metaKey) != null || metaData?.get(metaKey2) != null) {
                return pm.getApplicationLabel(ainfo) as String
            }
        }

        return "Result.NOT_FOUND"
    }



    override fun isDetected(context: Context): Boolean {
        TODO("Not yet implemented")
    }

    override fun getResults(): List<String> {
        TODO("Not yet implemented")
    }
}