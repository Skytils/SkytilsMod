package skytils.skytilsmod.features.impl.dungeons.solvers.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class ClickInOrderSolver {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final HashMap<Integer, Integer> slotOrder = new HashMap<>();
    private static int neededClick = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null) return;

        if (!Skytils.config.clickInOrderTerminalSolver) return;

        if (mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            List<Slot> invSlots = ((GuiChest) mc.currentScreen).inventorySlots.inventorySlots;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (chestName.equals("Click in order!")) {
                for (int i = 10; i <= 25; i++) {
                    if (i == 17 || i == 18) continue;
                    ItemStack itemStack = invSlots.get(i).getStack();
                    if (itemStack == null) continue;
                    if (itemStack.getItem() != Item.getItemFromBlock(Blocks.stained_glass_pane)) continue;
                    if (itemStack.getItemDamage() != 14 && itemStack.getItemDamage() != 5) continue;
                    if (itemStack.getItemDamage() == 5) {
                        if (itemStack.stackSize > neededClick) neededClick = itemStack.stackSize;
                    }
                    slotOrder.put(itemStack.stackSize - 1, i);
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        neededClick = 0;
        slotOrder.clear();
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!Utils.inSkyblock) return;
        if (!Skytils.config.clickInOrderTerminalSolver) return;
        if (event.gui instanceof GuiChest) {
            GuiChest inventory = (GuiChest) event.gui;
            Container containerChest = inventory.inventorySlots;
            if (containerChest instanceof ContainerChest) {
                List<Slot> invSlots = inventory.inventorySlots.inventorySlots;
                int chestSize = inventory.inventorySlots.inventorySlots.size();

                if (slotOrder.size() > neededClick) {
                    Slot slot = invSlots.get(slotOrder.get(neededClick));
                    RenderUtil.drawOnSlot(chestSize, slot.xDisplayPosition, slot.yDisplayPosition, new Color(2, 62, 138, 255).getRGB());
                }
                if (slotOrder.size() - 1 > neededClick) {
                    Slot slot = invSlots.get(slotOrder.get(neededClick + 1));
                    RenderUtil.drawOnSlot(chestSize, slot.xDisplayPosition, slot.yDisplayPosition, new Color(65, 102, 245, 255).getRGB());
                }
                if (slotOrder.size() - 2> neededClick) {
                    Slot slot = invSlots.get(slotOrder.get(neededClick + 2));
                    RenderUtil.drawOnSlot(chestSize, slot.xDisplayPosition, slot.yDisplayPosition, new Color(144, 224, 239, 255).getRGB());
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawSlot(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if (!Utils.inSkyblock) return;
        if (!Skytils.config.clickInOrderTerminalSolver) return;
        if (event.container instanceof ContainerChest) {
            FontRenderer fr = mc.fontRendererObj;
            Slot slot = event.slot;

            ContainerChest chest = (ContainerChest) event.container;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (chestName.equals("Click in order!")) {
                if (slot.getHasStack() && slot.inventory != mc.thePlayer.inventory) {
                    ItemStack item = slot.getStack();
                    if (item.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) && item.getItemDamage() == 14) {
                        GlStateManager.disableLighting();
                        GlStateManager.disableDepth();
                        GlStateManager.disableBlend();
                        fr.drawStringWithShadow(String.valueOf(item.stackSize), (float)(slot.xDisplayPosition + 9 - fr.getStringWidth(String.valueOf(item.stackSize)) / 2), (float)(slot.yDisplayPosition + 4), 16777215);
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (!Utils.inSkyblock) return;
        if (!Skytils.config.clickInOrderTerminalSolver) return;
        if (event.toolTip == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) player.openContainer;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText();

            if (chestName.equals("Click in order!")) {
                event.toolTip.clear();
            }
        }
    }
}
