package skytils.skytilsmod.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.events.GuiContainerEvent;

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
                if (currentMayor != null && !currentMayor.equals(elected)) {
                    isLocal = true;
                    currentMayor = elected;
                    mayorPerks.clear();
                    fetchMayorData();
                }
                lastCheckedElectionOver = System.currentTimeMillis();
            }
            ticks = 0;
        }

        ticks++;
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
                    List<String> lore = ItemUtil.getItemLore(item);
                    if (lore.contains("§8Perks List") && lore.contains("§7The listed perks are")) {
                        HashSet<String> perks = new HashSet<>();
                        for (String line : lore) {
                            if (line.startsWith("§d") && line.indexOf("§") == line.lastIndexOf("§")) {
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
                isLocal = false;
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
        }).start();
    }

    public static void sendMayorData(String mayor, HashSet<String> perks) {
        if (mayor == null || perks.size() == 0) return;
        new Thread(() -> {
            try {
                String serverId = UUID.randomUUID().toString().replaceAll("-", "");
                StringBuilder url = new StringBuilder(baseURL + "/new?username=" + mc.getSession().getUsername() + "&serverId=" + serverId + "&mayor=" + mayor);
                for (String perk : perks) {
                    url.append("&perks[]=").append(perk);
                }

                String commentForDecompilers = "This sends a request to Mojang's auth server, used for verification. This is how we verify you are the real user without your session details. This is the exact same system Optifine uses.";
                mc.getSessionService().joinServer(mc.getSession().getProfile(), mc.getSession().getToken(), serverId);
                JsonObject res = APIUtil.getJSONResponse(url.toString());
                System.out.println(res);
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
        }).start();
    }

}