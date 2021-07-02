package moe.exusiai;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Vigna extends JavaPlugin {
    public static File folder;
    public static FileConfiguration config;

    public static String certname;
    public static String certpassword;
    public static Integer port;
    public static Map<String, Object> proxyFolder;
    public static Map<String, Object> proxyHost;
    @Override
    public void onEnable() {
        folder = this.getDataFolder();
        folder.mkdirs();
        this.saveDefaultConfig();
        config = this.getConfig();

        certname = config.getString("cert.name");
        certpassword = config.getString("cert.password");
        port = config.getInt("port");
        proxyFolder = config.getConfigurationSection("proxyFolder").getValues(false);
        proxyHost = config.getConfigurationSection("proxyHost").getValues(false);

        try {
            ReverseProxyServer.StartProxyServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
