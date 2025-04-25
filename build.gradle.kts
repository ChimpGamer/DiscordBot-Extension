import java.text.SimpleDateFormat
import java.util.*

plugins {
    kotlin("jvm") version "1.9.25"
    //`maven-publish`
    id("io.github.goooler.shadow") version "8.1.7"
}

allprojects {
    group = "nl.chimpgamer.networkmanager.extensions"
    version = "1.8.8"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("io.github.goooler.shadow")
    }

    repositories {
        maven("https://jitpack.io") // For RedisBungee

        maven("https://repo.networkmanager.xyz/repository/maven-public/") // NetworkManager repository
    }

    dependencies {
        compileOnly(kotlin("stdlib"))

        compileOnly("com.github.ProxioDev.ValioBungee:RedisBungee-API:0.11.4")

        compileOnly("nl.chimpgamer.networkmanager:api:2.17.4")
    }

    tasks {
        compileJava {
            sourceCompatibility = "11"
        }
        compileTestJava {
            sourceCompatibility = "11"
        }
        compileKotlin {
            kotlinOptions.jvmTarget = "11"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "11"
        }

        processResources {
            val buildNumber = System.getenv("BUILD_NUMBER") ?: System.getenv("GITHUB_RUN_NUMBER ") ?: "SNAPSHOT"
            expand("version" to project.version, "buildDate" to getDate(), "buildNumber" to buildNumber)
        }

        shadowJar {
            exclude("natives/**")     // ~2 MB
            exclude("com/sun/jna/**") // ~1 MB
            exclude("com/google/crypto/tink/**") // ~2 MB
            exclude("com/google/gson/**") // ~300 KB
            exclude("com/google/protobuf/**") // ~2 MB
            exclude("google/protobuf/**")
            exclude("club/minnced/opus/util/*")
            exclude("tomp2p/opuswrapper/*")

            val shadedPackage = "nl.chimpgamer.networkmanager.shaded"
            val libPackage = "nl.chimpgamer.networkmanager.lib"

            relocate("kotlin", "$shadedPackage.kotlin")
            relocate("org.incendo.cloud", "$libPackage.cloud")
            relocate("com.fasterxml.jackson", "$libPackage.jackson")
            relocate("net.dv8tion.jda", "$shadedPackage.jda")
            relocate("dev.minn.jda.ktx", "$shadedPackage.jda.ktx")
            relocate("dev.dejvokep.boostedyaml", "$libPackage.boostedyaml")
            relocate("okhttp3", "$shadedPackage.okhttp3")
            relocate("okio", "$shadedPackage.okio")
            relocate("com.neovisionaries.ws", "$shadedPackage.nv-websocket-client")
            relocate("org.apache.commons.collections4", "$shadedPackage.commons-collections4")
            relocate("gnu.trove", "$shadedPackage.trove")
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

fun getDate(): String {
    return SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date())
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    jar {
        enabled = false
    }
}