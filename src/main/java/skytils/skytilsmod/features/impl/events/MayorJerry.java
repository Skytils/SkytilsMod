package skytils.skytilsmod.features.impl.events;

import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MayorJerry {

    private static final Pattern jerryType = Pattern.compile("(\\w+)(?=\\s+Jerry)");

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (Skytils.config.hiddenJerryAlert && unformatted.contains("☺") && unformatted.contains("Jerry") && !unformatted.contains("Jerry Box")) {
            Matcher matcher = jerryType.matcher(event.message.getFormattedText());
            if (matcher.find()) {
                String color = matcher.group(1);
                GuiManager.createTitle("\u00a7" + color.toUpperCase() + " JERRY!", 60);
            }
        }
    }

}
