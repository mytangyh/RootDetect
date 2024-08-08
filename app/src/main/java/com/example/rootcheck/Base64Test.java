package com.example.rootcheck;

import android.util.Base64;
import android.util.Log;

public class Base64Test {

    public String byteToBase64(byte[] byteArray) {
        String returnString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.i("encription", returnString);
        return returnString;
    }

    public byte[] base64ToByte(String str) {

        Log.i("encription", str);
        byte[] returnbyteArray = Base64.decode(str, Base64.DEFAULT);



        return returnbyteArray;
    }
}
