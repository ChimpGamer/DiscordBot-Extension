plugins {
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

allprojects {
    group = "nl.chimpgamer.networkmanager.extensions"
    version = "2.1.2-SNAPSHOT"

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

        maven("https://jitpack.io")

        maven("https://repo.codemc.org/repository/maven-public")

        maven("https://repo.networkmanager.xyz/repository/maven-public/") // NetworkManager repository
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        compileOnly("com.github.Carleslc:Simple-YAML:1.8.4")
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }

        processResources {
            expand("version" to project.version)
        }

        shadowJar {
            archiveFileName.set("SimpleChat-${project.name}-v${project.version}.jar")

            /*dependencies {
                include(dependency("net.kyori:.*"))
            }*/

            val shadedPackage = "nl.chimpgamer.networkmanager.shaded"
            val libPackage = "nl.chimpgamer.networkmanager.lib"
            //relocate("net.kyori", "$shadedPackage.kyori")
            relocate("kotlin", "$libPackage.kotlin")
            relocate("org.simpleyaml", "$libPackage.simpleyaml")
        }

        build {
            dependsOn(shadowJar)
        }
    }
}

tasks {
    jar {
        enabled = false
    }
}