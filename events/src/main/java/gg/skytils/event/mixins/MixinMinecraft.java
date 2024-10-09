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

package gg.skytils.event.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.TickEvent;
import gg.skytils.event.impl.play.KeyboardInputEvent;
import gg.skytils.event.impl.play.MouseInputEvent;
import gg.skytils.event.impl.play.BlockInteractEvent;
import gg.skytils.event.impl.play.WorldUnloadEvent;
import gg.skytils.event.impl.screen.ScreenOpenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow public WorldClient theWorld;

    @Shadow public GuiScreen currentScreen;

    @Inject(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    //#if MC>=12000
                    //$$ target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
                    //#else
                    target = "Lnet/minecraft/profiler/Profiler;endSection()V",
                    //#endif
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            )
    )
    private void tick(CallbackInfo ci) {
        EventsKt.postSync(new TickEvent());
    }

    //#if MC<13000
    @WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", remap = false))
    private boolean mouseInput(Operation<Boolean> original) {
        while(original.call()) {
            if (EventsKt.postCancellableSync(new MouseInputEvent(Mouse.getEventX(), Mouse.getEventY(), Mouse.getEventButton()))) {
                continue;
            }
            return true;
        }
        return false;
    }

    @WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z", remap = false))
    private boolean keyboardInput(Operation<Boolean> original) {
        while(original.call()) {
            if (EventsKt.postCancellableSync(new KeyboardInputEvent(Keyboard.getEventKey()))) {
                continue;
            }
            return true;
        }
        return false;
    }
    //#endif

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER), cancellable = true)
    private void openScreen(CallbackInfo ci, @Local(argsOnly = true) LocalRef<GuiScreen> screen) {
        ScreenOpenEvent event = new ScreenOpenEvent(screen.get());
        if (EventsKt.postCancellableSync(event)) {
            ci.cancel();
        }
        screen.set(event.getScreen());
        this.currentScreen = event.getScreen();
    }

    //#if MC<12000
    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    //#else
    //$$ @Inject(method = {
    //$$     "joinWorld",
    //$$     "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V",
    //$$     "enterReconfiguration"
    //$$ }, at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;world:Lnet/minecraft/client/world/ClientWorld;", opcode = Opcodes.PUTFIELD))
    //#endif
    private void worldChange(CallbackInfo ci) {
        if (this.theWorld != null) {
            EventsKt.postSync(new WorldUnloadEvent(this.theWorld));
        }
    }

    @WrapOperation(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;onPlayerRightClick(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/Vec3;)Z"))
    private boolean onBlockInteract(PlayerControllerMP instance, EntityPlayerSP iblockstate, WorldClient world, ItemStack itemStack, BlockPos pos, EnumFacing enumFacing, Vec3 hitVec, Operation<Boolean> original) {
        return !EventsKt.postCancellableSync(new BlockInteractEvent(itemStack, pos)) && original.call(instance, iblockstate, world, itemStack, pos, enumFacing, hitVec);
    }
}
