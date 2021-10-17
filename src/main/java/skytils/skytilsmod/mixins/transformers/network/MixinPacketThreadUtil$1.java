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

package skytils.skytilsmod.mixins.transformers.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.events.impl.MainReceivePacketEvent;

@Mixin(targets = "net.minecraft.network.PacketThreadUtil$1", remap = false)
public abstract class MixinPacketThreadUtil$1 {
    @Shadow @Final
    INetHandler val$p_180031_1_;

    @Shadow @Final
    Packet<INetHandler> val$p_180031_0_;

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void onQueuePacket(CallbackInfo ci) {
        if (new MainReceivePacketEvent<>(val$p_180031_1_, val$p_180031_0_).postAndCatch()) ci.cancel();
    }
}
