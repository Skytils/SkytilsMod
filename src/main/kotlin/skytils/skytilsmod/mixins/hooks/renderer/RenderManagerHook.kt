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
package skytils.skytilsmod.mixins.hooks.renderer

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import skytils.skytilsmod.events.CheckRenderEntityEvent

fun shouldRender(
    entityIn: Entity,
    camera: ICamera,
    camX: Double,
    camY: Double,
    camZ: Double,
    cir: CallbackInfoReturnable<Boolean>
) {
    try {
        if (MinecraftForge.EVENT_BUS.post(
                CheckRenderEntityEvent(
                    entityIn,
                    camera,
                    camX,
                    camY,
                    camZ
                )
            )
        ) cir.returnValue = false
    } catch (e: Throwable) {
        Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils caught and logged an exception at CheckRenderEntityEvent. Please report this on the Discord server."))
        e.printStackTrace()
    }
}