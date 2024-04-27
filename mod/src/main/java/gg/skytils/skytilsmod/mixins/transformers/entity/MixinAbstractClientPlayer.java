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

package gg.skytils.skytilsmod.mixins.transformers.entity;

import com.mojang.authlib.GameProfile;
import gg.skytils.skytilsmod.mixins.hooks.entity.AbstractClientPlayerHook;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends EntityPlayer {
    public MixinAbstractClientPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Unique
    private final AbstractClientPlayerHook hook = new AbstractClientPlayerHook(this);

    @Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At("RETURN"), cancellable = true)
    private void replaceSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        hook.replaceSkin(cir);
    }

    @Inject(method = "hasSkin", at = @At("RETURN"), cancellable = true)
    private void replaceHasSkin(CallbackInfoReturnable<Boolean> cir) {
        hook.replaceHasSkin(cir);
    }

    @Inject(method = "getSkinType", at = @At("RETURN"), cancellable = true)
    private void replaceSkinType(CallbackInfoReturnable<String> cir) {
        hook.replaceSkinType(cir);
    }
}
