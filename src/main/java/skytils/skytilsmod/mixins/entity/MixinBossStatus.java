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

package skytils.skytilsmod.mixins.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.events.BossBarEvent;

@Mixin(BossStatus.class)
public abstract class MixinBossStatus {

    @Inject(method = "setBossStatus", at = @At("HEAD"), cancellable = true)
    private static void onSetBossStatus(IBossDisplayData displayData, boolean hasColorModifierIn, CallbackInfo ci) {
        try {
            if (MinecraftForge.EVENT_BUS.post(new BossBarEvent.Set(displayData, hasColorModifierIn))) ci.cancel();
        } catch (Throwable e) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cSkytils caught and logged an exception at BossBarEvent.Set. Please report this on the Discord server."));
            e.printStackTrace();
        }
    }
}