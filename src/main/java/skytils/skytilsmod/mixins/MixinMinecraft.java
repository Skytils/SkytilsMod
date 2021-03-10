package skytils.skytilsmod.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow
    private EntityPlayerSP thePlayer;

    private final Minecraft that = (Minecraft) (Object) this;

    /**
     * Taken from Skyblockcatia under MIT License
     * Modified
     * https://github.com/SteveKunG/SkyBlockcatia/blob/1.8.9/LICENSE.md
     * @author SteveKunG
     */
    @Inject(method = "runGameLoop()V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/EntityRenderer.updateCameraAndRender(FJ)V", shift = At.Shift.AFTER))
    private void runGameLoop(CallbackInfo info)
    {
        GuiManager.toastGui.drawToast(new ScaledResolution(this.that));
    }

    @Inject(method = "clickMouse()V", at = @At(value = "INVOKE", target = "net/minecraft/client/entity/EntityPlayerSP.swingItem()V", shift = At.Shift.AFTER))
    private void clickMouse(CallbackInfo info) {
        if (!Utils.isOnHypixel() || !Utils.inSkyblock) return;

        ItemStack item = thePlayer.getHeldItem();
        if (item != null) {
            NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
            String itemId = ItemUtil.getSkyBlockItemID(extraAttr);

            if (itemId == null) return;

            if (itemId.equals("BLOCK_ZAPPER")) {
                Skytils.sendMessageQueue.add("/undozap");
            }

        }
    }
}
