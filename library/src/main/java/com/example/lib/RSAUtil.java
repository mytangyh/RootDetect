package com.example.lib;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Author   : tangyinghao@myhexin.com
 * Time     : 2025/02/20
 * Desc     : RSA加解密工具类 java服务端版本
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class RSAUtil {

    // 内部工具类，用于截取字节数组的一部分
    private static class ArrayUtils {
        /**
         * 截取字节数组的一部分。
         *
         * @param array             原始字节数组
         * @param startIndexInclusive 起始索引（包含）
         * @param endIndexExclusive   结束索引（不包含）
         * @return 截取的子数组
         */
        public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
            if (array == null) {
                return null;
            }
            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return new byte[0];
            }

            byte[] subarray = new byte[newSize];
            System.arraycopy(array, startIndexInclusive, subarray, 0, Math.min(array.length - startIndexInclusive, newSize));
            return subarray;
        }
    }

    // 内部工具类，用于Base64解码 (MIME)
    private static class Base64Wrapper {
        /**
         * 使用MIME解码器对Base64编码的字符串进行解码。
         *
         * @param str Base64编码的字符串
         * @return 解码后的字节数组
         */
        public static byte[] decodeBase64(String str) {
            return Base64.getMimeDecoder().decode(str);
        }
    }

    // 默认字符集
    public static final String DEFAULT_CHARSET = "UTF-8";
    // RSA算法名称
    public static final String ALGORITHM_RSA = "RSA";
    // 签名算法
    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";
    // 公钥在Map中的键名
    public static final String RSA_KEY_PUBLIC = "RSA_KEY_PUBLIC";
    // 私钥在Map中的键名
    public static final String RSA_KEY_PRIVATE = "RSA_KEY_PRIVATE";


    private static final Logger logger = Logger.getLogger(RSAUtil.class.getName());

    // 私有构造函数，防止工具类被实例化
    private RSAUtil() {
    }

    /**
     * 使用私钥对数据进行签名。
     *
     * @param privateKey Base64编码的私钥字符串
     * @param inputstr   待签名的数据
     * @return Base64编码的签名字符串
     * @throws RuntimeException 如果签名过程中发生错误
     */
    public static String sign(String privateKey, String inputstr) {
        // 加载私钥
        RSAPrivateKey key = loadPrivateKey(privateKey);
        if (key == null) {
            throw new RuntimeException("加密私钥为空, 请设置");
        }

        try {
            // 获取签名实例
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            // 初始化签名对象
            signature.initSign(key);
            // 更新待签名的数据
            signature.update(inputstr.getBytes(DEFAULT_CHARSET));
            // 生成签名并进行Base64编码
            return Base64.getMimeEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "签名生成异常", e);
            throw new RuntimeException("生成签名异常！");
        }
    }

    /**
     * 使用公钥验证签名。
     *
     * @param sign      Base64编码的签名字符串
     * @param publickey Base64编码的公钥字符串
     * @param inputstr  原始数据
     * @return 签名是否有效
     * @throws RuntimeException 如果验签过程中发生错误
     */
    public static boolean authenticate(String sign, String publickey, String inputstr) {
        // 加载公钥
        RSAPublicKey key = loadPublicKey(publickey);
        if (key == null) {
            throw new RuntimeException("加密公钥为空, 请设置");
        }

        try {
            // 获取签名实例
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            // 初始化验签对象
            signature.initVerify(key);
            // 更新原始数据
            signature.update(inputstr.getBytes(DEFAULT_CHARSET));
            // 验证签名
            return signature.verify(Base64.getMimeDecoder().decode(sign));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "验签异常", e);
            throw new RuntimeException("验签异常！");
        }
    }

    /**
     * 使用私钥加密数据。
     *
     * @param privateKey Base64编码的私钥字符串
     * @param inputstr   待加密的数据
     * @return Base64编码的加密后的数据
     * @throws RuntimeException 如果加密过程中发生错误
     */
    public static String encodeByPrivateKey(String privateKey, String inputstr) {
        // 加载私钥
        RSAPrivateKey key = loadPrivateKey(privateKey);
        if (key == null) {
            throw new RuntimeException("加密私钥为空, 请设置");
        }

        try {
            // 获取Cipher实例
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            // 初始化Cipher为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, key);
            // 加密数据并进行Base64编码
            byte[] output = cipher.doFinal(inputstr.getBytes(DEFAULT_CHARSET));
            return Base64.getMimeEncoder().encodeToString(output);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无此加密算法");
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("填充机制不可用,请检查");
        } catch (InvalidKeyException e) {
            throw new RuntimeException("加密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("明文长度非法");
        } catch (BadPaddingException e) {
            throw new RuntimeException("明文数据已损坏");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("不支持的编码格式");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "RSA私钥加密异常", e);
            throw new RuntimeException("RSA私钥加密异常");
        }
    }

    /**
     * 加载Base64编码的私钥。
     *
     * @param privateKey Base64编码的私钥字符串
     * @return RSAPrivateKey对象
     * @throws RuntimeException 如果加载过程中发生错误
     */
    private static RSAPrivateKey loadPrivateKey(String privateKey) {
        try {
            // 解码Base64编码的私钥
            byte[] buffer = Base64.getMimeDecoder().decode(privateKey.replace("\r\n", ""));
            // 创建PKCS8EncodedKeySpec对象
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            // 获取KeyFactory实例
            KeyFactory factory = KeyFactory.getInstance(ALGORITHM_RSA);
            // 生成私钥对象
            return (RSAPrivateKey) factory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("私钥非法");
        } catch (NullPointerException e) {
            throw new RuntimeException("私钥数据为空");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "RSA加载私钥异常", e);
            throw new RuntimeException("RSA加载私钥异常");
        }
    }

    /**
     * 使用私钥解密数据。
     *
     * @param privateKey Base64编码的私钥字符串
     * @param sign       Base64编码的加密数据
     * @return 解密后的原始数据
     * @throws RuntimeException 如果解密过程中发生错误
     */
    public static String decodeByPrivateKey(String privateKey, String sign) {
        // 加载私钥
        RSAPrivateKey key = loadPrivateKey(privateKey);
        if (key == null) {
            throw new RuntimeException("加密私钥为空, 请设置");
        }

        try {
            // 获取Cipher实例
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            // 初始化Cipher为解密模式
            cipher.init(Cipher.DECRYPT_MODE, key);
            // 解码Base64编码的数据
            byte[] buffer = Base64Wrapper.decodeBase64(sign);

            // 获取密钥长度（字节）
            int keyLength = key.getModulus().bitLength() / 8;
            System.out.println("密钥长度: " + keyLength);


            // 分块解密
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < buffer.length; i += keyLength) {
                byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(buffer, i, i + keyLength));
                sb.append(new String(doFinal));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "RSA私钥解密异常", e);
            throw new RuntimeException("RSA私钥解密异常: " + e.getMessage());
        }
    }

    /**
     * 使用公钥加密数据。
     *
     * @param publicKey Base64编码的公钥字符串
     * @param inputstr  待加密的数据
     * @return Base64编码的加密后的数据
     * @throws RuntimeException 如果加密过程中发生错误
     */
    public static String encodeByPublicKey(String publicKey, String inputstr) {
        // 加载公钥
        RSAPublicKey key = loadPublicKey(publicKey);
        if (key == null) {
            throw new RuntimeException("加密公钥为空, 请设置");
        }

        try {
            // 获取Cipher实例
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            // 初始化Cipher为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, key);

            // 计算最大块大小（密钥长度 - 11）  因为RSA加密算法要求,待加密内容不能超过这个长度
            int maxBlockSize = key.getModulus().bitLength() / 8 - 11;
            byte[] data = inputstr.getBytes(DEFAULT_CHARSET);

            // 分块加密
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int inputLen = data.length;
            int offSet = 0;
            byte[] cache;

            while (inputLen - offSet > 0) {
                if (inputLen - offSet > maxBlockSize) {
                    cache = cipher.doFinal(data, offSet, maxBlockSize);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                offSet += maxBlockSize;
            }

            byte[] encryptedData = out.toByteArray();
            out.close();
            return Base64.getMimeEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "RSA公钥加密异常", e);
            throw new RuntimeException("RSA公钥加密异常: " + e.getMessage());
        }
    }
    /**
     * 加载Base64编码的公钥。
     *
     * @param publicKey Base64编码的公钥字符串
     * @return RSAPublicKey对象
     * @throws RuntimeException 如果加载过程中发生错误
     */
    private static RSAPublicKey loadPublicKey(String publicKey) {
        try {
            // 解码Base64编码的公钥
            byte[] buffer = Base64.getMimeDecoder().decode(publicKey);
            // 获取KeyFactory实例
            KeyFactory factory = KeyFactory.getInstance(ALGORITHM_RSA);
            // 创建X509EncodedKeySpec对象
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            // 生成公钥对象
            return (RSAPublicKey) factory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("公钥非法");
        } catch (NullPointerException e) {
            throw new RuntimeException("公钥数据为空");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "RSA加载公钥异常", e);
            throw new RuntimeException("RSA加载公钥异常");
        }
    }

    /**
     * 生成RSA密钥对。
     *
     * @return 包含Base64编码的公钥和私钥的Map
     * @throws RuntimeException 如果生成密钥对过程中发生错误
     */
    public static Map<String, String> generateKeys() {
        try {
            // 获取KeyPairGenerator实例
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM_RSA);
            // 初始化密钥生成器（密钥长度为1024位）
            keyPairGen.initialize(1024, new SecureRandom());
            // 生成密钥对
            KeyPair keypair = keyPairGen.generateKeyPair();
            // 获取私钥和公钥
            RSAPrivateKey privateKey = (RSAPrivateKey) keypair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keypair.getPublic();

            // 将公钥和私钥进行Base64编码并存储在Map中
            Map<String, String> result = new HashMap<>(2);
            result.put(RSA_KEY_PRIVATE, Base64.getMimeEncoder().encodeToString(privateKey.getEncoded()));
            result.put(RSA_KEY_PUBLIC, Base64.getMimeEncoder().encodeToString(publicKey.getEncoded()));
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无此算法");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "生成RSA公私钥异常", e);
            throw new RuntimeException("生成RSA公私钥异常！");
        }
    }

    public static void main(String[] args) {
        try {
            // 1. 生成RSA密钥对
            System.out.println("=====生成RSA密钥对=====");
            Map<String, String> keyMap = generateKeys();
            String publicKey = keyMap.get(RSA_KEY_PUBLIC);
            String privateKey = keyMap.get(RSA_KEY_PRIVATE);
            System.out.println("公钥: " + publicKey);
            System.out.println("私钥: " + privateKey);

            // 2. 测试加密解密
            System.out.println("\n=====测试加密解密=====");
            String originalData = "Data to be encrypted";
            System.out.println("原始数据: " + originalData);

            // 使用公钥加密
            String encryptedData = encodeByPublicKey(publicKey, originalData);
            System.out.println("公钥加密后: " + encryptedData);

            // 使用私钥解密
            String decryptedData = decodeByPrivateKey(privateKey, encryptedData);
            System.out.println("私钥解密后: " + decryptedData);

            // 3. 测试签名验签
            System.out.println("\n=====测试签名验签=====");
            String signData = "Data to be signed";
            System.out.println("待签名数据: " + signData);

            // 生成签名
            String signature = sign(privateKey, signData);
            System.out.println("生成的签名: " + signature);

            // 验证签名
            boolean verified = authenticate(signature, publicKey, signData);
            System.out.println("签名验证结果: " + verified);

        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}