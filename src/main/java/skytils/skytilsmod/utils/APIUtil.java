package skytils.skytilsmod.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import skytils.skytilsmod.Skytils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Taken from Danker's Skyblock Mod under GPL 3.0 license
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
public class APIUtil {
    public static JsonObject getJSONResponse(String urlString) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", "Skytils/" + Skytils.VERSION);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String input;
                StringBuilder response = new StringBuilder();

                while ((input = in.readLine()) != null) {
                    response.append(input);
                }
                in.close();

                Gson gson = new Gson();

                return gson.fromJson(response.toString(), JsonObject.class);
            } else {
                if (urlString.startsWith("https://api.hypixel.net/")) {
                    InputStream errorStream = conn.getErrorStream();
                    try (Scanner scanner = new Scanner(errorStream)) {
                        scanner.useDelimiter("\\Z");
                        String error = scanner.next();

                        Gson gson = new Gson();
                        return gson.fromJson(error, JsonObject.class);
                    }
                } else if (urlString.startsWith("https://api.mojang.com/users/profiles/minecraft/") && conn.getResponseCode() == 204) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed with reason: Player does not exist."));
                } else if (urlString.startsWith(MayorInfo.baseURL)) {
                    StringBuilder response = new StringBuilder();

                    String line;

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println(conn.getResponseCode() + ": " + response.toString());
                    if (response.toString().startsWith("{")) {
                        Gson gson = new Gson();
                        return gson.fromJson(response.toString(), JsonObject.class);
                    }
                } else {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Request failed. HTTP Error Code: " + conn.getResponseCode()));
                }
            }
        } catch (IOException ex) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "An error has occured. See logs for more details."));
            ex.printStackTrace();
        }

        return new JsonObject();
    }

    // Only used for UUID => Username
    public static JsonArray getArrayResponse(String urlString) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String input;
                StringBuilder response = new StringBuilder();

                while ((input = in.readLine()) != null) {
                    response.append(input);
                }
                in.close();

                Gson gson = new Gson();

                return gson.fromJson(response.toString(), JsonArray.class);
            } else {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Request failed. HTTP Error Code: " + conn.getResponseCode()));
            }
        } catch (IOException ex) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "An error has occured. See logs for more details."));
            ex.printStackTrace();
        }

        return new JsonArray();
    }

    public static String getUUID(String username) {
        JsonObject uuidResponse = getJSONResponse("https://api.mojang.com/users/profiles/minecraft/" + username);
        return uuidResponse.get("id").getAsString();
    }

    public static String getLatestProfileID(String UUID, String key) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        // Get profiles
        System.out.println("Fetching profiles...");

        JsonObject profilesResponse = getJSONResponse("https://api.hypixel.net/skyblock/profiles?uuid=" + UUID + "&key=" + key);
        if (!profilesResponse.get("success").getAsBoolean()) {
            String reason = profilesResponse.get("cause").getAsString();
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed with reason: " + reason));
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
}
