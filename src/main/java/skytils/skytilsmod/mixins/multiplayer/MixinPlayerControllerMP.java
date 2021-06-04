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

package skytils.skytilsmod.mixins.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.DamageBlockEvent;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    @Final
    @Shadow
    private Minecraft mc;

    @Inject(method = "isPlayerRightClickingOnEntity", at = @At("HEAD"), cancellable = true)
    private void onRightClickEntity(EntityPlayer player, Entity target, MovingObjectPosition movingObject, CallbackInfoReturnable<Boolean> cir) {
        handleRightClickEntity(player, target, cir);
    }

    @Inject(method = "interactWithEntitySendPacket", at = @At("HEAD"), cancellable = true)
    private void onInteractWithEntitySendPacket(EntityPlayer player, Entity target, CallbackInfoReturnable<Boolean> cir) {
        handleRightClickEntity(player, target, cir);
    }

    private void handleRightClickEntity(EntityPlayer player, Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (!Skytils.config.prioritizeItemAbilities || !Utils.inSkyblock || Utils.inDungeons) return;
        ItemStack item = player.getHeldItem();
        if (item != null && !(target instanceof EntityArmorStand)) {
            if (ItemUtil.hasRightClickAbility(item)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    private void onPlayerDamageBlock(BlockPos pos, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (MinecraftForge.EVENT_BUS.post(new DamageBlockEvent(pos, directionFacing))) cir.cancel();
        } catch (Throwable e) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cSkytils caught and logged an exception at DamageBlockEvent. Please report this on the Discord server."));
            e.printStackTrace();
        }
    }
}
