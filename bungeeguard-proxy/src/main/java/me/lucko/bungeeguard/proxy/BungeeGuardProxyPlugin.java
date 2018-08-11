package me.lucko.bungeeguard.proxy;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.security.SecureRandom;

/**
 * BungeeCord plugin which injects a special authentication token into a players
 * profile properties when they connect to a backend server.
 */
public final class BungeeGuardProxyPlugin extends Plugin implements Listener {

    // characters to use to build a token
    private static final String TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Randomly generates a new token
     *
     * @return a new token
     */
    private static String generateToken() {
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 64; i++) {
            sb.append(TOKEN_CHARS.charAt(random.nextInt(TOKEN_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * The auth token to inject into the property map
     */
    private String token = null;

    @Override
    public void onEnable() {

        // load a token from the config, if present
        ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);

        getDataFolder().mkdirs();
        File file = new File(getDataFolder(), "token.yml");

        if (file.exists()) {
            try {
                Configuration configuration = provider.load(file);
                token = configuration.getString("token", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (token == null || token.isEmpty()) {
            token = generateToken();

            Configuration configuration = new Configuration();
            configuration.set("token", token);

            try {
                provider.save(configuration, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        getProxy().getPluginManager().registerListener(this, this);

    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        // inject our spoofed loginresult instance into the initial handler
        InitialHandler con = (InitialHandler) event.getConnection();
        SpoofedLoginResult.inject(con, token);
    }

}
