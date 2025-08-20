import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain {
            repositories {
                google()
                mavenCentral()
                maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // для JetBrains Compose
                maven("https://jogamp.org/deployment/maven")
                maven("https://www.jetbrains.com/intellij-repository/releases/")
            }
            dependencies {
                implementation(compose.desktop.currentOs){
                    exclude(group = "org.jetbrains.compose.material")
                    exclude(group = "org.jetbrains.compose.material3")
                }
                implementation(libs.kotlinx.coroutinesSwing)

                // See https://github.com/JetBrains/Jewel/releases for the release notes
                implementation("org.jetbrains.jewel:jewel-int-ui-standalone:0.29.0-252.24604")

                // Optional, for custom decorated windows:
                implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window:0.29.0-252.24604")
                implementation("org.jetbrains.jewel:jewel-ui:0.29.0-252.24604")
                implementation("com.jetbrains.intellij.platform:icons:252.23892.439")

                implementation("org.jogamp.jogl:jogl-all-main:2.5.0")
                implementation("org.jogamp.gluegen:gluegen-rt-main:2.5.0")
                implementation("com.google.code.gson:gson:2.13.1")

                val voyagerVersion = "1.1.0-beta03"

                implementation("cafe.adriel.voyager:voyager-navigator:${voyagerVersion}")

                implementation("org.kodein.di:kodein-di:7.26.1")
                implementation("org.kodein.di:kodein-di-framework-compose:7.26.1")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

                implementation("io.konform:konform-jvm:0.11.1")
                implementation("com.twelvemonkeys.imageio:imageio-webp:3.12.0")
                implementation("org.joml:joml:1.10.8")
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
}


tasks.named<JavaExec>("hotRunJvm") {
    workingDir = File("run").apply { mkdirs() }
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
