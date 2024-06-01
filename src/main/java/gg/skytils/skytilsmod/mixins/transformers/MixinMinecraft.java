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

package gg.skytils.skytilsmod.mixins.transformers;

import com.google.common.util.concurrent.ListenableFuture;
import gg.skytils.skytilsmod.Skytils;
import gg.skytils.skytilsmod.utils.ItemUtil;
import gg.skytils.skytilsmod.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.concurrent.Executor;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements Executor {
    @Shadow
    public EntityPlayerSP thePlayer;

    @Shadow
    public abstract ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule);

    @Inject(method = "clickMouse()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;swingItem()V", shift = At.Shift.AFTER))
    private void clickMouse(CallbackInfo info) {
        if (!Utils.INSTANCE.getInSkyblock()) return;

        ItemStack item = this.thePlayer.getHeldItem();
        if (item != null) {
            NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
            String itemId = ItemUtil.getSkyBlockItemID(extraAttr);

            if (Objects.equals(itemId, "BLOCK_ZAPPER")) {
                Skytils.sendMessageQueue.add("/undozap");
            }
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.addScheduledTask(command);
    }
}