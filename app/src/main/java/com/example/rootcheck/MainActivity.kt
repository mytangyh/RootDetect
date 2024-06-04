package com.example.rootcheck

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rootcheck.databinding.ActivityMainBinding
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


class MainActivity : AppCompatActivity() {
    private lateinit var mbinding: ActivityMainBinding
    private var TAG = "MainActivity"
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
//            val bottomSheetDialog = BottomSheetDialog()
//            bottomSheetDialog.show(supportFragmentManager, "BottomSheetDialog")

            val encryptData = KeyStoreUtil.encryptData("密码")
            val encode = encryptData?.let { it1 -> Base64.encode(it1) }
            Toast.makeText(this,encode, Toast.LENGTH_LONG).show()

            if (encryptData != null) {
                val decode = encode?.let { it1 -> Base64.decode(it1) }
                val decryptData = decode?.let { it1 -> KeyStoreUtil.decryptData(it1) }
                Toast.makeText(this,decryptData, Toast.LENGTH_LONG).show()
            }
            startActivity(Intent(this,com.example.rootcheck.SeekBar::class.java))


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

//    private fun showCustomBottomSheetDialog() {
//        val bottomSheetDialog = BottomSheetDialog(this)
//        val bottomSheetView = layoutInflater.inflate(R.layout.custom_bottom_sheet, null)
//        bottomSheetDialog.setContentView(bottomSheetView)
//
//        val title: TextView = bottomSheetView.findViewById(R.id.title)
//        val summary: TextView = bottomSheetView.findViewById(R.id.summary)
//        val progressBar: ProgressBar = bottomSheetView.findViewById(R.id.progress_bar)
//        val recyclerView: RecyclerView = bottomSheetView.findViewById(R.id.recycler_view)
//        val buttonClose: Button = bottomSheetView.findViewById(R.id.button_close)
//
//        // 设置RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        // 设置数据
//        val items = listOf(
//            Item("恒生电子", 300, "提交成功"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            Item("浙商证券", 400, "提交失败"),
//            // 添加更多数据项...
//        )
//
//        val adapter = CustomAdapter(this, items)
//        recyclerView.adapter = adapter
//
//        // 设置关闭按钮事件
//        buttonClose.setOnClickListener {
//            bottomSheetDialog.dismiss()
//        }
//
//        bottomSheetDialog.show()
//    }

}


