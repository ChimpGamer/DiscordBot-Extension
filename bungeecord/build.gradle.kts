dependencies {
    api(project(":shared"))

    compileOnly("net.md-5:bungeecord-api:1.20-R0.1") {
        exclude("net.md-5", "brigadier") // Does not seem available.
    }

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