plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    //id("io.github.slimjar") version "1.3.0"
}

repositories {
    mavenLocal()
    mavenCentral()

    //maven("https://oss.sonatype.org/content/repositories/snapshots")

    maven("https://jitpack.io/")

    maven("https://repo.md-5.net/content/repositories/snapshots/")

    maven("https://repo.papermc.io/repository/maven-public/")

    //maven("https://nexus.velocitypowered.com/repository/maven-public/")

    //maven("https://repo.codemc.org/repository/maven-public")

    // For slimjar
    maven("https://repo.glaremasters.me/repository/public/")

    // NetworkManager repositories
    //maven("https://repo.networkmanager.xyz/repository/maven-public/")
    /*maven {
        url = uri("https://repo.networkmanager.xyz/repository/maven-private/")
        credentials {
            username = project.property("NETWORKMANAGER_NEXUS_USERNAME").toString()
            password = project.property("NETWORKMANAGER_NEXUS_PASSWORD").toString()
        }
    }*/
}

dependencies {
    //compileOnly("io.github.slimjar:slimjar:1.2.7")
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("net.md-5:bungeecord-api:1.16-R0.4")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")

    compileOnly("nl.chimpgamer.networkmanager:common-proxy:2.14.5")

    /*slim("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains")
    }*/

    implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")

    implementation("net.dv8tion:JDA:5.0.0-beta.19") {
        exclude("club.minnced", "opus-java")
        /*exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains")
        exclude("com.google.code.findbugs")
        exclude("org.slf4j")*/
    }
    implementation("club.minnced:jda-ktx:0.11.0-beta.19") {
        /*exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
        exclude("org.jetbrains")*/
    }

    compileOnly("com.gitlab.ruany", "LiteBansAPI", "0.3.5")

    compileOnly("com.github.Carleslc:Simple-YAML:1.8.4")
    //compileOnly("com.imaginarycode.minecraft:RedisBungee:0.3.8-SNAPSHOT")
    compileOnly("com.github.ProxioDev.redisbungee:RedisBungee-API:0.11.2")
    compileOnly("com.github.ProxioDev.redisbungee:RedisBungee-Bungee:0.11.2")
    compileOnly("com.github.ProxioDev.redisbungee:RedisBungee-Velocity:0.11.2")

    compileOnly("cloud.commandframework:cloud-core:1.8.4")
    compileOnly("cloud.commandframework:cloud-annotations:1.8.4")

    //compileOnly("com.velocitypowered:velocity-api:3.1.0")
    //kapt("com.velocitypowered:velocity-api:3.1.0")
}

group = "nl.chimpgamer.networkmanager.extensions"
version = "1.7.1"

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
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
        archiveFileName.set("${project.name}-v${project.version}.jar")

        /*dependencies {
            include(dependency("nl.chimpgamer.networkmanager.extensions:.*"))
        }*/

        val shadedPackage = "nl.chimpgamer.networkmanager.shaded"
        val libPackage = "nl.chimpgamer.networkmanager.lib"
        relocate("io.github.slimjar", "$shadedPackage.slimjar")
        relocate("net.kyori", "$shadedPackage.kyori")

        relocate("kotlin", "$libPackage.kotlin")
        relocate("org.simpleyaml", "$libPackage.simpleyaml")
        relocate("cloud.commandframework", "$libPackage.cloud")
        relocate("com.fasterxml.jackson", "nl.chimpgamer.networkmanager.lib.jackson")
        relocate("net.dv8tion.jda", "nl.chimpgamer.networkmanager.lib.jda")
    }

    /*slimJar {
        shade = false
        relocate("net.dv8tion.jda", "nl.chimpgamer.networkmanager.lib.jda")
    }*/

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}