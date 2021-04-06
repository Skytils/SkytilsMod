/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.features.impl.dungeons.solvers.terminals;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartsWithSequenceSolver {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static final ArrayList<Integer> shouldClick = new ArrayList<>();
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
                String sequence = nameMatcher.group(1);
                if (!Objects.equals(sequence, sequenceNeeded)) {
                    sequenceNeeded = sequence;
                    shouldClick.clear();
                } else if (shouldClick.size() == 0) {
                    for (Slot slot : invSlots) {
                        if (slot.inventory == mc.thePlayer.inventory || !slot.getHasStack()) continue;
                        ItemStack item = slot.getStack();
                        if (item == null) continue;
                        if (item.isItemEnchanted()) continue;
                        if (slot.slotNumber < 9 || slot.slotNumber > 44 || slot.slotNumber % 9 == 0 || slot.slotNumber % 9 == 8)
                            continue;
                        if (StringUtils.stripControlCodes(item.getDisplayName()).startsWith(sequenceNeeded)) {
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
                sequenceNeeded = null;
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
        if (!Skytils.config.startsWithSequenceTerminalSolver) return;
        if (event.container instanceof ContainerChest) {
            Slot slot = event.slot;

            ContainerChest chest = (ContainerChest) event.container;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (chestName.startsWith("What starts with:")) {
                if (shouldClick.size() > 0 && !shouldClick.contains(slot.slotNumber) && slot.inventory != mc.thePlayer.inventory) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (!Utils.inDungeons) return;
        if (!Skytils.config.startsWithSequenceTerminalSolver) return;
        if (event.toolTip == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) player.openContainer;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText();

            if (chestName.startsWith("What starts with:")) {
                event.toolTip.clear();
            }
        }
    }
}
