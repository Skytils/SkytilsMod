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

package skytils.skytilsmod.mixins.renderer;

import net.minecraft.client.renderer.entity.layers.LayerCreeperCharge;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;

import java.util.Objects;

@Mixin(LayerCreeperCharge.class)
public abstract class MixinLayerCreeperCharge implements LayerRenderer<EntityCreeper> {

    ResourceLocation VISIBLE_CREEPER_ARMOR = new ResourceLocation("skytils", "creeper_armor.png");

    @ModifyArg(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderCreeper;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    private ResourceLocation modifyChargedCreeperLayer(ResourceLocation res) {
        if (Utils.inSkyblock && Skytils.config.moreVisibleGhosts && Objects.equals(SBInfo.INSTANCE.getMode(), "mining_3")) {
            res = VISIBLE_CREEPER_ARMOR;
        }
        return res;
    }
}
