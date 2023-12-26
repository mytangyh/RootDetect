package com.example.lib

class Native {
    companion object {
        init {
            System.loadLibrary("main")
        }

        external fun getNativeString(): String
    }
}