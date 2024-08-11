/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.event.mixins.gui;

import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.play.ChatMessageSentEvent;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        if (EventsKt.postCancellableSync(new ChatMessageSentEvent(chatText, addToHistory))) {
            cir.setReturnValue(true); // returning a value of false prevents the chat window from closing, but this wouldn't match 1.8.9 behavior
        }
    }
}
