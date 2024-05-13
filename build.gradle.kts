plugins {
    kotlin("jvm") version "1.9.23"
    //`maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    group = "nl.chimpgamer.networkmanager.extensions"
    version = "1.8.3-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("com.github.johnrengelman.shadow")
    }

    repositories {
        mavenLocal()

        maven("https://jitpack.io") // For RedisBungee

        //maven("https://repo.networkmanager.xyz/repository/maven-public/") // NetworkManager repository
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))

        compileOnly("com.github.ProxioDev.redisbungee:RedisBungee-API:0.11.2")

        compileOnly("nl.chimpgamer.networkmanager:common-proxy:2.15.2-SNAPSHOT")
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
            relocate("cloud.commandframework", "$libPackage.cloud")
            relocate("com.fasterxml.jackson", "$libPackage.jackson")
            relocate("net.dv8tion.jda", "$shadedPackage.jda")
            relocate("dev.dejvokep.boostedyaml", "$libPackage.boostedyaml")
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