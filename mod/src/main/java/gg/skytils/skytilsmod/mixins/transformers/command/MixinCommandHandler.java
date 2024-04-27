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

package gg.skytils.skytilsmod.mixins.transformers.command;

import gg.skytils.skytilsmod.features.impl.handlers.CommandAliases;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CommandHandler.class)
public abstract class MixinCommandHandler implements ICommandManager {
    @Inject(method = "getTabCompletionOptions", at = @At(value = "RETURN", ordinal = 0))
    private void addTabCompletableCommands(ICommandSender sender, String input, BlockPos pos, CallbackInfoReturnable<List<String>> cir) {
        List<String> list = cir.getReturnValue();
        for (String cmd : CommandAliases.INSTANCE.getAliases().keySet()) {
            if (cmd.startsWith(input)) list.add(cmd);
        }
    }
}
