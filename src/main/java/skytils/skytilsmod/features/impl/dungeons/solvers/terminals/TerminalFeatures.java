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

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.utils.StringUtils;
import skytils.skytilsmod.utils.Utils;

public class TerminalFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onGUIMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!Utils.inDungeons) return;
        // Skytils doesn't use this event, so it must be another mod that cancelled it
        if (event.isCanceled() && Skytils.config.blockIncorrectTerminalClicks) {
            if (mc.thePlayer.openContainer instanceof ContainerChest) {
                ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
                if (chestName.equals("Navigate the maze!") || chestName.equals("Correct all the panes!") || (chestName.startsWith("Select all the") && Skytils.config.selectAllColorTerminalSolver) || (chestName.startsWith("What starts with") && Skytils.config.startsWithSequenceTerminalSolver) || (chestName.equals("Click in order!") && Skytils.config.clickInOrderTerminalSolver)) {
                    event.setCanceled(false);
                }
            }
        }
    }

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (!Utils.inDungeons) return;
        if (event.toolTip == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) player.openContainer;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText();

            if (chestName.equals("Navigate the maze!") || chestName.equals("Correct all the panes!")) {
                event.toolTip.clear();
            }
        }
    }
}
