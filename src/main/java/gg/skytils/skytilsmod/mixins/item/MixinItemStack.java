package gg.skytils.skytilsmod.mixins.item;

import gg.skytils.skytilsmod.hooks.ItemStackHookKt;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow
    public abstract @Nullable Text getCustomName();

    @Shadow
    public abstract Text getItemName();

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    public void getName(CallbackInfoReturnable<Text> cir) {
        cir.setReturnValue(ItemStackHookKt.modifyDisplayName(this.getCustomName() != null ? this.getCustomName() : this.getItemName()));
    }
}
