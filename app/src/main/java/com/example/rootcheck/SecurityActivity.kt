package com.example.rootcheck

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lib.EmulatorDetectorNew
import com.example.lib.HookDetector
import com.example.lib.ProxyDetector
import com.example.rootcheck.databinding.ActivitySecurityBinding

class SecurityActivity : AppCompatActivity() {
    private var TAG = "SecurityActivity"
    private lateinit var binding: ActivitySecurityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.checkBtn.setOnClickListener {
            val emulatorDetectorNew = EmulatorDetectorNew()
            var detected = false
            var checkFrida: Boolean
            val buildStr =
                "\nBRAND:${Build.BRAND} \nDEVICE:${Build.DEVICE} \nRELEASE:${Build.VERSION.RELEASE} \nMODEL:${Build.MODEL}"

            val builder = AlertDialog.Builder(this)
            builder.setTitle("checkFrida!!")
            builder.setPositiveButton("确定") { _, _ ->
            }
            val dialog = builder.create()
            dialog.show()
            val xposedExists = HookDetector.isXposedExists()
            val eposedExistByThrow = HookDetector.isEposedExistByThrow()
            val tryShutdownXposed = HookDetector.tryShutdownXposed()
            val classCheck = HookDetector.classCheck()
            val exceptionCheck = HookDetector.exceptionCheck()
            val checkHttpProxy = ProxyDetector.checkHttpProxy()
            val vpnConnected1 = ProxyDetector.isVpnConnected(this)
            val deviceInVPN = ProxyDetector.isDeviceInVPN()
            Toast.makeText(this, toastMessage(), Toast.LENGTH_SHORT).show()

        }

    }

    private fun toastMessage(): String {
        return "我未被劫持"
    }

    private fun getCountry(context: Context): String {
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        return tm.networkCountryIso
    }

    private fun getNetName(context: Context): String {
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        return tm.simOperatorName
    }

}