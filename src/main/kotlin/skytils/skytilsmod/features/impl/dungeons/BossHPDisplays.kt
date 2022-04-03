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
package skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.world.World
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import skytils.skytilsmod.utils.stripControlCodes
import java.awt.Color
import java.util.regex.Pattern

class BossHPDisplays {
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inDungeons || event.type.toInt() == 2) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (unformatted.startsWith("[BOSS] Sadan")) {
            if (unformatted.contains("My giants! Unleashed!")) canGiantsSpawn = true
            if (unformatted.contains("It was inevitable.") || unformatted.contains("NOOOOOOOOO")) canGiantsSpawn = false
        }
        if (unformatted == "[BOSS] The Watcher: Plus I needed to give my new friends some space to roam...") canGiantsSpawn =
            true
        if (unformatted.startsWith("[BOSS] The Watcher: You have failed to prove yourself, and have paid with your lives.") || unformatted.startsWith(
                "[BOSS] The Watcher: You have proven yourself"
            )
        ) canGiantsSpawn = false
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        canGiantsSpawn = false
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Utils.inDungeons) return
        if (canGiantsSpawn && Skytils.config.showGiantHPAtFeet) {
            val isSadanPlayer = mc.theWorld.getPlayerEntityByName("Sadan ") != null
            val matrixStack = UMatrixStack()
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity !is EntityArmorStand) continue
                val name = entity.displayName.formattedText
                if (name.contains("❤") && (!isSadanPlayer && name.contains("§e﴾ §c§lSadan§r") || (name.contains("Giant") && Utils.equalsOneOf(
                        DungeonFeatures.dungeonFloor,
                        "F7",
                        "M6",
                        "M7"
                    )) || GiantHPElement.GIANT_NAMES.any {
                        name.contains(
                            it
                        )
                    })
                ) {
                    GlStateManager.disableCull()
                    GlStateManager.disableDepth()
                    RenderUtil.drawLabel(
                        entity.positionVector.addVector(0.0, -10.0, 0.0),
                        name,
                        Color.WHITE,
                        event.partialTicks,
                        matrixStack
                    )
                    GlStateManager.enableDepth()
                    GlStateManager.enableCull()
                }
            }
        }
    }

    companion object {

        private var canGiantsSpawn = false

        init {
            GiantHPElement()
            GuardianRespawnTimer()
        }
    }

    class GuardianRespawnTimer : GuiElement("Guardian Respawn Timer", FloatPair(200, 30)) {
        private val guardianNameRegex = Pattern.compile("§c(Healthy|Reinforced|Chaos|Laser) Guardian §e0§c❤")
        private val timerRegex = Pattern.compile("§c ☠ §7 (.+?) §c ☠ §7")

        override fun render() {
            if (toggled && DungeonFeatures.hasBossSpawned && Utils.equalsOneOf(
                    DungeonFeatures.dungeonFloor,
                    "M3",
                    "F3"
                ) && mc.theWorld != null
            ) {
                val respawnTimers = HashMap<String, String>()
                for (entity in mc.theWorld.loadedEntityList) {
                    if (respawnTimers.size >= 4) break
                    if (entity !is EntityArmorStand) continue
                    val name = entity.customNameTag
                    if (name.startsWith("§c ☠ §7 ") && name.endsWith(" §c ☠ §7")) {
                        val nameTag = mc.theWorld.getEntitiesWithinAABB(
                            EntityArmorStand::class.java,
                            entity.entityBoundingBox.expand(2.0, 5.0, 2.0)
                        ).find {
                            it.customNameTag.endsWith(" Guardian §e0§c❤")
                        } ?: continue
                        val matcher = guardianNameRegex.matcher(nameTag.customNameTag)
                        if (matcher.find()) {
                            val timeMatcher = timerRegex.matcher(name)
                            if (timeMatcher.find()) {
                                respawnTimers[matcher.group(1)] = timeMatcher.group(1)
                            }
                        }
                    }
                }
                RenderUtil.drawAllInList(this, respawnTimers.entries.map { "${it.key}: ${it.value}" })
            }
        }

        override fun demoRender() {
            val sr = UResolution
            val leftAlign = actualX < sr.scaledWidth / 2f
            val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Guardian Respawn Timer Here",
                if (leftAlign) 0f else actualWidth,
                0f,
                CommonColors.WHITE,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Guardian Respawn Timer Here")

        override val toggled: Boolean
            get() = Skytils.config.showGuardianRespawnTimer

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class GiantHPElement : GuiElement("Show Giant HP", FloatPair(200, 30)) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (canGiantsSpawn && toggled && Utils.inSkyblock && player != null && world != null) {
                val giantNames =
                    world.getEntities(EntityArmorStand::class.java) { entity: EntityArmorStand? ->
                        val name = entity!!.displayName.formattedText
                        if (name.contains("❤")) {
                            if (name.contains("§e﴾ §c§lSadan§r")) {
                                return@getEntities true
                            } else if (name.contains("Giant") && Utils.equalsOneOf(
                                    DungeonFeatures.dungeonFloor,
                                    "F7",
                                    "M6",
                                    "M7"
                                )
                            ) return@getEntities true
                            if (GIANT_NAMES.any { name.contains(it) }) return@getEntities true
                        }
                        false
                    }
                giantNames.removeAll { entity: EntityArmorStand ->
                    DungeonFeatures.dungeonFloor == "F6" && entity.displayName.formattedText.contains(
                        "Sadan"
                    ) && giantNames.size > 1
                }
                RenderUtil.drawAllInList(this, giantNames.map { it.displayName.formattedText })
            }
        }

        override fun demoRender() {
            RenderUtil.drawAllInList(this, GIANT_NAMES.map { "$it §a20M§c❤" })
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * 4
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§3§lThe Diamond Giant §a19.9M§c❤")

        override val toggled: Boolean
            get() = Skytils.config.showGiantHP

        companion object {
            val GIANT_NAMES =
                setOf(
                    "§3§lThe Diamond Giant",
                    "§c§lBigfoot",
                    "§4§lL.A.S.R.",
                    "§d§lJolly Pink Giant",
                    "§d§lMutant Giant"
                )
        }

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}