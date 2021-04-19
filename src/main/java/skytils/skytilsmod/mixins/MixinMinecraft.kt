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
package skytils.skytilsmod.mixins

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.resources.IReloadableResourceManager
import net.minecraft.client.resources.LanguageManager
import net.minecraft.client.settings.GameSettings
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.Utils.isOnHypixel
import skytils.skytilsmod.utils.graphics.ScreenRenderer

@Mixin(Minecraft::class)
abstract class MixinMinecraft {
    @Shadow
    var thePlayer: EntityPlayerSP? = null

    @Shadow
    var gameSettings: GameSettings? = null

    @get:Shadow
    abstract val isUnicode: Boolean

    @Shadow
    private val mcLanguageManager: LanguageManager? = null

    @Shadow
    private val mcResourceManager: IReloadableResourceManager? = null
    private val that = this as Minecraft

    /**
     * Taken from Skyblockcatia under MIT License
     * Modified
     * https://github.com/SteveKunG/SkyBlockcatia/blob/1.8.9/LICENSE.md
     *
     * @author SteveKunG
     */
    @Inject(
        method = ["runGameLoop()V"],
        at = [At(
            value = "INVOKE",
            target = "net/minecraft/client/renderer/EntityRenderer.updateCameraAndRender(FJ)V",
            shift = At.Shift.AFTER
        )]
    )
    private fun runGameLoop(info: CallbackInfo) {
        GuiManager.toastGui.drawToast(ScaledResolution(that))
    }

    @Inject(
        method = ["clickMouse()V"],
        at = [At(
            value = "INVOKE",
            target = "net/minecraft/client/entity/EntityPlayerSP.swingItem()V",
            shift = At.Shift.AFTER
        )]
    )
    private fun clickMouse(info: CallbackInfo) {
        if (!isOnHypixel || !Utils.inSkyblock) return
        val item = thePlayer!!.heldItem
        if (item != null) {
            val extraAttr = getExtraAttributes(item)
            val itemId = getSkyBlockItemID(extraAttr)
            if (itemId == "BLOCK_ZAPPER") {
                Skytils.sendMessageQueue.add("/undozap")
            }
        }
    }

    @Inject(
        method = ["startGame"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/IReloadableResourceManager;registerReloadListener(Lnet/minecraft/client/resources/IResourceManagerReloadListener;)V",
            shift = At.Shift.AFTER,
            ordinal = 1
        )]
    )
    private fun initializeSmartFontRenderer(ci: CallbackInfo) {
        if (gameSettings!!.language != null) {
            ScreenRenderer.fontRenderer.unicodeFlag = isUnicode
            ScreenRenderer.fontRenderer.bidiFlag = mcLanguageManager!!.isCurrentLanguageBidirectional
        }
        mcResourceManager!!.registerReloadListener(ScreenRenderer.fontRenderer)
    }
}