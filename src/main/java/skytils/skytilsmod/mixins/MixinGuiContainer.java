package skytils.skytilsmod.mixins;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.events.GuiContainerEvent;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {

    @Shadow public Container inventorySlots;

    @Inject(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V", shift = At.Shift.BEFORE))
    private void closeWindowPressed(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.CloseWindowEvent(inventorySlots));
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(Slot slot, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawSlotEvent.Pre(inventorySlots, slot))) ci.cancel();
    }

    @Inject(method = "handleMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;windowClick(IIIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.SlotClickEvent(inventorySlots, slot, slotId, clickedButton, clickType))) ci.cancel();
    }
}