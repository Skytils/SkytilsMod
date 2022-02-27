/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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
package skytils.skytilsmod.mixins.hooks.renderer

import skytils.skytilsmod.utils.SBInfo.mode
import org.spongepowered.asm.mixin.injection.At
import skytils.skytilsmod.Skytils
import org.spongepowered.asm.mixin.injection.ModifyArg
import net.minecraft.util.ResourceLocation
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.client.renderer.entity.layers.LayerCreeperCharge
import org.spongepowered.asm.mixin.Mixin
import skytils.skytilsmod.utils.*

val VISIBLE_CREEPER_ARMOR = ResourceLocation("skytils", "creeper_armor.png")

fun modifyChargedCreeperLayer(res: ResourceLocation): ResourceLocation {
    var res = res
    if (Utils.inSkyblock && Skytils.config.moreVisibleGhosts && mode == "mining_3") {
        res = VISIBLE_CREEPER_ARMOR
    }
    return res
}