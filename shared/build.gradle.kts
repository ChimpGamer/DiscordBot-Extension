dependencies {
    compileOnly("nl.chimpgamer.networkmanager:common-proxy:2.14.5")

    compileOnly("com.github.Carleslc:Simple-YAML:1.8.4")

    compileOnly("cloud.commandframework:cloud-core:1.8.4")
    compileOnly("cloud.commandframework:cloud-annotations:1.8.4")

    compileOnly("com.google.code.gson:gson:2.10.1")

    compileOnly("com.gitlab.ruany", "LiteBansAPI", "0.3.5")

    compileOnly("com.github.ProxioDev.redisbungee:RedisBungee-API:0.11.2")

    implementation("net.dv8tion:JDA:5.0.0-beta.20") {
        exclude("club.minnced", "opus-java")
    }

    implementation("club.minnced:jda-ktx:0.11.0-beta.20")
}