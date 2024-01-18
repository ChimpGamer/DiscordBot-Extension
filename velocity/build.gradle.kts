repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(project(":shared"))

    compileOnly("com.velocitypowered:velocity-api:3.1.1")

    compileOnly("nl.chimpgamer.networkmanager:common-proxy:2.14.5")

    compileOnly("com.github.ProxioDev.redisbungee:RedisBungee-Velocity:0.11.2")
}