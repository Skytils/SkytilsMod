/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.mixins.transformers.neu;

import net.minecraft.client.shader.Framebuffer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.miscgui.StorageOverlay")
public class MixinStorageOverlay {

    @Shadow(remap = false)
    private Framebuffer framebuffer;

    @SuppressWarnings({"UnqualifiedMemberReference"})
    @Dynamic
    @Inject(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lio/github/moulberry/notenoughupdates/miscgui/StorageOverlay;framebuffer",
                    shift = At.Shift.AFTER,
                    opcode = Opcodes.PUTFIELD,
                    remap = false
            ),
            remap = false
    )
    public void onSetFramebuffer(CallbackInfo ci) {
        framebuffer.enableStencil();
    }

}
