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
package skytils.skytilsmod.features.impl.misc

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.EntityManager.spawnEntity
import skytils.skytilsmod.core.EntityManager.renderEntities
import skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import skytils.skytilsmod.features.impl.misc.damagesplash.DamageSplashEntity
import skytils.skytilsmod.features.impl.misc.damagesplash.Location
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.stripControlCodes
import java.util.regex.Pattern

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
class DamageSplash {
    @SubscribeEvent
    fun renderFakeEntity(e: RenderWorldLastEvent) {
        renderEntities(e.partialTicks, e.context)
    }

    @SubscribeEvent
    fun onRenderLiving(e: RenderLivingEvent.Specials.Pre<EntityLivingBase>) {
        if (!Utils.inSkyblock || !Skytils.config.customDamageSplash) return
        val entity = e.entity
        if (entity.ticksExisted > 300 || entity !is EntityArmorStand) return
        if (!entity.hasCustomName()) return
        if (entity.isDead) return
        val strippedName = entity.customNameTag.stripControlCodes()
        val damageMatcher = damagePattern.matcher(strippedName)
        if (damageMatcher.matches()) {
            e.isCanceled = true
            entity.worldObj.removeEntity(e.entity)
            if (Skytils.config.hideDamageInBoss && DungeonFeatures.hasBossSpawned) return
            val name = entity.customNameTag
            val damage = damageMatcher.group(1).run {
                when {
                    name.startsWith("§0") -> "${this}☠"
                    name.startsWith("§f") && !name.contains("§e") -> "${this}❂"
                    name.startsWith("§6") && !name.contains("§e") -> "${this}火"
                    name.startsWith("§3") -> "${this}水"
                    else -> this
                }
            }
            spawnEntity(
                DamageSplashEntity(
                    entity,
                    damage,
                    Location(entity.posX, entity.posY + 1.5, entity.posZ)
                )
            )
        }
    }

    companion object {
        private val damagePattern = Pattern.compile("✧?(\\d+[⚔+✧❤♞☄✷]*)")
    }
}