import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.jogamp.jogl:jogl-all-main:2.5.0")
            implementation("org.jogamp.gluegen:gluegen-rt-main:2.5.0")
//            val currentOS = DefaultNativePlatform.getCurrentOperatingSystem()
//            when {
//                currentOS.isWindows -> {
//                    runtimeOnly("org.jogamp.jogl:jogl-all:2.4.0:natives-windows-amd64")
//                    runtimeOnly("org.jogamp.gluegen:gluegen-rt:2.4.0:natives-windows-amd64")
//                }
//                currentOS.isMacOsX -> {
//                    runtimeOnly("org.jogamp.jogl:jogl-all:2.4.0:natives-macosx-universal")
//                    runtimeOnly("org.jogamp.gluegen:gluegen-rt:2.4.0:natives-macosx-universal")
//                }
//                currentOS.isLinux -> {
//                    runtimeOnly("org.jogamp.jogl:jogl-all:2.4.0:natives-linux-amd64")
//                    runtimeOnly("org.jogamp.gluegen:gluegen-rt:2.4.0:natives-linux-amd64")
//                }
//            }
        }
    }
}


compose.desktop {
    application {
        mainClass = "ua.valeriishymchuk.lobmapeditor.MainKt"

        jvmArgs += listOf(
            "-Djogamp.gluegen.UseTempJarCache=false",
            "--add-opens=jogl.all/com.jogamp.opengl.util=ALL-UNNAMED",
            "--add-opens=jogl.all/com.jogamp.opengl.glu=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.java2d=ALL-UNNAMED"
        )
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ua.valeriishymchuk"
            packageVersion = "1.0.0"
        }
    }
}
