import dev.namelessnanashi.flipperzero.buildlogic.ApkConfig

// The multiplatform library plugin has no externalNativeBuild, so the mfkey32 JNI library
// is built here and pulled into :components:nfc:tools:impl.
plugins {
    id("com.android.library")
}

android {
    namespace = "dev.namelessnanashi.flipperzero.nfc.tools.jni"
    compileSdk = ApkConfig.COMPILE_SDK_VERSION
    ndkVersion = "29.0.14206865"

    defaultConfig {
        minSdk = ApkConfig.MIN_SDK_VERSION
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    externalNativeBuild {
        cmake {
            // Sources and the nfc-tools submodule stay in the impl module
            path = file("../impl/src/main/cpp/CMakeLists.txt")
            version = "4.1.2"
        }
    }
}
