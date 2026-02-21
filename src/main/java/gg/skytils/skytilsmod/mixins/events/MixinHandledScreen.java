package gg.skytils.skytilsmod.mixins.events;

import gg.skytils.event.impl.InventoryDrawSlotEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gg.skytils.event.EventsKt;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {
    @Inject(method = "drawSlot", at = @At(value = "HEAD"), cancellable = true)
    public void onDrawSlot(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new InventoryDrawSlotEvent(context, slot, mouseX, mouseY))) {
            ci.cancel();
        }
    }
}