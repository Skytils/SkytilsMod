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

import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.network.ClientDisconnectEvent;
import io.netty.channel.local.LocalEventLoopGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.LazyLoadBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class ClientDisconnectEvent_MixinNetworkManager {

    @Shadow private INetHandler packetListener;

    @Shadow @Final public static LazyLoadBase<LocalEventLoopGroup> CLIENT_LOCAL_EVENTLOOP;

    @Inject(method = "channelInactive", at = @At("HEAD"), remap = false)
    public void channelInactive(CallbackInfo ci) {
        if (this.packetListener instanceof NetHandlerPlayClient) {
            skytils$postDisconnectEvent();
        }
    }

    @Inject(method = "checkDisconnected", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/INetHandler;onDisconnect(Lnet/minecraft/util/IChatComponent;)V"))
    public void onDisconnect(CallbackInfo ci) {
        if (this.packetListener instanceof NetHandlerPlayClient) {
            skytils$postDisconnectEvent();
        }
    }

    @Unique
    private void skytils$postDisconnectEvent() {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            EventsKt.postSync(new ClientDisconnectEvent());
        });
    }
}
