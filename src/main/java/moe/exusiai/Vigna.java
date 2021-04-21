package moe.exusiai;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Vigna extends JavaPlugin {
    public static File folder;
    public static FileConfiguration config;

    public static String certname;
    public static String certpassword;
    public static String domain;
    @Override
    public void onEnable() {
        folder = this.getDataFolder();
        folder.mkdirs();
        this.saveDefaultConfig();
        config = this.getConfig();

        certname = config.getString("cert.name");
        certpassword = config.getString("cert.password");
        domain = config.getString("domain");


        try {
            ReverseProxyServer.StartProxyServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
