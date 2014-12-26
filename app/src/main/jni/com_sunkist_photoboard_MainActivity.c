#include "com_sunkist_photoboard_MainActivity.h"

JNIEXPORT jstring JNICALL Java_com_sunkist_photoboard_MainActivity_getStringFromNative (JNIEnv * env, jobject obj) {
    return (*env)->NewStringUTF(env, "Hello from jni~!!!");
}

