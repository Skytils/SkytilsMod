/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.event.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.TickEvent;
import gg.skytils.event.impl.play.*;
import gg.skytils.event.impl.screen.ScreenOpenEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

@Mixin(MinecraftClient.class)
public class MixinMinecraft {
    @Shadow public ClientWorld world;

    @Shadow public Screen currentScreen;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            )
    )
    private void tick(CallbackInfo ci) {
        EventsKt.postSync(new TickEvent());
    }

    @Inject(method = "setScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER), cancellable = true)
    private void openScreen(CallbackInfo ci, @Local(argsOnly = true) LocalRef<Screen> screen) {
        ScreenOpenEvent event = new ScreenOpenEvent(screen.get());
        if (EventsKt.postCancellableSync(event)) {
            ci.cancel();
        }
        screen.set(event.getScreen());
        this.currentScreen = event.getScreen();
    }

    @Inject(method = {
        "joinWorld",
        "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V",
        "enterReconfiguration"
    }, at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;world:Lnet/minecraft/client/world/ClientWorld;", opcode = Opcodes.PUTFIELD))
    private void worldChange(CallbackInfo ci) {
        if (this.world != null) {
            EventsKt.postSync(new WorldUnloadEvent(this.world));
        }
    }

    @WrapOperation(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult onBlockInteract(ClientPlayerInteractionManager instance, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, Operation<ActionResult> original) {
       ItemStack itemStack = player.getStackInHand(hand);
       BlockPos pos = hitResult.getBlockPos();
       if (!EventsKt.postCancellableSync(new BlockInteractEvent(itemStack, pos))) {
           return original.call(instance, player, hand, hitResult);
       } else {
           return ActionResult.PASS;
       }
    }

    @WrapOperation(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactEntityAtLocation(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult onEntityInteract(ClientPlayerInteractionManager instance, PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, Operation<ActionResult> original) {
       if (!EventsKt.postCancellableSync(new EntityInteractEvent(entity, hand))) {
           return original.call(instance, player, entity, hitResult, hand);
       } else {
           return ActionResult.PASS;
       }
    }
}
