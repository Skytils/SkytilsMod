package skytils.skytilsmod.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

public class ChatListener {

    public static Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.isOnHypixel()) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (unformatted.startsWith("Your new API key is ")) {
            String apiKey = event.message.getSiblings().get(0).getChatStyle().getChatClickEvent().getValue();
            Skytils.config.apiKey = apiKey;
            Skytils.config.markDirty();
            Skytils.config.writeData();
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Set API key to " + EnumChatFormatting.DARK_GREEN + apiKey));
        }
    }
}
