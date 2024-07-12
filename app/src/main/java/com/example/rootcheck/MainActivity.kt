package com.example.rootcheck

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rootcheck.databinding.ActivityMainBinding
import kotlin.io.encoding.ExperimentalEncodingApi
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var mbinding: ActivityMainBinding
    private var TAG = "MainActivity"
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
        private const val REQUEST_FILE_PICKER = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mbinding.root)
        KeyStoreUtil.createKeys()
        init()

    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun init() {
        mbinding.passNew.transformationMethod = CustomPasswordTransformationMethod()

        mbinding.checkBtn.setOnClickListener {

            startActivity(Intent(this, SecurityActivity::class.java))
        }
        mbinding.towTabBtn.setOnClickListener {
            startActivity(Intent(this, TabLayoutActivity::class.java))
        }
        mbinding.dialogBtn.setOnClickListener {
//            showCustomBottomSheetDialog()
            val bottomSheetDialog = BottomSheetDialog()
            bottomSheetDialog.show(supportFragmentManager, "BottomSheetDialog")

//            val encryptData = KeyStoreUtil.encryptData("密码")
//            val encode = encryptData?.let { it1 -> Base64.encode(it1) }
//            Toast.makeText(this,encode, Toast.LENGTH_LONG).show()
//
//            if (encryptData != null) {
//                val decode = encode?.let { it1 -> Base64.decode(it1) }
//                val decryptData = decode?.let { it1 -> KeyStoreUtil.decryptData(it1) }
//                Toast.makeText(this,decryptData, Toast.LENGTH_LONG).show()
//            }
//            startActivity(Intent(this,com.example.rootcheck.SeekBar::class.java))


        }
        mbinding.buildBtn.setOnClickListener {
//            mbinding.tvTest.text = BuildHelper.getBuildInfo()
            mbinding.tvTest.text = RequestPrivacy(this).getPhoneState()
        }
        val webSettings: WebSettings =  mbinding.webView.settings
        webSettings.javaScriptEnabled = true // 启用JavaScript
        webSettings.allowFileAccess = true
//        webSettings.allowContentAccess = true

        mbinding.webView.webViewClient = WebViewClient() // 保证使用WebView而不是默认浏览器打开网页
//        val url = "https://www.baidu.com/"
//        val url2 = "https://117.157.68.157:8088/SDK-list/SDK-list.html"
////        mbinding.webView.loadUrl("https://www.baidu.com/")
//        mbinding.webBtn.setOnClickListener {
//            mbinding.webView.loadUrl(url2)
//        }
        mbinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
                    return false
                }

                this@MainActivity.filePathCallback = filePathCallback
                val takePictureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_FILE_PICKER)
                }
                return true
            }
        }
        mbinding.webView.loadUrl("file:///android_asset/test.html")

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FILE_PICKER && resultCode == RESULT_OK) {
            filePathCallback?.let {
                val result = data?.extras?.get("data") as? Bitmap
                result?.let {
                    val uri = getImageUri(this, it)
                    filePathCallback?.onReceiveValue(arrayOf(uri))
                }
            }
        }else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }
    private fun getImageUri(context: Context, image: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, image, System.currentTimeMillis().toString(), null)
        return Uri.parse(path)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }



    class CustomPasswordTransformationMethod : PasswordTransformationMethod() {

        override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
            // 自定义密码转换逻辑
            return CustomPasswordCharSequence(source)
        }

        private class CustomPasswordCharSequence(private val source: CharSequence?) : CharSequence {

            override val length: Int
                get() = source?.length ?: 0

            override fun get(index: Int): Char {
                // 自定义密码显示的字符，这里使用了'*'
                return '*'
            }

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                // 返回部分源字符序列
                return source?.subSequence(startIndex, endIndex) ?: ""
            }
        }
    }



}


