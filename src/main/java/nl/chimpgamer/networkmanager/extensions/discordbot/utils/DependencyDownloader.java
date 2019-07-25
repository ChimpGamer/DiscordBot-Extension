package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import lombok.RequiredArgsConstructor;
import nl.chimpgamer.networkmanager.extensions.discordbot.DiscordBot;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

@RequiredArgsConstructor
public class DependencyDownloader {
    private final DiscordBot discordBot;

    public void downloadDependency(String url, String name, String fileName) {
        String localPath = this.getDiscordBot().getNetworkManager().getDataFolder().getPath() + File.separator + "lib" + File.separator + fileName + ".jar";
        File file = new File(localPath);
        if (!file.exists()) {
            this.getDiscordBot().getLogger().info("Downloading " + name + " ...");

            try {
                downloadFile(url, localPath);
            } catch (IOException ex) {
                this.getDiscordBot().getLogger().severe("An error occured while downloading a required lib.");
                ex.printStackTrace();
            }
        }
        this.getDiscordBot().getLogger().info("Loading dependency " + name + " ...");

        this.getDiscordBot().getNetworkManager().getPluginClassLoader().loadJar(file.toPath());
    }

    private void downloadFile(String url, String location) throws IOException {
        URL website = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) website.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
        File yourFile = new File(location);
        yourFile.getParentFile().mkdirs();
        Files.copy(connection.getInputStream(), yourFile.toPath());
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }
}