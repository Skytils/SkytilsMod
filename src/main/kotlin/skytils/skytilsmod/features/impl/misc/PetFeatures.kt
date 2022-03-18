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
package skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UChat
import gg.essential.universal.wrappers.message.UTextComponent
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.impl.CheckRenderEntityEvent
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.events.impl.PacketEvent.SendEvent
import skytils.skytilsmod.events.impl.SendChatMessageEvent
import skytils.skytilsmod.utils.ItemUtil.getItemLore
import skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import skytils.skytilsmod.utils.RenderUtil.highlight
import skytils.skytilsmod.utils.RenderUtil.renderTexture
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.Utils.isInTablist
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import skytils.skytilsmod.utils.setHoverText
import skytils.skytilsmod.utils.stripControlCodes
import java.util.regex.Pattern

class PetFeatures {
    val petItems = HashMap<String, Boolean>()

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityArmorStand) {
            val entity = event.entity
            val name = entity.customNameTag
            if (Skytils.config.hidePetNametags && name.startsWith("§8[§7Lv") && name.contains(
                    "'s "
                ) && !name.contains("❤") && !name.contains("Hit")
            ) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type.toInt() == 2) return
        val message = event.message.formattedText
        if (message.startsWith("§r§aYou despawned your §r§")) {
            lastPet = null
        } else if (message.startsWith("§r§aYou summoned your §r")) {
            val petMatcher = SUMMON_PATTERN.matcher(message)
            if (petMatcher.find()) {
                lastPet = petMatcher.group("pet").stripControlCodes()
            } else UChat.chat("§cSkytils failed to capture equipped pet.")
        } else if (message.startsWith("§cAutopet §eequipped your §7[Lvl ")) {
            val autopetMatcher = AUTOPET_PATTERN.matcher(message)
            if (autopetMatcher.find()) {
                lastPet = autopetMatcher.group("pet").stripControlCodes()
            } else UChat.chat("§cSkytils failed to capture equipped pet.")
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onDraw(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inSkyblock || event.container !is ContainerChest) return
        if (Skytils.config.highlightActivePet && (SBInfo.lastOpenContainerName?.endsWith(") Pets") == true || SBInfo.lastOpenContainerName == "Pets") && event.slot.hasStack && event.slot.slotNumber in 10..43) {
            val item = event.slot.stack
            for (line in getItemLore(item)) {
                if (line.startsWith("§7§cClick to despawn")) {
                    GlStateManager.translate(0f, 0f, 3f)
                    event.slot highlight Skytils.config.activePetColor
                    GlStateManager.translate(0f, 0f, -3f)
                    break
                }
            }
        }
    }

    @SubscribeEvent
    fun onSendPacket(event: SendEvent) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.petItemConfirmation && (event.packet is C02PacketUseEntity || event.packet is C08PacketPlayerBlockPlacement)) {
            val item = mc.thePlayer.heldItem ?: return
            val itemId = getSkyBlockItemID(item) ?: return
            if (itemId !in petItems) {
                val isPetItem =
                    (itemId.contains("PET_ITEM") && !itemId.endsWith("_DROP")) || itemId.endsWith("CARROT_CANDY") || itemId.startsWith(
                        "PET_SKIN_"
                    ) || getItemLore(item).asReversed().any {
                        it.contains("PET ITEM")
                    }
                petItems[itemId] = isPetItem
            }
            if (petItems[itemId] == true) {
                if (System.currentTimeMillis() - lastPetConfirmation > 5000) {
                    event.isCanceled = true
                    if (System.currentTimeMillis() - lastPetLockNotif > 10000) {
                        lastPetLockNotif = System.currentTimeMillis()
                        UChat.chat(
                            UTextComponent("§cSkytils stopped you from using that pet item! §6Click this message to disable the lock.").setHoverText(
                                "Click to disable the pet item lock for 5 seconds."
                            ).setClick(ClickEvent.Action.RUN_COMMAND, "/disableskytilspetitemlock")
                        )
                    }
                } else {
                    lastPetConfirmation = 0
                }
            }
        }
    }

    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        if (event.message == "/disableskytilspetitemlock" && !event.addToChat) {
            lastPetConfirmation = System.currentTimeMillis()
            UChat.chat("§aYou may now apply pet items for 5 seconds.")
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!Utils.inSkyblock || event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return

        if (Skytils.config.dolphinPetDisplay && lastPet == "Dolphin") {
            dolphinPlayers = mc.theWorld.getPlayers<EntityPlayer>(
                EntityOtherPlayerMP::class.java
            ) { p: EntityPlayer? ->
                p != null && p !== mc.thePlayer && p.getDistanceSqToEntity(mc.thePlayer) <= 10 * 10 && p.uniqueID.version() != 2 && isInTablist(
                    p
                )
            }.size
        }
    }

    companion object {
        private var lastPetConfirmation: Long = 0
        private var lastPetLockNotif: Long = 0
        var lastPet: String? = null
        private val SUMMON_PATTERN = Pattern.compile("§r§aYou summoned your §r(?<pet>.+)§r§a!§r")
        private val AUTOPET_PATTERN =
            Pattern.compile("§cAutopet §eequipped your §7\\[Lvl (?<level>\\d+)] (?<pet>.+)§e! §a§lVIEW RULE§r")
        var dolphinPlayers = 0

        init {
            DolphinPetDisplay()
        }
    }

    class DolphinPetDisplay : GuiElement("Dolphin Pet Display", FloatPair(50, 20)) {
        override fun render() {
            val player = mc.thePlayer
            if (toggled && Utils.inSkyblock && player != null && mc.theWorld != null) {
                if (lastPet != "Dolphin") return
                renderTexture(ICON, 0, 0)
                ScreenRenderer.fontRenderer.drawString(
                    if (Skytils.config.dolphinCap && dolphinPlayers > 5) "5" else dolphinPlayers.toString(),
                    20f,
                    5f,
                    CommonColors.ORANGE,
                    TextAlignment.LEFT_RIGHT,
                    TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val x = 0f
            val y = 0f
            renderTexture(ICON, x.toInt(), y.toInt())
            ScreenRenderer.fontRenderer.drawString(
                "5",
                x + 20,
                y + 5,
                CommonColors.ORANGE,
                TextAlignment.LEFT_RIGHT,
                TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = 16
        override val width: Int
            get() = 20 + ScreenRenderer.fontRenderer.getStringWidth("5")

        override val toggled: Boolean
            get() = Skytils.config.dolphinPetDisplay

        companion object {
            private val ICON = ResourceLocation("skytils", "icons/dolphin.png")
        }

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}