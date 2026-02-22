package gg.skytils.skytilsmod.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.skytils.skytilsmod.core.Config;
import gg.skytils.skytilsmod.features.impl.handlers.MiscFeatures;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class PreventMouseReset_MixinMouse {
    @Shadow
    private double x;

    @Shadow
    private double y;

    @WrapOperation(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;setCursorParameters(Lnet/minecraft/client/util/Window;IDD)V"))
    public void lockMouse(Window window, int inputModeValue, double x, double y, Operation<Void> original) {
        boolean preventReset = Config.INSTANCE.getPreventCursorReset() && MiscFeatures.INSTANCE.shouldPreventCursorReset();
        if (preventReset) {
            this.x = MiscFeatures.INSTANCE.getCachedMouseX();
            this.y = MiscFeatures.INSTANCE.getCachedMouseY();
            GLFW.glfwSetInputMode(window.getHandle(), InputUtil.GLFW_CURSOR, InputUtil.GLFW_CURSOR_NORMAL);
        }
        original.call(window, inputModeValue, this.x, this.y);
    }
}
