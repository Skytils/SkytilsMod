package gg.skytils.skytilsmod.mixins.events;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gg.skytils.event.EventsKt;
import gg.skytils.skytilsmod._event.DrawSlotEvent;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {
    @Inject(method = "drawSlot", at = @At(value = "HEAD"), cancellable = true)
    public void onDrawSlot(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new DrawSlotEvent(context, slot, mouseX, mouseY))) {
            ci.cancel();
        }
    }
}