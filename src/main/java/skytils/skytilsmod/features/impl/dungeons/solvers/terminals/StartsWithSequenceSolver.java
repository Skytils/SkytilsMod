package skytils.skytilsmod.features.impl.dungeons.solvers.terminals;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartsWithSequenceSolver {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final ArrayList<Slot> shouldClick = new ArrayList<>();
    private static String sequenceNeeded = null;

    private static final Pattern titlePattern = Pattern.compile("^What starts with: ['\"](.+)['\"]\\?$");

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null)
            return;

        if (!Skytils.config.startsWithSequenceTerminalSolver) return;

        if (mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            List<Slot> invSlots = ((GuiChest) mc.currentScreen).inventorySlots.inventorySlots;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            Matcher nameMatcher = titlePattern.matcher(chestName);
            if (nameMatcher.find()) {
                if (sequenceNeeded == null) {
                    sequenceNeeded = nameMatcher.group(1);
                } else if (shouldClick.size() == 0) {
                    for (Slot slot : invSlots) {
                        if (slot.inventory == mc.thePlayer.inventory || !slot.getHasStack()) continue;
                        ItemStack item = slot.getStack();
                        if (item == null) continue;
                        if (item.isItemEnchanted()) continue;
                        if (slot.slotNumber < 9 || slot.slotNumber > 44 || slot.slotNumber % 9 == 0 || slot.slotNumber % 9 == 8)
                            continue;
                        if (item.getDisplayName().startsWith(sequenceNeeded)) {
                            shouldClick.add(slot);
                        }
                    }
                } else {
                    for (Slot slot : ImmutableList.copyOf(shouldClick)) {
                        ItemStack item = slot.getStack();
                        if (item == null) continue;
                        if (item.isItemEnchanted()) {
                            shouldClick.remove(slot);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        shouldClick.clear();
        sequenceNeeded = null;
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!Utils.inDungeons) return;
        if (!Skytils.config.startsWithSequenceTerminalSolver) return;
        if (event.gui instanceof GuiChest) {
            GuiChest inventory = (GuiChest) event.gui;
            Container container = inventory.inventorySlots;
            if (container instanceof ContainerChest) {
                ContainerChest chest = (ContainerChest) container;
                int chestSize = chest.inventorySlots.size();
                String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();

                if (chestName.startsWith("What starts with:")) {
                    for (Slot slot : ImmutableList.copyOf(shouldClick)) {
                        RenderUtil.drawOnSlot(chestSize, slot.xDisplayPosition, slot.yDisplayPosition, new Color(50, 229, 35, 237).getRGB());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!Utils.inDungeons) return;
        if (!Skytils.config.startsWithSequenceTerminalSolver) return;

        if (event.container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) event.container;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (chestName.startsWith("What starts with:")) {
                event.setCanceled(true);
                if (Skytils.config.blockIncorrectTerminalClicks && event.slot != null) {
                    if (shouldClick.size() > 0) {
                        if (shouldClick.stream().noneMatch(slot -> slot.slotNumber == event.slot.slotNumber)) {
                            return;
                        }
                    }
                }
                mc.playerController.windowClick(event.container.windowId, event.slotId, 2, 0, mc.thePlayer);
            }
        }
    }
}
