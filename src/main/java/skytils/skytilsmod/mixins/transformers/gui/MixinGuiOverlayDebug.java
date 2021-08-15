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

package skytils.skytilsmod.mixins.transformers.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import skytils.skytilsmod.mixins.transformers.accessors.AccessorWorldInfo;

@Mixin(GuiOverlayDebug.class)
public class MixinGuiOverlayDebug extends Gui {
    @Redirect(method = "call", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getWorldTime()J"))
    private long returnRealWorldTime(WorldClient worldClient) {
        return ((AccessorWorldInfo)worldClient.getWorldInfo()).getRealWorldTime();
    }
}
