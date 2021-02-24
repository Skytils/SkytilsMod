package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.events.GuiRenderItemEvent;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

public class MinionFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static boolean blockUnenchanted = false;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        blockUnenchanted = false;
    }

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!Utils.inSkyblock) return;

        if (event.container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) event.container;

            IInventory inventory = chest.getLowerChestInventory();
            Slot slot = event.slot;
            if (slot == null) return;
            ItemStack item = slot.getStack();
            String inventoryName = inventory.getDisplayName().getUnformattedText();

            if (Skytils.config.onlyCollectEnchantedItems && inventoryName.contains("Minion") && item != null) {
                if (!item.isItemEnchanted()) {
                    if (inventoryName.equals("Minion Chest")) {
                        if (!blockUnenchanted) {
                            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                                ItemStack stack = inventory.getStackInSlot(i);
                                if (stack == null) continue;
                                if (stack.isItemEnchanted()) {
                                    blockUnenchanted = true;
                                    break;
                                }
                            }
                        }
                        if (blockUnenchanted && slot.inventory != mc.thePlayer.inventory) event.setCanceled(true);
                    } else {
                        ItemStack minionType = inventory.getStackInSlot(4);
                        if (minionType != null) {
                            if (StringUtils.stripControlCodes(minionType.getDisplayName()).contains("Minion")) {
                                if (!blockUnenchanted) {
                                    ItemStack firstUpgrade = inventory.getStackInSlot(37);
                                    ItemStack secondUpgrade = inventory.getStackInSlot(46);
                                    if (firstUpgrade != null) {
                                        if (StringUtils.stripControlCodes(firstUpgrade.getDisplayName()).contains("Super Compactor 3000")) {
                                            blockUnenchanted = true;
                                        }
                                    }
                                    if (secondUpgrade != null) {
                                        if (StringUtils.stripControlCodes(secondUpgrade.getDisplayName()).contains("Super Compactor 3000")) {
                                            blockUnenchanted = true;
                                        }
                                    }
                                }

                                int index = slot.getSlotIndex();
                                if (blockUnenchanted && slot.inventory != mc.thePlayer.inventory && index >= 21 && index <= 43 && index % 9 >= 3 && index % 9 <= 7) {
                                    event.setCanceled(true);
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
        if (!Utils.inSkyblock || item == null || item.stackSize != 1) return;

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
