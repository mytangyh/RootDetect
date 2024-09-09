package com.example.rootcheck

import java.io.File
import java.util.Base64

class RsaKeyRestorer(private val encodedFilePath: String) {

    fun restoreKeyByFileName(fileName: String): String {
        val encodedFile = File(encodedFilePath)
        if (!encodedFile.exists()) {
            throw IllegalArgumentException("Encoded file not found: $encodedFilePath")
        }

        // 文件名不带后缀
        val baseFileName = fileName.substringBeforeLast(".")
        val encodedFileName = Base64.getEncoder()
            .encodeToString(baseFileName.toByteArray(Charsets.UTF_8))
            .replace("=", "")  // 去除等号，匹配编码时的处理

        println("encodedFileName:$encodedFileName")
        // 按结束符将文件划分为多个公钥列表
        val lines = encodedFile.readLines()
        val rsaKeyParts = mutableListOf<MutableList<String>>()
        var currentKeyParts = mutableListOf<String>()

        val endFlag = Base64.getEncoder()
            .encodeToString("@${fileName.length}@".toByteArray(Charsets.UTF_8))
            .replace("=", "")
        lines.forEach { line ->
            if (line.contains(endFlag)) {
                // 碰到结束符时，将当前的公钥部分保存并开始新的一部分
                currentKeyParts.add(line)
                rsaKeyParts.add(currentKeyParts)
                currentKeyParts = mutableListOf()
            } else {
                currentKeyParts.add(line)
            }
        }
        println("rsaKeyParts:$rsaKeyParts")

        // 依次处理每个公钥部分，找到与文件名匹配的部分
        rsaKeyParts.forEach { keyParts ->
            val lastLine = keyParts.last()
            println("lastLine:$lastLine")
            if (lastLine.contains(encodedFileName.reversed())) {
                // 找到匹配的公钥部分
                val restoredKey = StringBuilder()

                keyParts.forEachIndexed { index, part ->
                    var decodedPart = part

                    // 如果是最后一行，移除文件名部分
                    if (index == keyParts.size - 1) {
                        decodedPart = decodedPart.removePrefix(encodedFileName.reversed()).removeSuffix(endFlag)
                        decodedPart = decodedPart.removeInsertedFileNameChars(encodedFileName.length)
                        println("lastLine decodedPart:$decodedPart")
                        restoredKey.append(decodedPart)
                        return@forEachIndexed
                    }
                    // 如果是奇数行，翻转字符串
                    if (index % 2 == 0) {
                        decodedPart = decodedPart.reversed()
                    }else{
                        // 如果是偶数行，前后反转字符串
                        val mid = decodedPart.length / 2

                        // 将字符串分为两部分
                        val firstHalf = decodedPart.substring(decodedPart.length - mid)  // 后半部分原来是前半部分
                        val secondHalf = decodedPart.substring(0, decodedPart.length - mid)  // 前半部分原来是后半部分
                        decodedPart = firstHalf+secondHalf
                    }

                    // 尝试进行Base64解码
                    decodedPart = try {
                        val base64Decoded = Base64.getDecoder().decode(decodedPart)
                        String(base64Decoded, Charsets.UTF_8)
                    } catch (e: IllegalArgumentException) {
                        // 可能是最后一行未编码部分
                        decodedPart
                    }
                    println("decodedPart de:$decodedPart")

                    // 去除插入的文件名字符
                    decodedPart = decodedPart.removeInsertedFileNameChars(encodedFileName.length)
                    println("decodedPart remove:$decodedPart")

                    restoredKey.append(decodedPart)
                }

                return restoredKey.toString()
            }
        }

        throw IllegalArgumentException("Public key not found for file name: $fileName")
    }

    // 扩展函数用于移除插入的文件名字符
    private fun String.removeInsertedFileNameChars(fileNameLength: Int): String {
        return this.chunked(fileNameLength).joinToString("") { chunk ->
            if (chunk.length > 1) {
                val middleIndex = chunk.length / 2
                chunk.removeRange(middleIndex, middleIndex + 1) // 移除插入字符
            } else {
                chunk
            }
        }
    }
}

fun main() {
    val args= listOf("public_key1.pem", "public_key2.pem", "public_key3.pem")
    if (args.isEmpty()) {
        println("Usage: RsaKeyRestorer <fileName>")
        return
    }

    val encodedFilePath = "D:\\Code\\gitRepo\\RootDetect\\app\\src\\main\\assets\\encoded_rsa_keys.txt"  // 替换为实际的路径
    val restorer = RsaKeyRestorer(encodedFilePath)

    for (arg in args) {
        try {
            val restoredKey = restorer.restoreKeyByFileName(arg)
            println("Restored Public Key for $arg:\n$restoredKey")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

}
