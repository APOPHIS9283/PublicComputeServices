import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sekret)
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.parcelize)
}

val tagName = "1.0.6"
val tagCode = 106

android {
    namespace = "com.kieronquinn.app.pcs"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.kieronquinn.app.pcs"
        minSdk = 34
        targetSdk = 36
        versionCode = tagCode
        versionName = tagName

        testInstrumentationRunner = providers.gradleProperty("pcsTestRunner")
            .getOrElse("androidx.test.runner.AndroidJUnitRunner")

        buildConfigField("String", "TAG_NAME", "\"${tagName}\"")
    }

    buildTypes {
        debug {
            // Uses default debug keystore automatically
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        aidl = true
    }

    sourceSets {
        getByName("main") {
            jniLibs.setSrcDirs(listOf("src/main/jniLibs-official"))
        }
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.packaging.jniLibs.excludes.add("/lib/x86/*.so")
        variant.packaging.jniLibs.excludes.add("/lib/x86_64/*.so")
    }
    onVariants { variant ->
        variant.packaging.jniLibs.keepDebugSymbols.add("**/libsekret.so")
    }
}

sekret {
    properties {
        enabled.set(true)
        packageName.set("com.kieronquinn.app.pcs.sekret")
        encryptionKey.set("")
    }
}

tasks.matching {
    it.name in listOf(
        "createSekretNativeBinary",
        "copySekretNativeBinary",
        "createAndCopySekretNativeBinary"
    )
}.configureEach {
    enabled = false
}

val grpcVersion = "1.74.0"
val protobufVersion = "4.32.0"
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
            task.plugins {
                id("grpc") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation(libs.androidx.core.ktx)
    compileOnly(libs.xposed)
    implementation(libs.okhttp)
    implementation(libs.grpc.binder)
    implementation(libs.grpc.android)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.protobuf.lite)
    implementation(libs.protobuf.java)
    implementation(libs.work)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.compose.preferences)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    implementation(libs.libsu.core)
    implementation(libs.dexkit)
    implementation(libs.tink)
    implementation(libs.annotatedText)
    implementation(libs.cardShape)
    implementation(libs.material.motion.compose.core)
    implementation(libs.accompanist.permissions)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.markwon)
    implementation(libs.markwon.tables)
}
