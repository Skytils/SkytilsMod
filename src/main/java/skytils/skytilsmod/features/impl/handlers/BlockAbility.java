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

package skytils.skytilsmod.features.impl.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.DataFetcher;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class BlockAbility {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static File saveFile;

    public static final HashSet<String> blockedItems = new HashSet<>();


    public static final List<Block> interactables = Arrays.asList(
            Blocks.acacia_door,
            Blocks.anvil,
            Blocks.beacon,
            Blocks.bed,
            Blocks.birch_door,
            Blocks.brewing_stand,
            Blocks.command_block,
            Blocks.crafting_table,
            Blocks.chest,
            Blocks.dark_oak_door,
            Blocks.daylight_detector,
            Blocks.daylight_detector_inverted,
            Blocks.dispenser,
            Blocks.dropper,
            Blocks.enchanting_table,
            Blocks.ender_chest,
            Blocks.furnace,
            Blocks.hopper,
            Blocks.jungle_door,
            Blocks.lever,
            Blocks.noteblock,
            Blocks.powered_comparator,
            Blocks.unpowered_comparator,
            Blocks.powered_repeater,
            Blocks.unpowered_repeater,
            Blocks.standing_sign,
            Blocks.wall_sign,
            Blocks.trapdoor,
            Blocks.trapped_chest,
            Blocks.wooden_button,
            Blocks.stone_button,
            Blocks.oak_door,
            Blocks.skull
    );

    public BlockAbility() {
        saveFile = new File(Skytils.modDir, "blockability.json");
        reloadSave();
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.entityPlayer != mc.thePlayer) return;
        ItemStack item = event.entityPlayer.getHeldItem();
        if (item == null) return;

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            if (shouldBlockAbility(item)) event.setCanceled(true);
        } else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Block block = event.world.getBlockState(event.pos).getBlock();
            if (!interactables.contains(block) || (Utils.inDungeons && (block == Blocks.coal_block || block == Blocks.stained_hardened_clay))) {
                if (shouldBlockAbility(item)) event.setCanceled(true);
            }
        }
    }

    private boolean shouldBlockAbility(ItemStack item) {
        if (item != null) {
            NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
            String itemId = ItemUtil.getSkyBlockItemID(extraAttr);
            if (extraAttr != null && itemId != null) {
                if (Skytils.config.blockMathHoeClicks && itemId.startsWith("THEORETICAL_HOE")) {
                    return true;
                }
                if (Skytils.config.blockUselessZombieSword && mc.thePlayer.getHealth() >= mc.thePlayer.getMaxHealth() && itemId.contains("ZOMBIE_SWORD")) {
                    return true;
                }
                return blockedItems.contains(itemId);
            }
        }
        return false;
    }


    public static void reloadSave() {
        blockedItems.clear();
        JsonArray dataArray;
        try (FileReader in = new FileReader(saveFile)) {
            dataArray = gson.fromJson(in, JsonArray.class);
            blockedItems.addAll(Arrays.asList(DataFetcher.getStringArrayFromJsonArray(dataArray)));
        } catch (Exception e) {
            dataArray = new JsonArray();
            try (FileWriter writer = new FileWriter(saveFile)) {
                gson.toJson(dataArray, writer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writeSave() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            JsonArray arr = new JsonArray();
            for (String itemId : blockedItems) {
                arr.add(new JsonPrimitive(itemId));
            }
            gson.toJson(arr, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
