/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.event.mixins.world;

import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.entity.EntityJoinWorldEvent;
import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientEntityManager.class)
public class MixinWorld<T extends EntityLike> {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    public void addEntity(T entity, CallbackInfo ci) {
        if (entity instanceof Entity && EventsKt.postCancellableSync(new EntityJoinWorldEvent((Entity) entity))) {
            ci.cancel();
        }
    }
}
