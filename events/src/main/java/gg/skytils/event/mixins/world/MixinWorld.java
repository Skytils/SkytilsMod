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

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.entity.EntityJoinWorldEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(World.class)
public class MixinWorld {
    @Inject(method = "spawnEntityInWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;addEntity(Lnet/minecraft/entity/Entity;)V"), cancellable = true)
    public void spawnEntityInWorld(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        if (EventsKt.postCancellableSync(new EntityJoinWorldEvent(entityIn))) {
            cir.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method = "loadEntities", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    public Iterator<Entity> loadEntities(Iterator<Entity> original) {
        return Iterators.filter(original, entity -> EventsKt.postCancellableSync(new EntityJoinWorldEvent(entity)));
    }

    @ModifyExpressionValue(method = "joinEntityInSurroundings", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
    public boolean joinEntityInSurroundings(boolean original, @Local(argsOnly = true) Entity entity) {
        return !EventsKt.postCancellableSync(new EntityJoinWorldEvent(entity)) && original;
    }
}
