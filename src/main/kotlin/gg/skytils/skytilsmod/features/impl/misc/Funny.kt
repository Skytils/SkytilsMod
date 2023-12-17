/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UChat
import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.gui.elements.GIFResource
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.getSkytilsResource
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.MathHelper
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage.ModList

object Funny {
    var ticks = 0
    var alphaMult = 0f
    var cheaterSnitcher = false

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onItemUse(event: PlayerInteractEvent) {
        if (!Utils.inSkyblock || !SuperSecretSettings.tryItAndSee || event.entityPlayer?.heldItem == null) return
        (event.entityPlayer as? EntityPlayerSP)?.dropOneItem(true)
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (SuperSecretSettings.bennettArthur) {
            if (++ticks >= 360) ticks = 0
            alphaMult = MathHelper.sin(ticks * 0.0174533f).coerceAtLeast(0f)
        } else {
            ticks = 0
            alphaMult = 1f
        }
    }

    fun joinedSkyblock() {
        if (!Utils.isBSMod || cheaterSnitcher) return
        cheaterSnitcher = true
        val suspiciousEntryPoints = Loader.instance().activeModList
        val classification = suspiciousEntryPoints.mapTo(hashSetOf()) { it.modId }
        val machineLearningModel = ModList(suspiciousEntryPoints).modList().keys
        if (classification.size != machineLearningModel.size) {
            Skytils.sendMessageQueue.addFirst("/lobby ptl")

            tickTimer(10) {
                val cheetos = classification - machineLearningModel

                UChat.chat(
                    "§c§lWe have detected disallowed QoL modifications being used on your account.\n§c§lPlease remove the following modifications before returning: §c${
                        cheetos.joinToString(
                            ", "
                        )
                    }."
                )
                UMessage(
                    UTextComponent("§e§nhttps://hypixel.net/threads/update-to-disallowed-modifications-qol-modifications.4043482/").setClick(
                        MCClickEventAction.OPEN_URL,
                        "https://hypixel.net/threads/4043482/"
                    )
                ).chat()

                UChat.chat("§cA kick occurred in your connection, so you were put in the SkyBlock lobby!")
            }
        }
    }

    init {
        GuiManager.registerElement(JamCatElement)
    }

    object JamCatElement : GuiElement("Jamcat", x = 0, y = 0) {
        val gif by lazy {
            GIFResource(getSkytilsResource("splashes/jamcat.gif"), frameDelay = 5)
        }

        override fun render() {
            if (!toggled) return
            gif.draw()
        }

        override fun demoRender() = render()

        override val toggled: Boolean
            get() = SuperSecretSettings.jamCat
        override val height: Int = 128
        override val width: Int = 128
    }
}