package gg.skytils.skytilsmod.mixins.events;

import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.HotbarDrawSlotEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "renderHotbarItem", at = @At("HEAD"), cancellable = true)
    public void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new HotbarDrawSlotEvent(context, stack, x, y))) {
            ci.cancel();
        }
    }
}
