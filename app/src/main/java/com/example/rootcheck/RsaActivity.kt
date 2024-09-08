package com.example.rootcheck

import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rootcheck.databinding.ActivityRsaBinding
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class RsaActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityRsaBinding
    private var encrypted: ByteArray? = null
    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey
    private val publicKeyDirectory = "public" // Public key directory in assets
    private val privateKeyDirectory = "private" // Private key directory in assets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityRsaBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set up public key selection
        mBinding.btnSelectPublicKey.setOnClickListener {
            showPublicKeySelectionDialog()
        }

        // Set up private key selection
        mBinding.btnSelectPrivateKey.setOnClickListener {
            showPrivateKeySelectionDialog()
        }

        mBinding.btnEncrypt.setOnClickListener {
            val textToEncrypt = mBinding.inputText.text.toString()
            if (textToEncrypt.isNotEmpty() && ::publicKey.isInitialized) {
                encrypted = encrypt(textToEncrypt, publicKey)
                mBinding.encryptedText.text = Base64.encodeToString(encrypted, Base64.DEFAULT)
            }
        }

        mBinding.btnDecrypt.setOnClickListener {
            encrypted?.let {
                if (::privateKey.isInitialized) {
                    try {
                        // 尝试解密
                        val decrypted = decrypt(it, privateKey)
                        mBinding.decryptedText.text = decrypted
                    } catch (e: IllegalArgumentException) {
                        // 捕获到解密失败时提示用户
                        Toast.makeText(this, "秘钥错误，请选择正确的私钥", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // 其他未知错误处理
                        Toast.makeText(this, "解密失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please select a private key first", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Show a dialog to select public key
    private fun showPublicKeySelectionDialog() {
        // Get list of public key files from assets/public directory
        val publicKeyFiles = assets.list(publicKeyDirectory)

        publicKeyFiles?.let { files ->
            // Show a dialog to let user select a public key file
            AlertDialog.Builder(this)
                .setTitle("Select Public Key")
                .setItems(files) { _, which ->
                    val selectedFile = files[which]
                    loadSelectedPublicKey(selectedFile)
                }
                .show()
        }
    }

    // Show a dialog to select private key
    private fun showPrivateKeySelectionDialog() {
        // Get list of private key files from assets/private directory
        val privateKeyFiles = assets.list(privateKeyDirectory)

        privateKeyFiles?.let { files ->
            // Show a dialog to let user select a private key file
            AlertDialog.Builder(this)
                .setTitle("Select Private Key")
                .setItems(files) { _, which ->
                    val selectedFile = files[which]
                    loadSelectedPrivateKey(selectedFile)
                }
                .show()
        }
    }

    // Load selected public key from assets/public directory
    private fun loadSelectedPublicKey(filename: String) {
        try {
            // Load the selected public key from assets
            val keyBytes = assets.open("$publicKeyDirectory/$filename").readBytes()
            publicKey = loadPublicKeyFromBytes(keyBytes)
            Toast.makeText(this, "Public key $filename loaded successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load public key", Toast.LENGTH_SHORT).show()
        }
    }

    // Load selected private key from assets/private directory
    private fun loadSelectedPrivateKey(filename: String) {
        try {
            // Load the selected private key from assets
            val keyBytes = assets.open("$privateKeyDirectory/$filename").readBytes()
            privateKey = loadPrivateKeyFromBytes(keyBytes)
            Toast.makeText(this, "Private key $filename loaded successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load private key", Toast.LENGTH_SHORT).show()
        }
    }

    // 加载公钥并解析
    private fun loadPublicKeyFromBytes(keyBytes: ByteArray): PublicKey {
        try {
            // 将字节数组转换为字符串
            var keyString = String(keyBytes)

            // 移除 PEM 文件中的头尾标志
            keyString = keyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s+".toRegex(), "")  // 移除所有空白字符

            // 对 Base64 编码的内容进行解码
            val decodedKey = Base64.decode(keyString, Base64.DEFAULT)

            // 使用 X509EncodedKeySpec 来生成公钥
            val keySpec = X509EncodedKeySpec(decodedKey)
            val keyFactory = KeyFactory.getInstance("RSA")
            return keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load public key", e)
        }
    }

    // 加载私钥并解析
    private fun loadPrivateKeyFromBytes(keyBytes: ByteArray): PrivateKey {
        try {
            // 将字节数组转换为字符串
            var keyString = String(keyBytes)

            // 移除 PEM 文件中的头尾标志
            keyString = keyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")  // 移除所有空白字符

            // 对 Base64 编码的内容进行解码
            val decodedKey = Base64.decode(keyString, Base64.DEFAULT)

            // 使用 PKCS8EncodedKeySpec 来生成私钥
            val keySpec = PKCS8EncodedKeySpec(decodedKey)
            val keyFactory = KeyFactory.getInstance("RSA")
            return keyFactory.generatePrivate(keySpec)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load private key", e)
        }
    }

    private fun encrypt(text: String, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(text.toByteArray())
    }

    private fun decrypt(encrypted: ByteArray, privateKey: PrivateKey): String {
        return try {
            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            // Perform decryption
            String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            // 捕获异常，并提示“秘钥错误”
            throw IllegalArgumentException("Failed to decrypt, possibly wrong private key", e)
        }
    }
}

