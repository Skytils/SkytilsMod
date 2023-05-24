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
package gg.skytils.skytilsmod.core

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.features.impl.misc.damagesplash.FakeEntity
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
object EntityManager {
    private val entityList: MutableSet<FakeEntity> = HashSet()
    private val toSpawn: MutableSet<FakeEntity> = HashSet()

    /**
     * Spawns and register a fake entity into the world
     * This method is THREAD SAFE.
     *
     * @param entity the entity you want to register
     */
    @JvmStatic
    fun spawnEntity(entity: FakeEntity) {
        toSpawn.add(entity)
    }

    /**
     * Removes every single FakeEntity from the world.
     * This method is THREAD SAFE.
     */
    fun clearEntities() {
        entityList.forEach { it.remove() }
    }

    /**
     * Called on ClientTickEvent, process entity ticking
     */
    @JvmStatic
    fun tickEntities() {
        val it = entityList.iterator()
        while (it.hasNext()) {
            val next = it.next()
            next.livingTicks += 1
            next.tick()
        }
    }

    /**
     * Called on RenderWorldLastEvent, proccess the rendering queue
     *
     * @param partialTicks the world partial ticks
     * @param context the rendering context
     */
    @JvmStatic
    fun renderEntities(partialTicks: Float, context: RenderGlobal?) {
        if (entityList.isEmpty() && toSpawn.isEmpty()) return
        mc.mcProfiler.startSection("fakeEntities")
        run {

            // adds all new entities to the set
            var it = toSpawn.iterator()
            while (it.hasNext()) {
                entityList.add(it.next())
                it.remove()
            }
            val renderManager = mc.renderManager
            if (renderManager?.options == null) return
            val player = mc.thePlayer
            // ticks each entity
            it = entityList.iterator()
            while (it.hasNext()) {
                val next = it.next()

                // remove marked entities
                if (next.toRemove()) {
                    it.remove()
                    continue
                }
                mc.mcProfiler.startSection(next.name)
                // render
                GlStateManager.pushMatrix()
                // translates to the correct entity position
                // subtracting the viewer position offset
                GlStateManager.translate(
                    next.currentLocation.x - renderManager.viewerPosX,
                    next.currentLocation.y - renderManager.viewerPosY,
                    next.currentLocation.z - renderManager.viewerPosZ
                )
                next.render(partialTicks, context, renderManager)
                GlStateManager.popMatrix()
                mc.mcProfiler.endSection()
            }
        }
        mc.mcProfiler.endSection()
    }
}