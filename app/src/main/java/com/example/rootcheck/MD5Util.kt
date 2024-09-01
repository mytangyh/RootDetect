package com.example.rootcheck

class MD5Util {
    companion object {
        val hexDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
        fun md5(str: String): String {
            val md = java.security.MessageDigest.getInstance("MD5")
            val array = md.digest(str.toByteArray())
            val sb = StringBuffer()
            for (b in array) {
                sb.append(Integer.toHexString((b.toInt() and 0xFF) or 0x100).substring(1, 3))
            }
            return sb.toString()
        }
    }
}