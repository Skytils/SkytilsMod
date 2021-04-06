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

package skytils.skytilsmod.features.impl.dungeons.solvers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.HashSet;

public class TeleportMazeSolver {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final HashSet<BlockPos> steppedPads = new HashSet<>();
    private static BlockPos lastTpPos;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!Skytils.config.teleportMazeSolver || !Utils.inDungeons) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        BlockPos groundBlock = new BlockPos(mc.thePlayer.posX, 69, mc.thePlayer.posZ);
        IBlockState state = mc.theWorld.getBlockState(groundBlock);
        if (state.getBlock() == Blocks.stone_slab) {
            if (lastTpPos != null) {
                boolean inNewCell = false;
                for (BlockPos routeTrace : BlockPos.getAllInBox(lastTpPos, groundBlock)) {
                    if (mc.theWorld.getBlockState(routeTrace).getBlock() == Blocks.iron_bars) {
                        inNewCell = true;
                        break;
                    }
                }
                if (inNewCell) {
                    for (BlockPos pad : Utils.getBlocksWithinRangeAtSameY(lastTpPos, 1, 69)) {
                        if (mc.theWorld.getBlockState(pad).getBlock() == Blocks.end_portal_frame) {
                            steppedPads.add(pad);
                            break;
                        }
                    }
                    for (BlockPos pad : Utils.getBlocksWithinRangeAtSameY(groundBlock, 1, 69)) {
                        if (mc.theWorld.getBlockState(pad).getBlock() == Blocks.end_portal_frame) {
                            steppedPads.add(pad);
                            break;
                        }
                    }
                }
            }
            lastTpPos = groundBlock;
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!Skytils.config.teleportMazeSolver) return;
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        for (BlockPos pos : steppedPads) {
            double x = pos.getX() - viewerX;
            double y = pos.getY() - viewerY;
            double z = pos.getZ() - viewerZ;
            GlStateManager.disableCull();
            RenderUtil.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01), new Color(255, 0, 0), 1f);
            GlStateManager.enableCull();
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        steppedPads.clear();
        lastTpPos = null;
    }
}
