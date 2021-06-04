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

package skytils.skytilsmod.mixins.crash;

import net.minecraft.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    public boolean isSkytilsCrash = false;

    private boolean usingForCompleteReport = false;

    @Shadow public abstract String getCauseStackTraceOrString();

    @Redirect(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", remap = false, ordinal = 0))
    private StringBuilder blameSkytils(StringBuilder stringBuilder, String str) {
        stringBuilder.append(str);
        String cause = getCauseStackTraceOrString();
        if (cause.contains("skytils.skytilsmod") && !cause.contains("use the latest Forge")) {
            isSkytilsCrash = true;
            stringBuilder.append("Skytils may have caused this crash.\nJoin the Discord for support at discord.gg/skytils\n");
        }
        return stringBuilder;
    }

    @ModifyArg(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", ordinal = 2, remap = false))
    private String replaceWittyComment(String comment) {
        if (isSkytilsCrash) {
            comment = "Did Sychic do that?";
        }
        return comment;
    }

    @Inject(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Lnet/minecraft/crash/CrashReport;getCauseStackTraceOrString()Ljava/lang/String;"))
    private void preStackTrace(CallbackInfoReturnable<String> cir) {
        usingForCompleteReport = true;
    }

    @Inject(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Lnet/minecraft/crash/CrashReport;getCauseStackTraceOrString()Ljava/lang/String;", shift = At.Shift.AFTER))
    private void postStackTrace(CallbackInfoReturnable<String> cir) {
        usingForCompleteReport = false;
    }

    @Inject(method = "getCauseStackTraceOrString", at = @At("HEAD"), cancellable = true)
    private void replaceCauseForLauncher(CallbackInfoReturnable<String> cir) {
        if (usingForCompleteReport && isSkytilsCrash) {
            usingForCompleteReport = false;
            cir.setReturnValue("Skytils may have caused this crash. Please send the full report below by clicking \"View crash report\" to discord.gg/skytils in the #support channel. Taking a screenshot of this screen provides no information to any mod developers.\n\n" + getCauseStackTraceOrString());
        }
    }

}
