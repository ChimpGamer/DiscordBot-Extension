plugins {
    kotlin("jvm") version "1.6.10"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.github.slimjar") version "1.3.0"
}

repositories {
    mavenLocal()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")

    maven("https://m2.dv8tion.net/releases")

    maven("https://jitpack.io")

    maven("https://repo.md-5.net/content/repositories/snapshots/")

    maven("https://nexus.velocitypowered.com/repository/maven-public/")

    maven("https://repo.codemc.org/repository/maven-public")

    maven("https://repo.networkmanager.xyz/repository/maven-public/")

    // Slimjar repository
    // Doesn't work anymore
    //maven("https://repo.vshnv.tech/releases")

    // For slimjar
    maven("https://repo.glaremasters.me/repository/public/")
}

dependencies {
    compileOnly("io.github.slimjar:slimjar:1.2.6")
    compileOnly(kotlin("stdlib-jdk8"))
    implementation("com.jagrosh:jda-utilities-command:3.0.4")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.4")
    //implementation("nl.chimpgamer.networkmanager:api:2.9.0-SNAPSHOT")
    compileOnly("nl.chimpgamer.networkmanager:bungeecord:2.10.9") {
        exclude("org.bstats:bstats-bungeecord:1.7")
    }
    slim("net.dv8tion:JDA:4.3.0_277") {
        exclude("club.minnced", "opus-java")
    }
    compileOnly("com.github.Carleslc:Simple-YAML:1.7.2")
    compileOnly("com.imaginarycode.minecraft:RedisBungee:0.3.8-SNAPSHOT")

    compileOnly("cloud.commandframework:cloud-core:1.6.1")
    compileOnly("cloud.commandframework:cloud-annotations:1.6.1")

    compileOnly("com.velocitypowered:velocity-api:3.1.0")
    //kapt("com.velocitypowered:velocity-api:3.1.0")
}

group = "nl.chimpgamer.networkmanager.extensions"
version = "1.6.0"

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

        dependencies {
            include(dependency("nl.chimpgamer.networkmanager.extensions:.*"))
            include(dependency("com.jagrosh:.*"))
        }

        val shadedPackage = "nl.chimpgamer.networkmanager.shaded"
        val libPackage = "nl.chimpgamer.networkmanager.lib"
        relocate("io.github.slimjar", "$shadedPackage.slimjar")
        relocate("net.kyori", "$shadedPackage.kyori")

        relocate("kotlin", "$libPackage.kotlin")
        relocate("org.simpleyaml", "$libPackage.simpleyaml")
        //relocate("org.eclipse.jetty", "$libPackage.jetty")
        //relocate("javax.servlet", "$libPackage.javax.servlet")
        relocate("com.jagrosh.jdautilities", "$shadedPackage.com.jagrosh.jdautilities")
        relocate("cloud.commandframework", "$libPackage.cloud")
    }

    slimJar {
        shade = false

        relocate("net.dv8tion.jda", "nl.chimpgamer.networkmanager.lib.jda")
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}