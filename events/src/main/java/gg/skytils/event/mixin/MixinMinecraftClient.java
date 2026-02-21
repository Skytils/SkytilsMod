package gg.skytils.event.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.ScreenOpenEvent;
import gg.skytils.event.impl.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow
    @Nullable
    public Screen currentScreen;

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
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
}
