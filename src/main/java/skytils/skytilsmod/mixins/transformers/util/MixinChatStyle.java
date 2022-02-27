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

package skytils.skytilsmod.mixins.transformers.util;

import net.minecraft.util.ChatStyle;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import skytils.skytilsmod.features.impl.handlers.ChatTabs;
import skytils.skytilsmod.mixins.extensions.ExtensionChatStyle;

@Mixin(ChatStyle.class)
public class MixinChatStyle implements ExtensionChatStyle {
    @Unique
    ChatTabs.ChatTab[] chatTab = null;

    @Unique
    @Override
    public ChatTabs.ChatTab[] getChatTabType() {
        return chatTab;
    }

    @Unique
    @Override
    public void setChatTabType(ChatTabs.ChatTab[] type) {
        chatTab = type;
    }
}
