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

package skytils.skytilsmod.features.impl.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.mixins.AccessorGuiNewChat;
import skytils.skytilsmod.utils.APIUtil;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.TabListUtils;
import skytils.skytilsmod.utils.Utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class MayorInfo {

    public static String currentMayor = null;
    public static HashSet<String> mayorPerks = new HashSet<>();
    public static boolean isLocal = true;

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static int ticks = 0;

    private static long lastCheckedElectionOver = 0;
    private static long lastFetchedMayorData = 0;

    public static final String baseURL = "https://sbe-stole-skytils.design/api/mayor";

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!Utils.inSkyblock || event.phase != TickEvent.Phase.START) return;

        if (ticks % 100 == 0) {
            if ((System.currentTimeMillis() - lastFetchedMayorData > (24 * 60 * 60 * 1000)) || isLocal) {
                fetchMayorData();
            }
            if (System.currentTimeMillis() - lastCheckedElectionOver > (24 * 60 * 60 * 1000)) {
                String elected = currentMayor;
                for (NetworkPlayerInfo pi : TabListUtils.getTabEntries()) {
                    String name = mc.ingameGUI.getTabList().getPlayerName(pi);
                    if (name.startsWith("§r §r§fWinner: §r§a")) {
                        elected = name.substring(19, name.length() - 2);
                        break;
                    }
                }
                if (currentMayor != null) {
                    if (!currentMayor.equals(elected)) {
                        isLocal = true;
                        currentMayor = elected;
                        mayorPerks.clear();
                    }
                    lastCheckedElectionOver = System.currentTimeMillis();
                }
            }
            ticks = 0;
        }

        ticks++;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock) return;

        if (event.message.getUnformattedText().equals("§eEverybody unlocks §6exclusive §eperks! §a§l[HOVER TO VIEW]") && event.message.getSiblings().size() == 1) {
            HoverEvent hoverEvent = event.message.getSiblings().get(0).getChatStyle().getChatHoverEvent();
            if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
                IChatComponent value = hoverEvent.getValue();
                System.out.println(value.getFormattedText());
                String[] lines = value.getFormattedText().split("\n");
                if (lines.length < 2) return;
                String color = "";
                if (StringUtils.stripControlCodes(lines[0]).startsWith("Mayor ")) {
                    color = lines[0].substring(0, 2);
                }
                isLocal = true;
                currentMayor = lines[0].substring(lines[0].lastIndexOf(" ") + 1);
                mayorPerks.clear();
                fetchMayorData();
                HashSet<String> perks = new HashSet<>();
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];

                    if (!line.contains("§") || line.indexOf("§") != 0 || line.lastIndexOf("§") != 2) continue;

                    if (color.length() > 0) {
                        if (line.startsWith("§r" + color)) {
                            perks.add(StringUtils.stripControlCodes(line));
                        }
                    } else if (!line.startsWith("§r§7") && !line.startsWith("§r§8")) {
                        perks.add(StringUtils.stripControlCodes(line));
                    }
                }
                System.out.println("Got perks from chat: " + perks);
                mayorPerks.addAll(perks);
                sendMayorData(currentMayor, mayorPerks);
            }
        }
    }

    @SubscribeEvent
    public void onDrawSlot(GuiContainerEvent.DrawSlotEvent event) {
        if (!Utils.inSkyblock) return;
        if (event.container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) event.container;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText();
            if (mayorPerks.size() == 0 && (chestName.equals("Mayor " + currentMayor) || (chestName.startsWith("Mayor ") && currentMayor == null))) {
                Slot slot = event.slot;
                ItemStack item = slot.getStack();
                if (item != null && item.getItem() == Items.skull && (item.getDisplayName().contains("Mayor " + currentMayor) || (currentMayor == null && item.getDisplayName().contains("Mayor ") && !item.getDisplayName().contains("Election")))) {
                    if (currentMayor == null) {
                        isLocal = true;
                        currentMayor = chestName.substring(6);
                        mayorPerks.clear();
                        fetchMayorData();
                    }
                    String color = item.getDisplayName().substring(0, 2);
                    List<String> lore = ItemUtil.getItemLore(item);
                    if (lore.contains("§8Perks List") && lore.contains("§7The listed perks are")) {
                        HashSet<String> perks = new HashSet<>();
                        for (String line : lore) {
                            if (line.startsWith(color) && line.indexOf("§") == line.lastIndexOf("§")) {
                                perks.add(StringUtils.stripControlCodes(line));
                            }
                        }
                        System.out.println("Got Perks: " + perks);
                        mayorPerks.addAll(perks);
                        sendMayorData(currentMayor, mayorPerks);
                    }
                }
            }
        }
    }

    public static void fetchMayorData() {
        new Thread(() -> {
            JsonObject res = APIUtil.getJSONResponse(baseURL);
            if (res.has("name") && res.has("perks")) {
                if (res.get("name").getAsString().equals(currentMayor)) isLocal = false;
                currentMayor = res.get("name").getAsString();
                lastFetchedMayorData = System.currentTimeMillis();
                mayorPerks.clear();

                JsonArray perks = res.get("perks").getAsJsonArray();

                for (int i = 0; i < perks.size(); i++) {
                    JsonObject perk = perks.get(i).getAsJsonObject();
                    if (perk.has("name")) {
                        mayorPerks.add(perk.get("name").getAsString());
                    }
                }
            }
        }, "Skytils-FetchMayor").start();
    }

    public static void sendMayorData(String mayor, HashSet<String> perks) {
        if (mayor == null || perks.size() == 0) return;
        new Thread(() -> {
            try {
                String serverId = UUID.randomUUID().toString().replaceAll("-", "");
                StringBuilder url = new StringBuilder(baseURL + "/new?username=" + mc.getSession().getUsername() + "&serverId=" + serverId + "&mayor=" + mayor);
                for (String perk : perks) {
                    url.append("&perks[]=").append(URLEncoder.encode(perk, "UTF-8"));
                }

                String commentForDecompilers = "This sends a request to Mojang's auth server, used for verification. This is how we verify you are the real user without your session details. This is the exact same system Optifine uses.";
                mc.getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getToken(), serverId);
                System.out.println(APIUtil.getJSONResponse(url.toString()));
            } catch (AuthenticationException | IOException e) {
                e.printStackTrace();
            }
        }, "Skytils-SendMayor").start();
    }

}