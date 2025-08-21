/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.mixins.transformers.network;

import com.llamalad7.mixinextras.sugar.Local;
import gg.skytils.skytilsmod.features.impl.dungeons.MasterMode7Features;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//# if MC>12000
import gg.skytils.skytilsmod.Skytils;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.block.PaneBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
//# endif

//#if MC==10809
//$$ import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
//#else
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
//#endif

@Mixin(value = ClientPlayNetworkHandler.class, priority = 1001)
public abstract class MixinNetHandlerPlayClient implements ClientPlayPacketListener {
    //#if MC==10809
    //$$ @Inject(method = "onMobSpawn", at = @At("TAIL"))
    //#else
    @Inject(method = "onEntitySpawn", at = @At("TAIL"))
    //#endif
    private void onHandleSpawnMobTail(
            //#if MC==10809
            //$$ MobSpawnS2CPacket packet,
            //#else
            EntitySpawnS2CPacket packet,
            //#endif
            CallbackInfo ci, @Local Entity entity) {
        if (entity != null) {
            MasterMode7Features.INSTANCE.onMobSpawned(entity);
        }
    }

    //# if MC>12000
    private static final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "onBlockUpdate", at = @At("TAIL"))
    private void onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        if (client.world == null) return;
        if (!Skytils.getConfig().getGlassPaneDesync()) return;

        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos neighborPos = packet.getPos().offset(dir);
            BlockState neighborState = client.world.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof PaneBlock) {
                client.world.setBlockState(neighborPos, updateState(neighborPos, neighborState));
            }
        }
    }

    @Unique
    private BlockState updateState(BlockPos pos, BlockState state){
        int validConnect = 0;
        for (Direction dir : PaneBlock.FACING_PROPERTIES.keySet()){
            BooleanProperty property = PaneBlock.FACING_PROPERTIES.get(dir);
            if (state.get(property)){
                if (client.world.getBlockState(pos.offset(dir)).isAir()){
                    state = state.with(property, false);
                }
                else validConnect++;
            }
        }

        if (validConnect == 0){
            state = state.with(PaneBlock.NORTH, true)
                    .with(PaneBlock.EAST, true)
                    .with(PaneBlock.SOUTH, true)
                    .with(PaneBlock.WEST, true);
        }
        return state;
    }
    //# endif
}
