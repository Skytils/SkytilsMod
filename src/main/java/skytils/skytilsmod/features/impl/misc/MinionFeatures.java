package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiRenderItemEvent;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

public class MinionFeatures {

    @SubscribeEvent
    public void onMouseInputPre(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!Utils.inSkyblock) return;
        if (Mouse.getEventButton() != 0 && Mouse.getEventButton() != 1 && Mouse.getEventButton() != 2) return;
        if (!Mouse.getEventButtonState()) return;

        if (event.gui instanceof GuiChest) {
            Container containerChest = ((GuiChest) event.gui).inventorySlots;
            if (containerChest instanceof ContainerChest) {
                GuiChest chest = (GuiChest) event.gui;

                IInventory inventory = ((ContainerChest) containerChest).getLowerChestInventory();
                Slot mouseSlot = Utils.getSlotUnderMouse(chest);
                if (mouseSlot == null) return;
                ItemStack item = mouseSlot.getStack();
                String inventoryName = inventory.getDisplayName().getUnformattedText();

                if (Skytils.config.onlyCollectEnchantedItems && inventoryName.contains("Minion") && item != null) {
                    if (!item.isItemEnchanted()) {
                        if (inventoryName.equals("Minion Chest")) {
                            boolean chestHasEnchantedItem = false;
                            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                                ItemStack stack = inventory.getStackInSlot(i);
                                if (stack == null) continue;
                                if (stack.isItemEnchanted()) {
                                    chestHasEnchantedItem = true;
                                    break;
                                }
                            }
                            if (chestHasEnchantedItem) {
                                event.setCanceled(true);
                                return;
                            }
                        } else {
                            ItemStack minionType = inventory.getStackInSlot(4);
                            if (minionType != null) {
                                if (StringUtils.stripControlCodes(minionType.getDisplayName()).contains("Minion")) {
                                    int index = mouseSlot.getSlotIndex();
                                    if (index >= 21 && index <= 43 && index % 9 >= 3 && index % 9 <= 7) {
                                        ItemStack firstUpgrade = inventory.getStackInSlot(37);
                                        ItemStack secondUpgrade = inventory.getStackInSlot(46);
                                        if (firstUpgrade != null) {
                                            if (StringUtils.stripControlCodes(firstUpgrade.getDisplayName()).contains("Super Compactor 3000")) {
                                                event.setCanceled(true);
                                                return;
                                            }
                                        }
                                        if (secondUpgrade != null) {
                                            if (StringUtils.stripControlCodes(secondUpgrade.getDisplayName()).contains("Super Compactor 3000")) {
                                                event.setCanceled(true);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderItemOverlayPost(GuiRenderItemEvent.RenderOverlayEvent.Post event) {
        ItemStack item = event.stack;
        if (!Utils.inSkyblock || item == null) return;

        NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(item);

        if (Skytils.config.showMinionTier && extraAttributes != null && extraAttributes.hasKey("generator_tier")) {
            String s = String.valueOf(extraAttributes.getInteger("generator_tier"));

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            event.fr.drawStringWithShadow(s, (float)(event.x + 17 - event.fr.getStringWidth(s)), (float)(event.y + 9), 16777215);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

}
