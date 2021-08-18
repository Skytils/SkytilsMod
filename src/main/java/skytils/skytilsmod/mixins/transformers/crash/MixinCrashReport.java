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

package skytils.skytilsmod.mixins.transformers.crash;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import skytils.skytilsmod.utils.Utils;

@Mixin(value = CrashReport.class, priority = 988)
public abstract class MixinCrashReport {

    public boolean isSkytilsCrash = false;

    @Shadow public abstract String getCauseStackTraceOrString();

    @Shadow @Final private CrashReportCategory theReportCategory;

    @Inject(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", args = "ldc=// ;captureargs=true", remap = false, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void thereIsNoOtherWay(CallbackInfoReturnable<String> cir, StringBuilder stringbuilder) {
        String cause = getCauseStackTraceOrString();
        if (cause.contains("skytils.skytilsmod") && !cause.contains("use the latest Forge")) {
            isSkytilsCrash = true;
            stringbuilder.append("Skytils may have caused this crash.\nJoin the Discord for support at discord.gg/skytils\n");
        }
    }

    @ModifyArg(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", remap = false, ordinal = 10, args = "matches=method::getCauseStackTraceOrString"))
    private String otherReplaceCauseForLauncher(String theCauseStackTraceOrString) {
        if (isSkytilsCrash) {
            return "Skytils may have caused this crash. Please send the full report below by clicking \"View crash report\" to discord.gg/skytils in the #support channel. Taking a screenshot of this screen provides no information to any mod developers.\n\n";
        }
        return theCauseStackTraceOrString;
    }

    @ModifyArg(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", ordinal = 2, remap = false, args = "matches=method::getWittyComment"))
    private String replaceWittyComment(String comment) {
        if (isSkytilsCrash) {
            comment = "Did Sychic do that?";
        }
        return comment;
    }


    @Inject(method = "populateEnvironment", at = @At("RETURN"))
    private void addDataToCrashReport(CallbackInfo ci) {
        Utils.INSTANCE.generateDebugInfo(this.theReportCategory);
    }
}
