package com.example.rootcheck

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher

object KeyStoreUtil {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "MyAppKey"
    private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"

    // 创建密钥对
    fun createKeys() {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        }.build()

        keyPairGenerator.initialize(keyGenParameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    // 加密信息
    fun encryptData(data: String): ByteArray? {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
        val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }

        return cipher.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    // 解密信息
    fun decryptData(encryptedData: ByteArray): String {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
        val privateKey = keyStore.getKey(KEY_ALIAS, null)

        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, privateKey)
        }

        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData, Charsets.UTF_8)
    }
}