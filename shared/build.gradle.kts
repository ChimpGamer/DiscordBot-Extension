dependencies {
    compileOnly("dev.dejvokep:boosted-yaml:1.3.7")

    compileOnly("org.incendo:cloud-core:2.0.0")

    compileOnly("com.google.code.gson:gson:2.10.1")

    compileOnly("com.gitlab.ruany", "LiteBansAPI", "0.3.5")

    implementation("net.dv8tion:JDA:5.3.2") {
        exclude("club.minnced", "opus-java")
    }

    implementation("club.minnced:jda-ktx:0.12.0")
}