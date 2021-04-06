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

package skytils.skytilsmod.features.impl.misc.damagesplash;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;

import java.util.Random;
import java.util.UUID;

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
 * <b>THESE ENTITIES ARE NOT PERSISTENT</b>.
 *
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class FakeEntity {

    public UUID uuid = UUID.randomUUID();
    public long livingTicks = 0;
    public Location currentLocation;

    private boolean toRemove = false;

    /**
     * Creates the FakeEntity objects
     * and updates it currently location
     *
     * @param currentLocation the spawn location
     */
    public FakeEntity(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    /**
     * Called before rendering.
     * Use this to calculate pathfinders and related things.
     *
     * @param partialTicks the world partial ticks
     */
    public void tick(float partialTicks, Random r, EntityPlayerSP player) {

    }

    /**
     * Called every world rendering tick
     *
     * @param partialTicks the world partial ticks
     * @param context the rendering context
     * @param render the Minecraft Render Manager
     */
    public void render(float partialTicks, RenderGlobal context, RenderManager render) {

    }

    /**
     * Marks the entity to be removed from the world.
     * If overriding this method, you should call super!
     */
    public void remove() {
        toRemove = true;
    }

    /**
     * @return The Entity current world location
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * @return The entity living ticks (how old it's)
     */
    public long getLivingTicks() {
        return livingTicks;
    }

    /**
     * @return A random generated identificator for the entity
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Please override this method
     * @return The entity name
     */
    public String getName() {
        return "DefaultFakeEntity";
    }

    /**
     * @return if the entity will be removed in the next tick
     */
    public boolean toRemove() {
        return toRemove;
    }

    /**
     * Updates the current location of the entity
     * @param currentLocation the provided location
     */
    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}

