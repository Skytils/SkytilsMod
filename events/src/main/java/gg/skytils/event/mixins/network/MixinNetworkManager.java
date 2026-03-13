/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinNetworkManager {

    @Shadow private PacketListener packetListener;

    @Inject(method = "channelInactive", at = @At("HEAD"), remap = false)
    public void channelInactive(CallbackInfo ci) {
        if (this.packetListener instanceof ClientPlayNetworkHandler) {
            skytils$postDisconnectEvent();
        }
    }

    @Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/listener/PacketListener;onDisconnected(Lnet/minecraft/network/DisconnectionInfo;)V"))
    public void onDisconnect(CallbackInfo ci) {
        if (this.packetListener instanceof ClientPlayNetworkHandler) {
            skytils$postDisconnectEvent();
        }
    }

    @Unique
    private void skytils$postDisconnectEvent() {
        MinecraftClient.getInstance()
        //#if MC>=12000
        .execute(
        //#else
        //$$ .submit(
        //#endif
            () -> {
                EventsKt.postSync(new ClientDisconnectEvent());
            }
        );
    }
}
