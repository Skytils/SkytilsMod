package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.SendChatMessageEvent;
import skytils.skytilsmod.events.SendPacketEvent;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

public class PetFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static long lastPetConfirmation = 0;
    private static long lastPetLockNotif = 0;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatLow(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());

        if (Skytils.config.hideAutopetMessages) {
            if (unformatted.contains("Autopet equipped your")) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (!Utils.inSkyblock) return;

        if (Skytils.config.petItemConfirmation && (event.packet instanceof C02PacketUseEntity || event.packet instanceof C08PacketPlayerBlockPlacement)) {
            ItemStack item = mc.thePlayer.getHeldItem();
            if (item != null) {
                String itemId = ItemUtil.getSkyBlockItemID(item);
                if (itemId != null) {
                    if (itemId.contains("PET_ITEM") || ItemUtil.getItemLore(item).stream().anyMatch(s -> s.contains("PET ITEM"))) {
                        if (System.currentTimeMillis() - lastPetConfirmation > 5000) {
                            event.setCanceled(true);
                            if (System.currentTimeMillis() - lastPetLockNotif > 10000) {
                                lastPetLockNotif = System.currentTimeMillis();
                                ChatComponentText cc = new ChatComponentText("\u00a7cSkytils stopped you from using that pet item! \u00a76Click this message to disable the lock.");
                                cc.setChatStyle(cc.getChatStyle()
                                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/disableskytilspetitemlock"))
                                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to disable the pet item lock for 5 seconds.")))
                                );
                                mc.thePlayer.addChatMessage(cc);
                            }
                        } else {
                            lastPetConfirmation = 0;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onSendChatMessage(SendChatMessageEvent event) {
        if (event.message.equals("/disableskytilspetitemlock") && !event.addToChat) {
            lastPetConfirmation = System.currentTimeMillis();
            mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7aYou may now apply pet items for 5 seconds."));
            event.setCanceled(true);
        }
    }

}
