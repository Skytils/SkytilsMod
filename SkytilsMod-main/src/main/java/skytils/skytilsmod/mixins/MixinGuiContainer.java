package skytils.skytilsmod.mixins;

import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {

    @Shadow public Container inventorySlots;

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(Slot slot, CallbackInfo ci) {
        if (!Utils.inSkyblock) return;
        Container container = this.inventorySlots;
        if (container instanceof ContainerChest) {
            ContainerChest cc = (ContainerChest) container;
            String displayName = cc.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (slot.getHasStack()) {
                ItemStack item = slot.getStack();
                if (Skytils.config.spiritLeapNames && displayName.equals("Spirit Leap")) {
                    if (item.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) {
                        ci.cancel();
                        return;
                    }
                }
            }
        }
    }
}