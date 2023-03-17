/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2023 Skytils
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

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.util.SBInfo", remap = false)
public class MixinSBInfo {
    @Dynamic
    @ModifyConstant(method = "tick", constant = @Constant(stringValue = "Dungeon", ordinal = 0), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScorePlayerTeam;func_96667_a(Lnet/minecraft/scoreboard/Team;Ljava/lang/String;)Ljava/lang/String;"),
            to = @At(value = "FIELD", target = "Lio/github/moulberry/notenoughupdates/util/SBInfo;isInDungeon:Z", opcode = Opcodes.PUTFIELD)
    ), allow = 1)
    private String modifyDungeon(String dungeon) {
        return "Cleared:";
    }
}
