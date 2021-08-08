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
package skytils.skytilsmod.mixins.hooks.renderer

import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.utils.Utils

fun modifyInteractables(entityList: List<Entity>): List<Entity> {
    entityList as ArrayList<Entity>
    if (Utils.inSkyblock) {
        if (!Utils.inDungeons && Skytils.config.hideCreeperVeilNearNPCs) {
            val npcs = mc.theWorld.getPlayers(
                EntityOtherPlayerMP::class.java
            ) { p: EntityOtherPlayerMP? -> p!!.uniqueID.version() == 2 && p.health == 20f && !p.isPlayerSleeping }
            entityList.removeAll label@{ entity: Entity ->
                if (entity is EntityCreeper && entity.isInvisible()) {
                    val creeper = entity
                    if (creeper.maxHealth == 20f && creeper.health == 20f && creeper.powered) {
                        return@label npcs.any { npc: EntityOtherPlayerMP -> npc.getDistanceSqToEntity(entity) <= 49 }
                    }
                }
                false
            }
        }
    }
    return entityList
}

fun onHurtcam(partialTicks: Float, ci: CallbackInfo) {
    if (Utils.inSkyblock && Skytils.config.noHurtcam) ci.cancel()
}

fun getLastLightningBolt(world: World): Int {
    return if (Skytils.config.hideLightning && Utils.inSkyblock) 0 else world.lastLightningBolt
}