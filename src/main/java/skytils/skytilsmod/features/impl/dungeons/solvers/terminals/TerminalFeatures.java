package skytils.skytilsmod.features.impl.dungeons.solvers.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.utils.Utils;

public class TerminalFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!Utils.inDungeons) return;
        if (event.container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) event.container;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (chestName.equals("Navigate the maze!") || chestName.equals("Correct all the panes!")) {
                event.setCanceled(true);
                if (chestName.equals("Correct all the panes!") && Skytils.config.blockIncorrectTerminalClicks && event.slot != null) {
                    ItemStack item = event.slot.getStack();
                    if (item != null) {
                        if (!StringUtils.stripControlCodes(item.getDisplayName()).startsWith("Off")) return;
                    }
                }
                mc.playerController.windowClick(event.container.windowId, event.slotId, 2, 0, mc.thePlayer);
            }
        }
    }
}
