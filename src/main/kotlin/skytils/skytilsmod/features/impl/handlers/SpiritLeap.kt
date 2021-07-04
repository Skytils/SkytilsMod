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

package skytils.skytilsmod.features.impl.handlers

import com.google.gson.JsonObject
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.utils.RenderUtil.highlight
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.stripControlCodes
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import java.util.regex.Pattern

class SpiritLeap : PersistentSave(File(Skytils.modDir, "spiritleap.json")) {

    private val playerPattern = Pattern.compile("(?:\\[.+?] )?(\\w+)")

    companion object {
        val names = HashMap<String, Boolean>()
        val classes = DungeonListener.DungeonClass.values()
            .associateWithTo(EnumMap(DungeonListener.DungeonClass::class.java)) { false }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inSkyblock) return
        val slot = event.slot
        if (event.container is ContainerChest) {
            val cc = event.container
            val displayName = cc.lowerChestInventory.displayName.unformattedText.trim()
            if (slot.hasStack) {
                val item = slot.stack
                if (Skytils.config.spiritLeapNames && displayName == "Spirit Leap") {
                    if (item.item === Item.getItemFromBlock(Blocks.stained_glass_pane)) {
                        event.isCanceled = true
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onGuiDrawPost(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!Utils.inDungeons) return
        if (event.container is ContainerChest) {
            val containerChest = event.container
            val fr = ScreenRenderer.fontRenderer
            val invSlots = containerChest.inventorySlots
            val displayName = containerChest.lowerChestInventory.displayName.unformattedText.trim { it <= ' ' }
            if (Skytils.config.spiritLeapNames && displayName == "Spirit Leap" || Skytils.config.reviveStoneNames && displayName == "Revive A Teammate") {
                var people = 0
                for (slot in invSlots) {
                    if (slot.inventory == mc.thePlayer.inventory) continue
                    if (!slot.hasStack || slot.stack.item != Items.skull) continue
                    val item = slot.stack
                    people++

                    //slot is 16x16
                    val x = slot.xDisplayPosition
                    val y = slot.yDisplayPosition + if (people % 2 != 0) -15 else 20
                    val matcher = playerPattern.matcher(item.displayName.stripControlCodes())
                    if (!matcher.find()) continue
                    val name = matcher.group(1)
                    if (name == "Unknown") continue
                    val teammate = (DungeonListener.team.find { it.playerName == name }
                        ?: continue)
                    val dungeonClass = teammate.dungeonClass
                    val text = fr.trimStringToWidth(item.displayName.substring(0, 2) + name, 32)
                    var shouldDrawBkg = true
                    if (Skytils.usingNEU && displayName != "Revive A Teammate") {
                        try {
                            val neuClass =
                                Class.forName("io.github.moulberry.notenoughupdates.NotEnoughUpdates")
                            val neuConfig = neuClass.getDeclaredField("config")
                            val config = neuConfig[neuClass.getDeclaredField("INSTANCE")[null]]
                            val improvedSBMenuS = config.javaClass.getDeclaredField("improvedSBMenu")[config]
                            val enableSbMenus = improvedSBMenuS.javaClass.getDeclaredField("enableSbMenus")
                            val customGuiEnabled = enableSbMenus.getBoolean(improvedSBMenuS)
                            if (customGuiEnabled) shouldDrawBkg = false
                        } catch (ignored: ClassNotFoundException) {
                        } catch (ignored: NoSuchFieldException) {
                        } catch (ignored: IllegalAccessException) {
                        }
                    }
                    val scale = 0.9
                    val scaleReset = 1 / scale
                    GlStateManager.pushMatrix()
                    GlStateManager.pushAttrib()
                    GlStateManager.disableLighting()
                    GlStateManager.disableDepth()
                    GlStateManager.disableBlend()
                    GlStateManager.translate(0f, 0f, 1f)
                    if (names.getOrDefault(name, false)) {
                        slot highlight Color(255, 0, 0)
                    } else if (classes.getOrDefault(dungeonClass, false)) {
                        slot highlight Color(0, 255, 0)
                    }
                    if (shouldDrawBkg) Gui.drawRect(
                        x - 2 - fr.getStringWidth(text) / 2,
                        y - 2,
                        x + fr.getStringWidth(text) / 2 + 2,
                        y + fr.FONT_HEIGHT + 2,
                        Color(47, 40, 40).rgb
                    )
                    fr.drawString(
                        text,
                        x.toFloat(),
                        y.toFloat(),
                        alignment = SmartFontRenderer.TextAlignment.MIDDLE,
                        shadow = SmartFontRenderer.TextShadow.OUTLINE
                    )
                    GlStateManager.scale(scale, scale, 1.0)
                    fr.drawString(
                        dungeonClass.className.first().uppercase(),
                        (scaleReset * x).toFloat(),
                        (scaleReset * slot.yDisplayPosition).toFloat(),
                        Color(255, 255, 0).rgb,
                        true
                    )
                    GlStateManager.popAttrib()
                    GlStateManager.popMatrix()
                }
            }
        }
    }

    override fun read(reader: FileReader) {
        val obj = gson.fromJson(reader, JsonObject::class.java)
        for (entry in obj["classes"].asJsonObject.entrySet()) {
            classes[DungeonListener.DungeonClass.getClassFromName(entry.key)] =
                entry.value.asJsonObject["enabled"].asBoolean
        }
        for (entry in obj["users"].asJsonObject.entrySet()) {
            names[entry.key] =
                entry.value.asJsonObject["enabled"].asBoolean
        }
    }

    override fun write(writer: FileWriter) {
        val obj = JsonObject()
        val classes = JsonObject()
        for (dClass in Companion.classes) {
            val classObj = JsonObject()
            classObj.addProperty("enabled", dClass.value)
            classes.add(dClass.key.name, classObj)
        }

        val users = JsonObject()
        for (name in names) {
            val userObj = JsonObject()
            userObj.addProperty("enabled", name.value)
            users.add(name.key, userObj)
        }

        obj.add("classes", classes)
        obj.add("users", users)
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: FileWriter) {
    }
}