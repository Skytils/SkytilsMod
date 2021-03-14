package skytils.skytilsmod.features.impl.dungeons.solvers.terminals;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SelectAllColorSolver {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final ArrayList<Integer> shouldClick = new ArrayList<>();
    private static String colorNeeded;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null)
            return;

        if (!Skytils.config.selectAllColorTerminalSolver) return;

        if (mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            List<Slot> invSlots = ((GuiChest) mc.currentScreen).inventorySlots.inventorySlots;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (chestName.startsWith("Select all the")) {
                String promptColor = null;
                for (EnumDyeColor color : EnumDyeColor.values()) {
                    String unlocalized = color.getName().replaceAll("_", " ").toUpperCase(Locale.ENGLISH);
                    if (chestName.contains(unlocalized)) {
                        promptColor = color.getUnlocalizedName();
                        break;
                    }
                }
                if (!Objects.equals(promptColor, colorNeeded)) {
                    colorNeeded = promptColor;
                    shouldClick.clear();
                } else if (shouldClick.size() == 0) {
                    for (Slot slot : invSlots) {
                        if (slot.inventory == mc.thePlayer.inventory || !slot.getHasStack()) continue;
                        ItemStack item = slot.getStack();
                        if (item == null) continue;
                        if (item.isItemEnchanted()) continue;
                        if (slot.slotNumber < 9 || slot.slotNumber > 44 || slot.slotNumber % 9 == 0 || slot.slotNumber % 9 == 8)
                            continue;
                        if (item.getUnlocalizedName().contains(colorNeeded)) {
                            shouldClick.add(slot.slotNumber);
                        }
                    }
                } else {
                    for (int slotNum : ImmutableList.copyOf(shouldClick)) {
                        Slot slot = chest.getSlot(slotNum);
                        if (slot.getHasStack() && slot.getStack().isItemEnchanted()) {
                            shouldClick.remove((Integer) slotNum);
                        }
                    }
                }
            } else {
                shouldClick.clear();
                colorNeeded = null;
            }
        }
    }

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!Utils.inDungeons) return;
        if (event.container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) event.container;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (chestName.startsWith("Select all the")) {
                event.setCanceled(true);
                if (Skytils.config.blockIncorrectTerminalClicks && event.slot != null) {
                    if (shouldClick.size() > 0) {
                        if (shouldClick.stream().noneMatch(slotNum -> slotNum == event.slot.slotNumber)) {
                            return;
                        }
                    }
                }
                mc.playerController.windowClick(event.container.windowId, event.slotId, 2, 0, mc.thePlayer);
            }
        }
    }

    @SubscribeEvent
    public void onDrawSlot(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if (!Utils.inDungeons) return;
        if (!Skytils.config.selectAllColorTerminalSolver) return;
        if (event.container instanceof ContainerChest) {
            Slot slot = event.slot;

            ContainerChest chest = (ContainerChest) event.container;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (chestName.startsWith("Select all the")) {
                if (shouldClick.size() > 0 && !shouldClick.contains(slot.slotNumber) && slot.inventory != mc.thePlayer.inventory) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (!Utils.inDungeons) return;
        if (!Skytils.config.selectAllColorTerminalSolver) return;
        if (event.toolTip == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) player.openContainer;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText();

            if (chestName.startsWith("Select all the")) {
                event.toolTip.clear();
            }
        }
    }

}
