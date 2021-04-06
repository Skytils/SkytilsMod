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

package skytils.skytilsmod.utils.toasts;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;

import java.nio.FloatBuffer;

public class ComboEndToast implements IToast<ComboToast> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("skytils:gui/toast.png");
    private static final ResourceLocation ICON = new ResourceLocation("skytils:toasts/combo/comboFail.png");
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
    private final long maxDrawTime;

    public ComboEndToast() {
        this.maxDrawTime = Skytils.config.toastTime;
    }

    @Override
    public IToast.Visibility draw(GuiToast toastGui, long delta) {
        toastGui.mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);

        toastGui.mc.fontRendererObj.drawStringWithShadow("§r§c§lCombo Failed!", 30, 7, 16777215);

        GuiToast.drawSubline(toastGui, delta, 0L, this.maxDrawTime, this.buffer, "", false);
        RenderHelper.enableGUIStandardItemLighting();

        RenderUtil.renderTexture(ICON, 8, 8);

        GlStateManager.disableLighting();

        return delta >= this.maxDrawTime ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }
}
