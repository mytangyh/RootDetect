package com.example.lib

class Native {
    companion object {
        init {
            System.loadLibrary("frida_detect")
        }

        external fun checkFrida():Boolean
    }
}