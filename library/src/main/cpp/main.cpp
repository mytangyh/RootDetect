// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("main");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("main")
//      }
//    }

#include <jni.h>


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_lib_Native_00024Companion_getNativeString(JNIEnv *env, jobject thiz) {
    // TODO: implement getNativeString()
    return env->NewStringUTF("Hello from C++!");
}