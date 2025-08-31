# NDK Secure Storage Implementation Guide

## Overview

This document explains how the UCO Bank demo app securely stores sensitive keys using the Android NDK (Native Development Kit). By storing the registration key in native C++ code instead of Java/Kotlin, we make it significantly harder for attackers to extract these values from the APK.

## Architecture

### Components

1. **Native Library (`aegis_keys`)**: C++ code that stores the sensitive keys
2. **JNI Bridge (`SecureKeys.kt`)**: Kotlin object that loads the native library and provides access to the keys
3. **Application Integration**: The app retrieves keys at runtime from the native layer

### Security Benefits

- **Obfuscation**: Keys stored in native code are harder to find via simple string searches
- **Binary Protection**: Native code is compiled to machine code, making reverse engineering more difficult
- **Runtime Access**: Keys are only accessible when the native library is properly loaded

## Implementation Details

### 1. Build Configuration

The app's `build.gradle.kts` is configured to build native code:

```kotlin
android {
    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }
    
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
```

### 2. CMake Configuration

`CMakeLists.txt` defines how to build the native library:

```cmake
cmake_minimum_required(VERSION 3.22.1)
project("aegis_keys")

add_library(aegis_keys SHARED native-lib.cpp)
find_library(log-lib log)
target_link_libraries(aegis_keys ${log-lib})
```

### 3. Native C++ Code

The `native-lib.cpp` file contains the actual key storage:

```cpp
extern "C" JNIEXPORT jstring JNICALL
Java_com_aegis_sfe_security_SecureKeys_getRegistrationKey(JNIEnv *env, jobject) {
    std::string registrationKey = "your_secure_key_here";
    return env->NewStringUTF(registrationKey.c_str());
}
```

### 4. Kotlin Bridge

The `SecureKeys` object provides access to the native methods:

```kotlin
object SecureKeys {
    init {
        System.loadLibrary("aegis_keys")
    }
    
    external fun getRegistrationKey(): String
    external fun getClientId(): String
}
```

### 5. Application Usage

The app retrieves keys lazily when needed:

```kotlin
companion object {
    val CLIENT_ID: String by lazy { SecureKeys.getClientId() }
    val REGISTRATION_KEY: String by lazy { SecureKeys.getRegistrationKey() }
}
```

## Security Considerations

### What This Protects Against

1. **Basic APK Analysis**: Simple unzip and string searches won't reveal the keys
2. **Decompilation Tools**: Standard Java/Kotlin decompilers won't show the key values
3. **Automated Scanners**: Many security scanning tools focus on Java/Kotlin code

### What This Doesn't Protect Against

1. **Determined Attackers**: Native code can still be reverse engineered with tools like IDA Pro
2. **Runtime Attacks**: Keys can be intercepted when accessed from memory
3. **Rooted Devices**: Full device access allows various attack vectors

## Best Practices for Production

1. **Additional Obfuscation**: Consider using string obfuscation techniques in the C++ code
2. **Anti-Debugging**: Implement native anti-debugging checks
3. **Certificate Pinning**: Combine with certificate pinning for network security
4. **Code Signing**: Ensure the native library hasn't been tampered with
5. **Regular Key Rotation**: Implement a mechanism to rotate keys periodically

## Build Requirements

To build the app with NDK support:

1. Install Android NDK via Android Studio SDK Manager
2. Install CMake (version 3.22.1 or higher)
3. Sync and build the project

The native library will be automatically compiled for all supported ABIs.

## Troubleshooting

### Common Issues

1. **UnsatisfiedLinkError**: The native library failed to load
   - Ensure NDK is properly installed
   - Check that the library name matches in all references

2. **Build Failures**: CMake configuration errors
   - Verify CMake version compatibility
   - Check file paths in CMakeLists.txt

3. **Runtime Crashes**: Native method signature mismatches
   - Ensure JNI function names match the package and class structure
   - Verify parameter and return types

## Conclusion

This NDK implementation provides a significant security improvement over storing keys in plain Kotlin/Java code. While not impenetrable, it raises the bar for attackers and demonstrates security best practices for mobile applications handling sensitive data.