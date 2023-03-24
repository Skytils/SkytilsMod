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
package gg.skytils.skytilsmod.features.impl.misc.damagesplash

import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.entity.RenderManager
import java.util.*

/**
 * Represents a completely client-side entity
 * That is fast handled by Skytils.
 *
 * With that you can have an entity similar vanilla behaviour but with a faster,
 * simple registration and execution.
 *
 * This is not attached to ANY WORLD
 * meaning that the entity will show in any world without exception.
 *
 * **THESE ENTITIES ARE NOT PERSISTENT**.
 *
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
open class FakeEntity
/**
 * Creates the FakeEntity objects
 * and updates it currently location
 *
 * @param currentLocation the spawn location
 */(
    /**
     * Updates the current location of the entity
     * @param currentLocation the provided location
     */
    var currentLocation: Location
) {
    /**
     * @return A random generated identificator for the entity
     */
    var uUID: UUID = UUID.randomUUID()

    /**
     * @return The entity living ticks (how old it's)
     */
    var livingTicks: Long = 0

    /**
     * @return The Entity current world location
     */
    private var toRemove = false

    /**
     * Called before rendering.
     * Use this to calculate pathfinders and related things.
     *
     * @param partialTicks the world partial ticks
     */
    open fun tick() {}

    /**
     * Called every world rendering tick
     *
     * @param partialTicks the world partial ticks
     * @param context the rendering context
     * @param render the Minecraft Render Manager
     */
    open fun render(partialTicks: Float, context: RenderGlobal?, render: RenderManager) {}

    /**
     * Marks the entity to be removed from the world.
     * If overriding this method, you should call super!
     */
    fun remove() {
        toRemove = true
    }

    /**
     * Please override this method
     * @return The entity name
     */
    open val name: String
        get() = "DefaultFakeEntity"

    /**
     * @return if the entity will be removed in the next tick
     */
    fun toRemove(): Boolean {
        return toRemove
    }
}