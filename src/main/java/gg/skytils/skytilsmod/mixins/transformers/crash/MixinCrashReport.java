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

package gg.skytils.skytilsmod.mixins.transformers.crash;

import gg.skytils.skytilsmod.mixins.hooks.crash.CrashReportHook;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = CrashReport.class, priority = 988)
public abstract class MixinCrashReport {

    @Shadow
    @Final
    private CrashReportCategory theReportCategory;

    @Unique
    private final CrashReportHook hook = new CrashReportHook((CrashReport) (Object) this);

    @Inject(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", shift = At.Shift.AFTER, remap = false, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void thereIsNoOtherWay(CallbackInfoReturnable<String> cir, StringBuilder stringbuilder) {
        hook.checkSkytilsCrash(cir, stringbuilder);
    }

    @ModifyArg(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", remap = false, ordinal = 10))
    private String otherReplaceCauseForLauncher(String theCauseStackTraceOrString) {
        return hook.generateCauseForLauncher(theCauseStackTraceOrString);
    }

    @ModifyArg(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", ordinal = 2, remap = false, args = "matches=method::getWittyComment"))
    private String replaceWittyComment(String comment) {
        return hook.generateWittyComment(comment);
    }


    @Inject(method = "populateEnvironment", at = @At("RETURN"))
    private void addDataToCrashReport(CallbackInfo ci) {
        hook.addDataToCrashReport(this.theReportCategory);
    }
}
