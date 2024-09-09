package com.example.rootcheck

import java.io.File
import java.util.Arrays.asList
import java.util.Base64


    fun main() {
        val baseFileName = "fileName"
        val encodedFileName = Base64.getEncoder()
            .encodeToString(baseFileName.toByteArray(Charsets.UTF_8))
            .replace("=", "")
        println(encodedFileName)

        val decodedPart = "Rzl3MEJBUUhFRkFBT0NBUQ"
        val base64Decoded = Base64.getDecoder().decode(decodedPart)
        println(String(base64Decoded, Charsets.UTF_8))
    }
