/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.mixins.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiRenderItemEvent;
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.NEUCompatibility;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    private final Minecraft mc = Minecraft.getMinecraft();

    @Shadow @Final private static ResourceLocation RES_ITEM_GLINT;

    @Shadow @Final private TextureManager textureManager;

    @Shadow protected abstract void renderModel(IBakedModel model, int color);

    @Inject(method = "renderItemIntoGUI", at = @At("HEAD"))
    private void renderRarity(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (Utils.inSkyblock && Skytils.config.showItemRarity) {
            if (mc.currentScreen != null) {
                String name = mc.currentScreen.getClass().getName();
                if (NEUCompatibility.INSTANCE.isStorageMenuActive() || NEUCompatibility.INSTANCE.isTradeWindowActive() || name.equals("io.github.moulberry.notenoughupdates.auction.CustomAHGui")) {
                    RenderUtil.renderRarity(stack, x, y);
                }
            }
        }
    }

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("RETURN"))
    private void renderItemOverlayPost(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        try {
            MinecraftForge.EVENT_BUS.post(new GuiRenderItemEvent.RenderOverlayEvent.Post(fr, stack, xPosition, yPosition, text));
        } catch (Throwable e) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cSkytils caught and logged an exception at GuiRenderItemEvent.RenderOverlayEvent.Post. Please report this on the Discord server."));
            e.printStackTrace();
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.scale(FFF)V", shift = At.Shift.AFTER))
    private void renderItemPre(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        if (!Utils.inSkyblock) return;
        if (stack.getItem() == Items.skull) {
            double scale = Skytils.config.largerHeadScale;
            GlStateManager.scale(scale, scale, scale);
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderEffect(Lnet/minecraft/client/resources/model/IBakedModel;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void modifyGlintRendering(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        if (Utils.inSkyblock) {
            String itemId = ItemUtil.getSkyBlockItemID(stack);
            if (GlintCustomizer.glintColors.containsKey(itemId)) {
                int color = GlintCustomizer.glintColors.get(itemId).toInt();

                GlStateManager.depthMask(false);
                GlStateManager.depthFunc(514);
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(768, 1);
                this.textureManager.bindTexture(RES_ITEM_GLINT);
                GlStateManager.matrixMode(5890);
                GlStateManager.pushMatrix();
                GlStateManager.scale(8.0F, 8.0F, 8.0F);
                float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
                GlStateManager.translate(f, 0.0F, 0.0F);
                GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
                this.renderModel(model, color);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.scale(8.0F, 8.0F, 8.0F);
                float f1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
                GlStateManager.translate(-f1, 0.0F, 0.0F);
                GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
                this.renderModel(model, color);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5888);
                GlStateManager.blendFunc(770, 771);
                GlStateManager.enableLighting();
                GlStateManager.depthFunc(515);
                GlStateManager.depthMask(true);
                this.textureManager.bindTexture(TextureMap.locationBlocksTexture);

                ci.cancel();

                //Since we prematurely exited, we need to reset the matrices
                GlStateManager.popMatrix();
            }
        }
    }

}
