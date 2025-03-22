repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    api(project(":shared"))

    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")

    compileOnly("com.github.ProxioDev.ValioBungee:RedisBungee-Bungee:0.11.4")
}

tasks {
    shadowJar {
        val buildNumber = System.getenv("BUILD_NUMBER") ?: System.getenv("GITHUB_RUN_NUMBER")
        if (buildNumber == null) {
            archiveFileName.set("DiscordBot-BungeeCord-v${project.version}.jar")
        } else {
            archiveFileName.set("DiscordBot-BungeeCord-v${project.version}-b$buildNumber.jar")
        }

        val shadedPackage = "nl.chimpgamer.networkmanager.shaded"
        relocate("net.kyori", "$shadedPackage.kyori")
    }
}