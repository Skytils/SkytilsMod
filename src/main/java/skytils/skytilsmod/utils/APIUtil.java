/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class APIUtil {

    private static final JsonParser parser = new JsonParser();

    private static SSLContext sslContext = null;

    static {
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(APIUtil::isValidCert)
                .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    private static final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
            .setSslContext(sslContext)
            .build();

    private static final PoolingHttpClientConnectionManagerBuilder cm = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(sslSocketFactory);

    public static HttpClientBuilder builder =
            HttpClients.custom().setUserAgent("Skytils/${Skytils.VERSION}")
            .addRequestInterceptorFirst((request, entity, context) -> {
                if (!request.containsHeader("Pragma")) request.addHeader("Pragma", "no-cache");
                if (!request.containsHeader("Cache-Control")) request.addHeader("Cache-Control", "no-cache");
            });

    public static JsonObject getJSONResponse(String urlString) {
        CloseableHttpClient client = builder.setConnectionManager(cm.build()).build();
        try {
            HttpGet request = new HttpGet(new URL(urlString).toURI());

            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            JsonObject obj = parser.parse(EntityUtils.toString(entity)).getAsJsonObject();
            EntityUtils.consume(entity);
            return obj;
        } catch (Throwable ex) {
            ex.printStackTrace();
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cSkytils ran into an error whilst fetching a resource. See logs for more details."));
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new JsonObject();
    }

    public static JsonArray getArrayResponse(String urlString) {
        CloseableHttpClient client = builder.setConnectionManager(cm.build()).build();
        try {
            HttpGet request = new HttpGet(new URL(urlString).toURI());

            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            JsonArray obj = parser.parse(EntityUtils.toString(entity)).getAsJsonArray();
            EntityUtils.consume(entity);
            return obj;
        } catch (Throwable ex) {
            ex.printStackTrace();
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cSkytils ran into an error whilst fetching a resource. See logs for more details."));
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new JsonArray();
    }

    /**
     * Modified from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    public static String getUUID(String username) {
        JsonObject uuidResponse = getJSONResponse("https://api.mojang.com/users/profiles/minecraft/" + username);
        return uuidResponse.get("id").getAsString();
    }

    /**
     * Modified from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    public static String getLatestProfileID(String UUID, String key) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        // Get profiles
        System.out.println("Fetching profiles...");

        JsonObject profilesResponse = getJSONResponse("https://api.hypixel.net/skyblock/profiles?uuid=" + UUID + "&key=" + key);
        if (!profilesResponse.get("success").getAsBoolean()) {
            String reason = profilesResponse.get("cause").getAsString();
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Skytils failed to fetch UUID because reason: " + reason));
            return null;
        }
        if (profilesResponse.get("profiles").isJsonNull()) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This player doesn't appear to have played SkyBlock."));
            return null;
        }

        // Loop through profiles to find latest
        System.out.println("Looping through profiles...");
        String latestProfile = "";
        long latestSave = 0;
        JsonArray profilesArray = profilesResponse.get("profiles").getAsJsonArray();

        for (JsonElement profile : profilesArray) {
            JsonObject profileJSON = profile.getAsJsonObject();
            long profileLastSave = 1;
            if (profileJSON.get("members").getAsJsonObject().get(UUID).getAsJsonObject().has("last_save")) {
                profileLastSave = profileJSON.get("members").getAsJsonObject().get(UUID).getAsJsonObject().get("last_save").getAsLong();
            }

            if (profileLastSave > latestSave) {
                latestProfile = profileJSON.get("profile_id").getAsString();
                latestSave = profileLastSave;
            }
        }

        return latestProfile;
    }

    private static boolean isValidCert(X509Certificate[] chain, String authType) {
        for (X509Certificate cert : chain) {
            if (cert.getIssuerDN().getName().equals("CN=R3, O=Let's Encrypt, C=US")) return true;
        }
        return false;
    }
}
