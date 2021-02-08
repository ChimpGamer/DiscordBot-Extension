import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    base
    kotlin("jvm") version "1.4.10"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")

    maven("https://jitpack.io")

    maven("http://repo.md-5.net/content/repositories/snapshots/")

    maven("https://repo.codemc.org/repository/maven-public")

    maven("https://repo.maven.apache.org/maven2")
}

dependencies {
    implementation("com.jagrosh:jda-utilities-command:3.0.4")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.2-SNAPSHOT")
    //implementation("nl.chimpgamer.networkmanager:api:2.9.0-SNAPSHOT")
    implementation("nl.chimpgamer.networkmanager:bungeecord:2.9.4-SNAPSHOT") {
        exclude("org.bstats:bstats-bungeecord:1.7")
    }
    implementation("net.dv8tion:JDA:4.2.0_217")
    compileOnly("com.github.Carleslc:Simple-YAML:1.4.1")
    compileOnly("com.imaginarycode.minecraft:RedisBungee:0.3.8-SNAPSHOT")
    compileOnly(kotlin("stdlib-jdk8"))
}

group = "nl.chimpgamer.networkmanager.extensions"
version = "1.3.7-SNAPSHOT"

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