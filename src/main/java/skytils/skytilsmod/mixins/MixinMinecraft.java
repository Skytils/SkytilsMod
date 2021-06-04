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

package skytils.skytilsmod.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;

import java.io.File;
import java.util.Objects;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Shadow
    public EntityPlayerSP thePlayer;

    @Shadow public GameSettings gameSettings;

    @Shadow public abstract boolean isUnicode();

    @Shadow private LanguageManager mcLanguageManager;
    @Shadow private IReloadableResourceManager mcResourceManager;
    @Shadow @Final public File mcDataDir;
    private final Minecraft that = (Minecraft) (Object) this;

    /**
     * Taken from Skyblockcatia under MIT License
     * Modified
     * https://github.com/SteveKunG/SkyBlockcatia/blob/1.8.9/LICENSE.md
     *
     * @author SteveKunG
     */
    @Inject(method = "runGameLoop()V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/EntityRenderer.updateCameraAndRender(FJ)V", shift = At.Shift.AFTER))
    private void runGameLoop(CallbackInfo info) {
        GuiManager.toastGui.drawToast(new ScaledResolution(this.that));
    }

    @Inject(method = "clickMouse()V", at = @At(value = "INVOKE", target = "net/minecraft/client/entity/EntityPlayerSP.swingItem()V", shift = At.Shift.AFTER))
    private void clickMouse(CallbackInfo info) {
        if (!Utils.isOnHypixel() || !Utils.inSkyblock) return;

        ItemStack item = thePlayer.getHeldItem();
        if (item != null) {
            NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
            String itemId = ItemUtil.getSkyBlockItemID(extraAttr);

            if (Objects.equals(itemId, "BLOCK_ZAPPER")) {
                Skytils.sendMessageQueue.add("/undozap");
            }
        }
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/IReloadableResourceManager;registerReloadListener(Lnet/minecraft/client/resources/IResourceManagerReloadListener;)V", shift = At.Shift.AFTER, ordinal = 1))
    private void initializeSmartFontRenderer(CallbackInfo ci) {
        if (this.gameSettings.language != null) {
            ScreenRenderer.fontRenderer.setUnicodeFlag(this.isUnicode());
            ScreenRenderer.fontRenderer.setBidiFlag(this.mcLanguageManager.isCurrentLanguageBidirectional());
        }
        this.mcResourceManager.registerReloadListener(ScreenRenderer.fontRenderer);
    }
}