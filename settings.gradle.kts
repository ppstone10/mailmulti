//定义**Gradle 插件（如 Android Gradle Plugin、Kotlin 插件等）**从哪些仓库拉取
pluginManagement {
    repositories {
        //Google 提供的插件仓库，Android 插件通常从这里获取
        google()
        //Gradle 官方插件门户
        gradlePluginPortal()
        //Maven 中央仓库，最广泛使用
        mavenCentral()
    }
}

//控制整个项目中所有模块的依赖库（如 OkHttp、Ktor、Coroutines）从哪些仓库下载
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

//应用一个插件，名为 org.gradle.toolchains.foojay-resolver-convention，用于自动解析合适的 JDK 工具
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

//定义项目的名称，影响构建产物名称、生成的 .idea 工程文件夹名、日志显示、IDE识别等
rootProject.name = "mailmulti"

//若有子模块，用下列代码进行添加
//include(":app", ":shared")
