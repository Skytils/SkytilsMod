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

package skytils.skytilsmod.mixins.transformers.gui;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import skytils.skytilsmod.mixins.extensions.ExtensionChatLine;

@Mixin(ChatLine.class)
public class MixinChatLine implements ExtensionChatLine {

    private IChatComponent fullComponent = null;

    @NotNull
    @Override
    public IChatComponent getFullComponent() {
        return fullComponent;
    }

    @Override
    public void setFullComponent(@NotNull IChatComponent fullComponent) {
        this.fullComponent = fullComponent;
    }

    @NotNull
    @Override
    public ChatLine withFullComponent(@NotNull IChatComponent fullComponent) {
        this.fullComponent = fullComponent;
        return (ChatLine) (Object) this;
    }
}
