plugins {
    //启用 Kotlin 跨平台开发支持（KMP）
    kotlin("multiplatform") version "1.9.23"
    //定义这是一个 Android 应用模块（不是库）
    id("com.android.application") version "8.5.0"
}

//配置 Kotlin Multiplatform 的目标平台、源码结构和依赖。
kotlin {
    jvm() // JVM平台（桌面/服务器）
    androidTarget() // Android 平台

    //配置源码文件夹（如 commonMain, jvmMain, androidMain）的依赖项：
    sourceSets {
        //所有平台共享逻辑
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            }
        }
        //JVM 特有逻辑
        val jvmMain by getting {
            dependencies {
                implementation("com.sun.mail:jakarta.mail:2.0.1")
                implementation("com.sun.activation:jakarta.activation:2.0.1")
            }
        }

        //Android 特有逻辑
        val androidMain by getting {
            dependencies {
                implementation("com.sun.mail:android-mail:1.6.7")
                implementation("com.sun.mail:android-activation:1.6.7")

                // ✅ 新增：支持运行 Activity 和 Compose（可选）
                implementation("androidx.activity:activity-ktx:1.9.0")
                implementation("androidx.activity:activity-compose:1.9.0")
                implementation("androidx.core:core-ktx:1.13.1")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

                // ✅ 如果不需要 Compose，可以不添加 Compose 的依赖
                implementation("androidx.compose.ui:ui:1.6.7")
                implementation("androidx.compose.foundation:foundation:1.6.7")
                implementation("androidx.compose.material3:material3:1.2.1")
                implementation("androidx.compose.runtime:runtime:1.6.7")
            }
        }
        //JVM 平台测试代码
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

//配置 Android 应用构建的核心参数
android {
    //控制包名、最小兼容 SDK、构建目标 SDK、版本号等
    namespace = "org.example.email"
    compileSdk = 34
    defaultConfig {
        // ✅ 设置 Android 应用 ID（包名）
        applicationId = "org.example.email"

        // ✅ 设置最低和目标 SDK 版本
        minSdk = 26
        targetSdk = 34

        // ✅ 设置版本号
        versionCode = 1
        versionName = "1.0"

        // ✅ 指定用于测试的 runner
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    //启用 Jetpack Compose UI，并指定编译器扩展版本
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    //设置 Java 版本为 17，以便用更现代的语法特性
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    //关闭 UI 动画，加快测试速度 、启用单元测试对 Android 资源访问支持
    testOptions {
        animationsDisabled = true
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    //打包过滤不必要的 LICENSE 等资源
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }

}

//添加顶层依赖（注意：你已经在各 sourceSets 添加了更合适的测试依赖，这里也可以写，但不常见）
dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    //implementation("junit:junit:4.13.1")
}
