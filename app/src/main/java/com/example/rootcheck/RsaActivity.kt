package com.example.rootcheck

import android.os.Bundle
import android.util.Base64
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rootcheck.databinding.ActivityRsaBinding
import java.io.BufferedReader
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

        // Load keys from assets
        privateKey = loadPrivateKey("private_key1.pem")
        publicKey = loadPublicKey("public_key1.pem")

        mBinding.btnEncrypt.setOnClickListener {
            val textToEncrypt = mBinding.inputText.text.toString()
            if (textToEncrypt.isNotEmpty()) {
                encrypted = encrypt(textToEncrypt, publicKey)
                mBinding.encryptedText.text = Base64.encodeToString(encrypted, Base64.DEFAULT)
            }
        }

        mBinding.btnDecrypt.setOnClickListener {
            encrypted?.let {
                val decrypted = decrypt(it, privateKey)
                mBinding.decryptedText.text = decrypted
            }
        }
    }

    private fun loadPublicKey(fileName: String): PublicKey {
        val key = assets.open(fileName).bufferedReader().use(BufferedReader::readText)
        val publicKeyPEM = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        val encoded = Base64.decode(publicKeyPEM, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(encoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    private fun loadPrivateKey(fileName: String): PrivateKey {
        val key = assets.open(fileName).bufferedReader().use(BufferedReader::readText)
        val privateKeyPEM = key
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val encoded = Base64.decode(privateKeyPEM, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(encoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }

    private fun encrypt(data: String, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data.toByteArray())
    }

    private fun decrypt(data: ByteArray, privateKey: PrivateKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return String(cipher.doFinal(data))
    }
}