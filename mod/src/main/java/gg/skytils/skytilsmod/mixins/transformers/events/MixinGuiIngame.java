/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.mixins.transformers.events;

import gg.skytils.event.EventsKt;
import gg.skytils.skytilsmod._event.RenderHUDEvent;
import gg.skytils.skytilsmod.utils.GlState;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if FORGE
import net.minecraftforge.client.GuiIngameForge;

@Mixin(GuiIngameForge.class)
//#else
//$$ @Mixin(InGameHud.class)
//#endif
public class MixinGuiIngame {
    @Inject(method = "renderTooltip", at = @At("TAIL"))
    public void render(CallbackInfo ci) {
        GlState.Companion.pushState();
        EventsKt.postSync(new RenderHUDEvent());
        GlState.Companion.popState();
    }
}
