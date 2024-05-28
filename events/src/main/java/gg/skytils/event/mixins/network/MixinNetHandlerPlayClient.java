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

package gg.skytils.event.mixins.network;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.network.ClientConnectEvent;
import gg.skytils.event.impl.play.ActionBarReceivedEvent;
import gg.skytils.event.impl.play.ChatMessageReceivedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handleJoinGame", at = @At("RETURN"), remap = false)
    public void onConnect(CallbackInfo ci) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            EventsKt.postSync(new ClientConnectEvent());
        });
    }

    @Inject(method = "handleChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;printChatMessage(Lnet/minecraft/util/IChatComponent;)V"), cancellable = true)
    public void onChat(CallbackInfo ci, @Local(argsOnly = true) LocalRef<S02PacketChat> packet) {
        ChatMessageReceivedEvent event = new ChatMessageReceivedEvent(packet.get().getChatComponent());
        if (EventsKt.postCancellableSync(event)) {
            ci.cancel();
        }
        packet.set(new S02PacketChat(event.getMessage(), packet.get().getType()));
    }

    @Inject(method = "handleChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;setRecordPlaying(Lnet/minecraft/util/IChatComponent;Z)V"), cancellable = true)
    public void onActionbar(CallbackInfo ci, @Local(argsOnly = true) LocalRef<S02PacketChat> packet) {
        ActionBarReceivedEvent event = new ActionBarReceivedEvent(packet.get().getChatComponent());
        if (EventsKt.postCancellableSync(event)) {
            ci.cancel();
        }
        packet.set(new S02PacketChat(event.getMessage(), packet.get().getType()));
    }
}
