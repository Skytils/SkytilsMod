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

package gg.skytils.skytilsmod.mixins.transformers.accessors;

import net.minecraft.client.model.ModelDragon;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelDragon.class)
public interface AccessorModelDragon {
    @Accessor
    float getPartialTicks();

    @Accessor
    ModelRenderer getHead();

    @Accessor
    ModelRenderer getSpine();

    @Accessor
    ModelRenderer getJaw();

    @Accessor
    ModelRenderer getBody();

    @Accessor
    ModelRenderer getRearLeg();

    @Accessor
    ModelRenderer getFrontLeg();

    @Accessor
    ModelRenderer getRearLegTip();

    @Accessor
    ModelRenderer getFrontLegTip();

    @Accessor
    ModelRenderer getRearFoot();

    @Accessor
    ModelRenderer getFrontFoot();

    @Accessor
    ModelRenderer getWing();

    @Accessor
    ModelRenderer getWingTip();
}
