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

package gg.skytils.skytilsmod.features.impl.handlers

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.stripControlCodes
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.hylin.skyblock.dungeons.DungeonClass
import java.io.File
import java.io.Reader
import java.io.Writer
import java.util.*

object SpiritLeap : PersistentSave(File(Skytils.modDir, "spiritleap.json")) {

    private val playerPattern = Regex("(?:\\[.+?] )?(?<name>\\w+)")
    private val doorOpenedPattern = Regex("^(?:\\[.+?] )?(?<name>\\w+) opened a WITHER door!$")
    private const val bloodOpenedString = "§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r"
    private var doorOpener: String? = null
    val names = HashMap<String, Boolean>()
    val classes = DungeonClass.entries
        .associateWithTo(EnumMap(DungeonClass::class.java)) { false }
    private val shortenedNameCache = WeakHashMap<String, String>()
    private val nameSlotCache = HashMap<Int, String>()

    @SubscribeEvent
    fun onGuiDrawPost(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!Utils.inDungeons) return
        if (event.container is ContainerChest) {
            if ((Skytils.config.spiritLeapNames && event.chestName == "Spirit Leap") || (Skytils.config.reviveStoneNames && event.chestName == "Revive A Teammate") || (Skytils.config.ghostTeleportMenuNames && event.chestName == "Teleport to Player")) {
                val fr = ScreenRenderer.fontRenderer
                var people = 0
                GlStateManager.disableLighting()
                GlStateManager.enableBlend()
                for (slot in event.container.inventorySlots) {
                    if (slot.inventory == mc.thePlayer.inventory) continue
                    if (!slot.hasStack || slot.stack.item != Items.skull) continue
                    val item = slot.stack
                    people++

                    val x = slot.xDisplayPosition.toFloat()
                    val y = slot.yDisplayPosition + if (people % 2 != 0) -15f else 20f
                    val name = nameSlotCache[slot.slotNumber]
                    if (name == null || name == "Unknown") {
                        nameSlotCache[slot.slotNumber] =
                            playerPattern.find(item.displayName.stripControlCodes())?.groups?.get("name")?.value
                                ?: continue
                        continue
                    }
                    val teammate = DungeonListener.team[name] ?: continue
                    val dungeonClass = teammate.dungeonClass
                    val text = shortenedNameCache.getOrPut(name) {
                        fr.trimStringToWidth(item.displayName.substring(0, 2) + name, 32)
                    }
                    val scale = 0.9f
                    val scaleReset = 1 / scale
                    GlStateManager.pushMatrix()
                    if (name == doorOpener) {
                        slot highlight 1174394112
                    } else if (names.getOrDefault(name, false)) {
                        slot highlight 1174339584
                    } else if (classes.getOrDefault(dungeonClass, false)) {
                        slot highlight 1157693184
                    }
                    GlStateManager.translate(0f, 0f, 299f)
                    Gui.drawRect(
                        (x - 2 - fr.getStringWidth(text) / 2).toInt(),
                        (y - 2).toInt(),
                        (x + fr.getStringWidth(text) / 2 + 2).toInt(),
                        (y + fr.FONT_HEIGHT + 2).toInt(),
                        -13686744
                    )
                    fr.drawString(
                        text,
                        x,
                        y,
                        alignment = SmartFontRenderer.TextAlignment.MIDDLE,
                        shadow = SmartFontRenderer.TextShadow.OUTLINE
                    )
                    GlStateManager.scale(scale, scale, 1f)
                    fr.drawString(
                        dungeonClass.className.first().uppercase(),
                        scaleReset * x,
                        scaleReset * slot.yDisplayPosition,
                        -256,
                        true
                    )
                    GlStateManager.popMatrix()
                }
                GlStateManager.disableBlend()
            }
        }
    }

    @SubscribeEvent
    fun onChatReceived(event: ClientChatReceivedEvent) {
        if (!Skytils.config.highlightDoorOpener || !Utils.inDungeons || event.type == 2.toByte()) return
        doorOpener = if (event.message.formattedText == bloodOpenedString) null
        else (doorOpenedPattern.find(event.message.unformattedText)?.groups?.get("name")?.value ?: return)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        nameSlotCache.clear()
    }

    override fun read(reader: Reader) {
        val data = json.decodeFromString<SaveData>(reader.readText())
        names.putAll(data.users.entries.associate { it.key to it.value.enabled })
        data.classes.forEach { (clazz, state) ->
            classes[clazz] = state.enabled
        }
    }

    override fun write(writer: Writer) {
        writer.write(
            json.encodeToString(
                SaveData(
                    names.entries.associate { it.key to SaveComponent(it.value) },
                    classes.entries.associate { it.key to SaveComponent(it.value) })
            )
        )
    }

    override fun setDefault(writer: Writer) {
        write(writer)
    }
}

@Serializable
private data class SaveData(val users: Map<String, SaveComponent>, val classes: Map<DungeonClass, SaveComponent>)

@Serializable
private data class SaveComponent(val enabled: Boolean)