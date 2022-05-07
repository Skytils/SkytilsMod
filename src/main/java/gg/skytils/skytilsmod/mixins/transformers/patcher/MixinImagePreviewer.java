/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package gg.skytils.skytilsmod.mixins.transformers.patcher;

import gg.essential.api.EssentialAPI;
import gg.essential.api.utils.TrustedHostsUtil;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.URL;

@Pseudo
@Mixin(targets = "club.sk1er.patcher.screen.render.overlay.ImagePreview", remap = false)
public class MixinImagePreviewer {

    private boolean missingImgur = false;
    private boolean checkedImgur = false;

    @Dynamic
    @Inject(method = "handle", at = @At(value = "RETURN", ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onHandle(String value, CallbackInfo ci, final URL url, final String host, boolean found) {
        if (!checkedImgur) {
            if (EssentialAPI.getOnboardingData().hasDeniedEssentialTOS()) {
                missingImgur = true;
                checkedImgur = true;
                return;
            }
            for (TrustedHostsUtil.TrustedHost trustedHost : EssentialAPI.getTrustedHostsUtil().getTrustedHosts()) {
                for (String domain : trustedHost.getDomains()) {
                    if (domain.equalsIgnoreCase("imgur.com")) {
                        checkedImgur = true;
                        return;
                    }
                }
            }
            missingImgur = true;
            checkedImgur = true;
        }
        if (!missingImgur) return;
        if (!found) {
            if (host.equalsIgnoreCase("imgur.com") || host.equalsIgnoreCase("i.imgur.com")) {
                //noinspection UnusedAssignment
                found = true;
            }
        }
    }
}
