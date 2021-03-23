package skytils.skytilsmod.mixins;

import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.SetActionBarEvent;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "setRecordPlaying(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSetActionBar(String message, boolean isPlaying, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new SetActionBarEvent(message, isPlaying))) ci.cancel();
    }

    @Inject(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/item/ItemStack;II)V"))
    private void renderRarityOnHotbar(int index, int xPos, int yPos, float partialTicks, EntityPlayer player, CallbackInfo ci) {
        if (Utils.inSkyblock && Skytils.config.showItemRarity) {
            RenderUtil.renderRarity(player.inventory.mainInventory[index], xPos, yPos);
        }
    }

}
