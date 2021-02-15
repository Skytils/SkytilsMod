package skytils.skytilsmod.mixins;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiRenderItemEvent;
import skytils.skytilsmod.utils.Utils;

@Mixin(RenderItem.class)
public class MixinRenderItem {

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("RETURN"))
    public void renderItemOverlayPost(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new GuiRenderItemEvent.RenderOverlayEvent.Post(fr, stack, xPosition, yPosition, text));
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.scale(FFF)V", shift = At.Shift.BY, by = 1))
    public void renderItemPre(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        if (!Utils.inSkyblock) return;
        if (Skytils.config.largerHeads && stack.getItem() == Items.skull) {
            float factor = 1.2f;
            GlStateManager.scale(factor, factor, factor);
        }
    }
}
