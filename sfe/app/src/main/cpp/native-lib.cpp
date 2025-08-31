#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "AegisKeys"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_aegis_sfe_security_SecureKeys_getRegistrationKey(JNIEnv *env, jobject /* this */) {
    // The registration key is stored as a C++ string
    // This makes it harder to find via simple string searches in the APK
    std::string registrationKey = "mi7un3g3e4nqj66hcpqbd60k2091otpctlaqgbarmg3p45qdroh";
    
    LOGI("Registration key accessed");
    
    return env->NewStringUTF(registrationKey.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_aegis_sfe_security_SecureKeys_getClientId(JNIEnv *env, jobject /* this */) {
    // Also store the client ID securely
    std::string clientId = "UCO_BANK_PROD_ANDROID";
    
    LOGI("Client ID accessed");
    
    return env->NewStringUTF(clientId.c_str());
}