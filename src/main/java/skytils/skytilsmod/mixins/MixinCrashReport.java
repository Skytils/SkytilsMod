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

import net.minecraft.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.FileWriter;
import java.io.IOException;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    public boolean isSkytilsCrash = false;

    @Shadow public abstract String getCauseStackTraceOrString();

    @Shadow private StackTraceElement[] stacktrace;

    @Redirect(method = "getCompleteReport", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", remap = false, ordinal = 0))
    private StringBuilder blameSkytils(StringBuilder stringBuilder, String str) {
        stringBuilder.append(str);
        if (getCauseStackTraceOrString().contains("skytils.skytilsmod")) {
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

}
