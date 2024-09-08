
import java.io.FileOutputStream
import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.rootcheck"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rootcheck"
        minSdk = 28
        targetSdk = 33
//        versionCode = 101
//        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
   buildFeatures {
        viewBinding = true
    }
    packagingOptions {
        // 排除原始 RSA 公钥文件，防止它们被打包到 APK 中
        resources.excludes.add("assets/public/*.pem")
    }
}
tasks.register("processRsaKeys") {
    doLast {
        val assetsDir = file("${project.rootDir}/app/src/main/assets")
        val publicDir = file("${assetsDir}/public")

        val rsaFiles = fileTree(publicDir).apply {
            include("*.pem")
        }

        val outputFile = file("${assetsDir}/encoded_rsa_keys.txt")
        val outputStream = FileOutputStream(outputFile)

        rsaFiles.forEach { rsaKeyFile ->
            var rsaKey = rsaKeyFile.readText()

            // 去除公钥的头尾标志
            rsaKey = rsaKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")  // 去除所有空白字符

            // 对文件名进行Base64编码并去除等号
            val encodedFileName = Base64.getEncoder()
                .encodeToString(rsaKeyFile.name.toByteArray(Charsets.UTF_8))
                .replace("=", "")  // 去除Base64编码的等号
            println("Encoded FileName: $encodedFileName")

            // 记录文件名的字符长度
            val fileNameLength = encodedFileName.length
            println("File Name Length: $fileNameLength")

            // 根据文件名的长度对公钥进行分段
            val keyParts = rsaKey.chunked(fileNameLength)

            // 将文件名字符插入到每段的中间位置
            val combinedParts = keyParts.mapIndexed { index, part ->
                val charToInsert = if (index < encodedFileName.length) {
                    encodedFileName[index].toString()
                } else {
                    ""
                }
                val middleIndex = part.length / 2
                part.substring(0, middleIndex) + charToInsert + part.substring(middleIndex)
            }

            // 对每一段进行Base64编码并去除等号，除了最后一行不编码
            val encodedParts = combinedParts.mapIndexed { index, part ->
                if (index != combinedParts.size - 1) {
                    Base64.getEncoder()
                        .encodeToString(part.toByteArray(Charsets.UTF_8))
                        .replace("=", "")  // 去除Base64编码的等号
                } else {
                    part  // 最后一段不编码
                }
            }

            // 将处理后的公钥部分写入文件
            encodedParts.forEach { part ->
                outputStream.write((part + "\n").toByteArray())
            }

            // 记录一个分隔符用于区分不同公钥文件
            outputStream.write("-----END RSA KEY-----\n".toByteArray())
        }

        outputStream.close()
        println("Processed and encoded RSA keys saved to encoded_rsa_keys.txt")

        // 读取并恢复原始文件名及公钥内容
        println("\nRestoring file names and public keys...")

        val inputFile = file("${assetsDir}/encoded_rsa_keys.txt")
        val lines = inputFile.readLines()

        var currentKey = StringBuilder()
        lines.forEach { line ->
            if (line == "-----END RSA KEY-----") {
                // 当遇到分隔符时恢复当前公钥文件
                val restoredKey = currentKey.toString()
                println("Restored Public Key:\n$restoredKey")
                currentKey.clear() // 清除以恢复下一个公钥
            } else {
                // 如果不是最后一行且Base64编码过，则进行解码
                val decodedPart = try {
                    Base64.getDecoder().decode(line).toString(Charsets.UTF_8)
                } catch (e: IllegalArgumentException) {
                    line // 最后一行或未编码部分直接返回原内容
                }
                currentKey.append(decodedPart)
            }
        }
    }
}


// 确保任务在编译前运行
tasks.named("preBuild").configure {
    dependsOn("processRsaKeys")
}
dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.1")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.lifecycle:lifecycle-process:2.8.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(project(":library"))
    implementation ("com.github.lzyzsd:jsbridge:1.0.4")
    implementation ("com.tencent:mmkv:1.3.2")

}

