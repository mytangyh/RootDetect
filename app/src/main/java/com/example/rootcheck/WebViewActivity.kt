package com.example.rootcheck

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rootcheck.ImageActivity.Companion
import com.example.rootcheck.databinding.ActivityWebViewBinding
import java.io.ByteArrayOutputStream
import java.security.Permission

class WebViewActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityWebViewBinding
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
        private const val REQUEST_FILE_PICKER = 2
        private const val REQUEST_CODE_PERMISSION_READ_MEDIA_IMAGES = 1003
        private const val REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 1005
        private const val REQUEST_CODE_OPEN_GALLERY = 1004
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mBinding.checkBtn.setOnClickListener {
            checkAndRequestPermissions()
        }
        val webSettings: WebSettings =  mBinding.webView.settings
        webSettings.javaScriptEnabled = true // 启用JavaScript
        webSettings.allowFileAccess = true
        webSettings.allowFileAccessFromFileURLs =false
        webSettings.setAllowUniversalAccessFromFileURLs(false);
//        webSettings.allowContentAccess = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true)
        }
        mBinding.webView.webViewClient = WebViewClient() // 保证使用WebView而不是默认浏览器打开网页
//        val url = "https://www.baidu.com/"
//        val url2 = "https://117.157.68.157:8088/SDK-list/SDK-list.html"
////        mBinding.webView.loadUrl("https://www.baidu.com/")
//        mBinding.webBtn.setOnClickListener {
//            mBinding.webView.loadUrl(url2)
//        }
        mBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (ContextCompat.checkSelfPermission(this@WebViewActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@WebViewActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
                    return false
                }

                this@WebViewActivity.filePathCallback = filePathCallback
                val takePictureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_FILE_PICKER)
                }
                return true
            }
        }
        mBinding.webView.loadUrl("file:///android_asset/test.html")
        
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
        if (requestCode == REQUEST_CODE_PERMISSION_READ_MEDIA_IMAGES) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "获得READ_MEDIA_IMAGES的权限", Toast.LENGTH_SHORT).show()

                openGallery()
            } else {
                Toast.makeText(this, "需要READ_MEDIA_IMAGES的权限", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "获得READ_EXTERNAL_STORAGE的权限", Toast.LENGTH_SHORT).show()

                openGallery()
            } else {
                Toast.makeText(this, "需要READ_EXTERNAL_STORAGE的权限", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkAndRequestPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13及以上版本
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "READ_MEDIA_IMAGES", Toast.LENGTH_SHORT).show()

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        REQUEST_CODE_PERMISSION_READ_MEDIA_IMAGES
                    )
                } else {
                    openGallery()
                }
            }
            else -> {
                // Android 13以下版本
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show()

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE
                    )
                } else {
                    openGallery()
                }
            }
        }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_OPEN_GALLERY)
    }

}