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

package skytils.skytilsmod.mixins.transformers.command;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import skytils.skytilsmod.features.impl.handlers.CommandAliases;

import java.util.List;

@Mixin(CommandHandler.class)
public abstract class MixinCommandHandler implements ICommandManager {
    @ModifyVariable(method = "getTabCompletionOptions", at = @At(value = "STORE"), ordinal = 0)
    private List<String> addTabCompletableCommands(List<String> list) {
        list.addAll(CommandAliases.Companion.getAliases().keySet());
        return list;
    }
}
