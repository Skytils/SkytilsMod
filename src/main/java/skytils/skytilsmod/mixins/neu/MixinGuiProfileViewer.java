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

package skytils.skytilsmod.mixins.neu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer")
public abstract class MixinGuiProfileViewer extends GuiScreen {

    @Shadow(remap = false)
    private int guiLeft;
    @Shadow(remap = false)
    private int guiTop;

    @Inject(method = "drawInvsPage", at = @At(value = "INVOKE", target = "Lio/github/moulberry/notenoughupdates/util/Utils;drawItemStack(Lnet/minecraft/item/ItemStack;II)V", remap = false, ordinal = 0), remap = false, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderRarityOnInvPage(int mouseX, int mouseY, float partialTicks, CallbackInfo ci, JsonObject inventoryInfo, int invNameIndex, int i, ItemStack stack) {
        if (Skytils.config.showItemRarity) {
            RenderUtil.renderRarity(stack, guiLeft+173, guiTop+67-18*i);
        }
    }

    @Inject(method = "drawInvsPage", at = @At(value = "INVOKE", target = "Lio/github/moulberry/notenoughupdates/util/Utils;drawItemStack(Lnet/minecraft/item/ItemStack;II)V", remap = false, ordinal = 1), remap = false, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderRarityOnInvPage1(int mouseX, int mouseY, float partialTicks, CallbackInfo ci, JsonObject inventoryInfo, int invNameIndex, ItemStack[][][] inventories, ItemStack[][] inventory, int i, ItemStack stack) {
        if (Skytils.config.showItemRarity) {
            RenderUtil.renderRarity(stack, guiLeft+143, guiTop+13+18*i);
        }
    }

    @Inject(method = "drawInvsPage", at = @At(value = "INVOKE", target = "Lio/github/moulberry/notenoughupdates/util/Utils;drawItemStack(Lnet/minecraft/item/ItemStack;II)V", remap = false, ordinal = 2), remap = false, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderRarityOnInvPage2(int mouseX, int mouseY, float partialTicks, CallbackInfo ci, JsonObject inventoryInfo, int invNameIndex, ItemStack[][][] inventories, ItemStack[][] inventory, int i, ItemStack stack) {
        if (Skytils.config.showItemRarity) {
            RenderUtil.renderRarity(stack, guiLeft+143, guiTop+137+18*i);
        }
    }

    @Inject(method = "drawInvsPage", at = @At(value = "INVOKE", target = "Lio/github/moulberry/notenoughupdates/util/Utils;drawItemStack(Lnet/minecraft/item/ItemStack;II)V", remap = false, ordinal = 3), remap = false, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderRarityOnInvPage3(int mouseX, int mouseY, float partialTicks, CallbackInfo ci, JsonObject inventoryInfo, int invNameIndex, ItemStack[][][] inventories, ItemStack[][] inventory, int inventoryRows, int invSizeY, int x, int y, boolean leftHovered, boolean rightHovered, ItemStack stackToRender, int overlay, int yIndex, int xIndex, ItemStack stack) {
        if (Skytils.config.showItemRarity) {
            RenderUtil.renderRarity(stack, x+8+xIndex*18, y+18+yIndex*18);
        }
    }

    @Inject(method = "drawPetsPage", at = @At(value = "INVOKE", target = "Lio/github/moulberry/notenoughupdates/util/Utils;drawItemStack(Lnet/minecraft/item/ItemStack;II)V", remap = false, ordinal = 0), remap = false, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void renderRarityOnPetsPage(int mouseX, int mouseY, float partialTicks, CallbackInfo ci, JsonObject petsInfo, JsonObject petsJson, String location, JsonObject status, String panoramaIdentifier, JsonArray pets, JsonElement activePetElement, boolean leftHovered, boolean rightHovered, int i, JsonObject pet, ItemStack stack, int xIndex, int yIndex, float x, float y) {
        if (Skytils.config.showItemRarity) {
            RenderUtil.renderRarity(stack, this.guiLeft + (int)x + 2, this.guiTop + (int)y + 2);
        }
    }
}
