plugins {
    kotlin("jvm") version "1.9.24"
    //`maven-publish`
    id("io.github.goooler.shadow") version "8.1.7"
}

allprojects {
    group = "nl.chimpgamer.networkmanager.extensions"
    version = "1.8.4"

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
        mavenLocal()

        maven("https://jitpack.io") // For RedisBungee

        //maven("https://repo.networkmanager.xyz/repository/maven-public/") // NetworkManager repository
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))

        compileOnly("com.github.ProxioDev.redisbungee:RedisBungee-API:0.11.2")

        compileOnly("nl.chimpgamer.networkmanager:common-proxy:2.16.0-SNAPSHOT")
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
            expand("version" to project.version)
        }

        shadowJar {
            archiveFileName.set("DiscordBot-${project.name}-v${project.version}.jar")

            val shadedPackage = "nl.chimpgamer.networkmanager.shaded"
            val libPackage = "nl.chimpgamer.networkmanager.lib"

            relocate("kotlin", "$libPackage.kotlin")
            relocate("org.incendo.cloud", "$libPackage.cloud")
            relocate("com.fasterxml.jackson", "$libPackage.jackson")
            relocate("net.dv8tion.jda", "$shadedPackage.jda")
            relocate("dev.minn.jda.ktx", "$shadedPackage.jda-ktx")
            relocate("dev.dejvokep.boostedyaml", "$libPackage.boostedyaml")
            relocate("okhttp3", "$shadedPackage.okhttp3")
            relocate("okio", "$shadedPackage.okio")
        }

        build {
            dependsOn(shadowJar)
        }
    }
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