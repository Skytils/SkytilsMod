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

package gg.skytils.skytilsmod.mixins.transformers.events;

import com.llamalad7.mixinextras.sugar.Local;
import gg.skytils.event.EventsKt;
import gg.skytils.skytilsmod._event.MainThreadPacketReceiveEvent;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.NetworkThreadUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(NetworkThreadUtils.class)
public class MixinPacketThreadUtil {
    @ModifyArg(method = "forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/ThreadExecutor;executeSync(Ljava/lang/Runnable;)V"))
    private static Runnable processPacket(Runnable var1, @Local(argsOnly = true) Packet<?> packet) {
        return () -> {
            if (!EventsKt.postCancellableSync(new MainThreadPacketReceiveEvent(packet))) {
                var1.run();
            }
        };
    }
}
