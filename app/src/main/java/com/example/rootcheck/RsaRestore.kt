package com.example.rootcheck

import java.io.File
import java.util.Base64

class RsaKeyRestorer(private val encodedFilePath: String) {

    // 新的方法，输入文件名列表，输出文件名与对应公钥的映射
    fun restoreKeysByFileNames(fileNames: List<String>): Map<String, String> {
        val encodedFile = File(encodedFilePath)
        if (!encodedFile.exists()) {
            throw IllegalArgumentException("Encoded file not found: $encodedFilePath")
        }

        // 为每个文件名生成对应的编码文件名和结束标志
        val fileNameDataMap = mutableMapOf<String, FileNameData>()
        val endFlagToFileName = mutableMapOf<String, String>()
        fileNames.forEach { fileName ->
            val baseFileName = fileName.substringBeforeLast(".")
            val encodedFileName = Base64.getEncoder()
                .encodeToString(baseFileName.toByteArray(Charsets.UTF_8))
                .replace("=", "")
            val endFlag = Base64.getEncoder()
                .encodeToString("@${encodedFileName.reversed()+encodedFileName.length}@".toByteArray(Charsets.UTF_8))
                .replace("=", "")

            fileNameDataMap[fileName] = FileNameData(
                fileName = fileName,
                baseFileName = baseFileName,
                encodedFileName = encodedFileName,
                endFlag = endFlag
            )
            endFlagToFileName[endFlag] = fileName
        }

        // 按行读取文件，基于结束标志将文件内容拆分为对应的公钥部分
        val lines = encodedFile.readLines()
        val filenameToKeyParts = mutableMapOf<String, MutableList<String>>()
        var currentKeyParts = mutableListOf<String>()

        lines.forEach { line ->
            currentKeyParts.add(line)
            // 检查当前行是否包含任何结束标志
            val matchingEndFlags = endFlagToFileName.keys.filter { endFlag ->
                line.contains(endFlag)
            }

            if (matchingEndFlags.size == 1) {
                val endFlag = matchingEndFlags.first()
                val fileName = endFlagToFileName[endFlag]!!

                // 保存当前的公钥部分到对应的文件名
                filenameToKeyParts[fileName] = currentKeyParts
                currentKeyParts = mutableListOf()
            } else if (matchingEndFlags.size > 1) {
                throw IllegalStateException("Multiple matching end flags found in line: $line")
            }
            // 如果没有匹配的结束标志，则继续收集公钥部分
        }

        // 处理每个文件名对应的公钥部分，生成公钥
        val restoredKeys = mutableMapOf<String, String>()
        fileNames.forEach { fileName ->
            val keyParts = filenameToKeyParts[fileName]
                ?: throw IllegalArgumentException("Public key not found for file name: $fileName")
            val data = fileNameDataMap[fileName]!!
            val restoredKey = processKeyParts(data, keyParts)
            restoredKeys[fileName] = restoredKey
        }

        return restoredKeys
    }

    // 处理公钥部分，生成公钥字符串
    private fun processKeyParts(data: FileNameData, keyParts: List<String>): String {
        val encodedFileName = data.encodedFileName
        val endFlag = data.endFlag
        val restoredKey = StringBuilder()

        keyParts.forEachIndexed { index, part ->
            var decodedPart = part

            if (index == keyParts.size - 1) {
                // 最后一行，移除文件名部分
                decodedPart = decodedPart.removePrefix(endFlag)
                decodedPart = decodedPart.removeInsertedFileNameChars(decodedPart.length)
                restoredKey.append(decodedPart)
                return@forEachIndexed
            }

            if (index % 2 == 0) {
                // 奇数行，翻转字符串
                decodedPart = decodedPart.reversed()
            } else {
                // 偶数行，前后反转字符串
                val mid = decodedPart.length / 2
                val firstHalf = decodedPart.substring(decodedPart.length - mid)
                val secondHalf = decodedPart.substring(0, decodedPart.length - mid)
                decodedPart = firstHalf + secondHalf
            }

            // 尝试进行Base64解码
            decodedPart = try {
                val base64Decoded = Base64.getDecoder().decode(decodedPart)
                String(base64Decoded, Charsets.UTF_8)
            } catch (e: IllegalArgumentException) {
                decodedPart // 可能是未编码部分
            }

            // 去除插入的文件名字符
            decodedPart = decodedPart.removeInsertedFileNameChars(encodedFileName.length)
            restoredKey.append(decodedPart)
        }

        return restoredKey.toString()
    }

    // 扩展函数，用于移除插入的文件名字符
    private fun String.removeInsertedFileNameChars(fileNameLength: Int): String {
        return this.chunked(fileNameLength).joinToString("") { chunk ->
            if (chunk.length > 1) {
                val middleIndex = chunk.length / 2
                chunk.removeRange(middleIndex, middleIndex + 1)
            } else {
                chunk
            }
        }
    }

    // 用于存储每个文件名的相关数据
    data class FileNameData(
        val fileName: String,
        val baseFileName: String,
        val encodedFileName: String,
        val endFlag: String
    )
}


fun main() {

    val fileNames= listOf("public_key1.pem", "zhfx_key.pem", "hexin_pub.pem")
    val encodedFilePath = "/media/tangyh/f/AndroidProject/RootDetect/app/src/main/assets/encoded_rsa_keys.txt"  // 替换为实际的路径
    val restorer = RsaKeyRestorer(encodedFilePath)
    val keys = restorer.restoreKeysByFileNames(fileNames)
    keys.forEach { (fileName, key) ->
        println("File: $fileName, Restored Key: $key")
    }


}
