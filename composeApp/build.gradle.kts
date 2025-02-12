import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0" // 必须添加
}

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopMain by getting
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // 最新版本
            implementation("com.squareup.okio:okio:3.2.0")
        }
    }
}


compose.desktop {
    application {
        mainClass = "MainKt"

//        buildTypes.release.proguard {
//            configurationFiles.from(project.file("compose-desktop.pro"))
//        }
        nativeDistributions {
            modules("java.instrument", "java.net.http", "jdk.unsupported")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project_v1.0"
            packageVersion = "1.0.0"
        }
    }
}


//tasks.withType<Jar> {
//    manifest {
//        attributes["Main-Class"] = "MainKt"
//    }
////    configurations.map {   if (it.dependencies) it else zipTree(it)}
////    duplicatesStrategy = DuplicatesStrategy.INCLUDE
//}
