package me.imTedzi.ABA.spigot.util;

import me.imTedzi.ABA.spigot.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;

public class MojangAPI {

    private final String hasJoined = "https://sessionserver.mojang.com/session/minecraft/hasJoined?";

    private static MojangAPI instance = new MojangAPI();

    public boolean hasJoinedServer(LoginSession session, String serverId) {
        BukkitLoginSession playerSession = (BukkitLoginSession) session;
        try {
            String url = hasJoined + "username=" + playerSession.getUsername() + "&serverId=" + serverId;
            HttpURLConnection conn = getConnection(url);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.equals("null")) {
                //validate parsing
                //http://wiki.vg/Protocol_Encryption#Server
                JSONObject userData = (JSONObject) JSONValue.parseWithException(line);
                String uuid = (String) userData.get("id");
                playerSession.setUuid(parseId(uuid));

                JSONArray properties = (JSONArray) userData.get("properties");
                JSONObject skinProperty = (JSONObject) properties.get(0);

                String propertyName = (String) skinProperty.get("name");
                if (propertyName.equals("textures")) {
                    String skinValue = (String) skinProperty.get("value");
                    String signature = (String) skinProperty.get("signature");
                    playerSession.setSkin(skinValue, signature);
                }

                return true;
            }
        }
        catch (Exception ex) {
            Main.getInstance().getLogger().log(Level.WARNING, "Unable to verify session " + session.getUsername());
        }
        return false;
    }

    private HttpsURLConnection getConnection(String url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(2 * 3000);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Premium-Checker");
        return connection;
    }

    private UUID parseId(String withoutDashes) {
        if (withoutDashes == null) {
            return null;
        }

        return UUID.fromString(withoutDashes.substring(0, 8)
                + "-" + withoutDashes.substring(8, 12)
                + "-" + withoutDashes.substring(12, 16)
                + "-" + withoutDashes.substring(16, 20)
                + "-" + withoutDashes.substring(20, 32));
    }

    public static MojangAPI getInstance() {
        return instance;
    }
}
