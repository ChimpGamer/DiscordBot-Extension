import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    base
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }

    maven { url = uri("https://jitpack.io") }

    maven { url = uri("http://repo.md-5.net/content/repositories/snapshots/") }

    maven { url = uri("https://repo.codemc.org/repository/maven-public") }

    maven { url = uri("http://repo.maven.apache.org/maven2") }
}

dependencies {
    implementation("net.kyori:text-api:3.0.2")
    implementation("com.jagrosh:jda-utilities-command:3.0.4")
    implementation("net.md-5:bungeecord-api:1.15-SNAPSHOT")
    implementation("nl.chimpgamer.networkmanager:api:2.8.9-SNAPSHOT")
    implementation("nl.chimpgamer.networkmanager:bungeecord:2.8.9-SNAPSHOT") {
        exclude("org.bstats:bstats-bungeecord:1.7")
    }
    implementation("net.dv8tion:JDA:4.2.0_168")
    implementation("com.github.Carleslc:Simple-YAML:1.4.1")
    compileOnly("com.imaginarycode.minecraft:RedisBungee:0.3.8-SNAPSHOT")
    implementation(kotlin("stdlib-jdk8"))
}

group = "nl.chimpgamer.networkmanager.extensions"
version = "1.3.4-SNAPSHOT"

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    processResources {
        val tokens = mapOf("version" to project.version)
        from(sourceSets["main"].resources.srcDirs) {
            filter<ReplaceTokens>("tokens" to tokens)
        }
    }

    shadowJar {
        archiveFileName.set("${project.name}-v${project.version}.jar")

        dependencies {
            include(dependency("nl.chimpgamer.networkmanager.extensions:.*"))
            include(dependency("com.jagrosh:.*"))
        }

        relocate("kotlin", "nl.chimpgamer.networkmanager.lib.kotlin")
        relocate("org.simpleyaml", "nl.chimpgamer.networkmanager.lib.simpleyaml")
        relocate("org.eclipse.jetty", "nl.chimpgamer.networkmanager.lib.jetty")
        relocate("javax.servlet", "nl.chimpgamer.networkmanager.lib.javax.servlet")
        relocate("com.google.gson", "nl.chimpgamer.networkmanager.lib.gson")
        relocate("com.jagrosh.jdautilities", "nl.chimpgamer.networkmanager.shaded.com.jagrosh.jdautilities")
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}