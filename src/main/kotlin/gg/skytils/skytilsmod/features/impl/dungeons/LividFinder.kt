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

package gg.skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEnumDyeColor
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.block.BlockStainedGlass
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.EnumDyeColor
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumChatFormatting
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object LividFinder {
    private var foundLivid = false
    var livid: Entity? = null
    private var lividTag: Entity? = null
    private var lividJob: Job? = null
    private val lividBlock = BlockPos(13, 107, 25)

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || !Utils.inDungeons || DungeonFeatures.dungeonFloorNumber != 5 || !DungeonFeatures.hasBossSpawned || !Skytils.config.findCorrectLivid) return

        if (Skytils.config.lividFinderType == 1 && !foundLivid && mc.thePlayer.isPotionActive(Potion.blindness)) {
            if (lividJob == null || lividJob?.isCancelled == true || lividJob?.isCompleted == true) {
                printDevMessage("Starting livid job", "livid")
                lividJob = Skytils.launch {
                    while (mc.thePlayer.isPotionActive(Potion.blindness)) {
                        delay(1)
                    }
                    val state = mc.theWorld.getBlockState(lividBlock)
                    val color = state.getValue(BlockStainedGlass.COLOR)
                    val (a, otherColor) = getLividColors(color)
                    getLivid(color, a, otherColor)
                }
            }
        }

        if (lividTag?.isDead == true || livid?.isDead == true) {
            printDevMessage("Livid is dead?", "livid")
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (mc.thePlayer == null || !Utils.inDungeons || DungeonFeatures.dungeonFloorNumber != 5 || !DungeonFeatures.hasBossSpawned || !Skytils.config.findCorrectLivid) return
        if (event.pos == lividBlock) {
            printDevMessage("Livid block changed", "livid")
            if (Skytils.config.lividFinderType == 0) {
                printDevMessage("block detection started", "livid")
                val color = event.update.getValue(BlockStainedGlass.COLOR)
                val (mapped, other) = getLividColors(color)
                Skytils.launch {
                    while (mc.thePlayer.isPotionActive(Potion.blindness)) {
                        delay(1)
                    }
                    getLivid(color, mapped, other)
                }
                printDevMessage("block detection done", "livid")
            }
        }
    }

    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<*>) {
        if (!Utils.inDungeons) return
        if (event.entity == lividTag) {
            val (x, y, z) = RenderUtil.fixRenderPos(event.x, event.y, event.z)
            val aabb = AxisAlignedBB(
                x - 0.5,
                y - 2,
                z - 0.5,
                x + 0.5,
                y,
                z + 0.5
            )
            RenderUtil.drawOutlinedBoundingBox(
                aabb,
                Color(255, 107, 11, 255),
                3f,
                RenderUtil.getPartialTicks()
            )
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        lividTag = null
        livid = null
        foundLivid = false
    }

    fun getLividColors(color: EnumDyeColor): Pair<EnumChatFormatting?, EnumChatFormatting> {
        printDevMessage("Block color: $color", "livid")
        val mappedColor = when (color) {
            EnumDyeColor.WHITE -> EnumChatFormatting.WHITE
            EnumDyeColor.MAGENTA -> EnumChatFormatting.LIGHT_PURPLE
            EnumDyeColor.PINK -> EnumChatFormatting.LIGHT_PURPLE
            EnumDyeColor.RED -> EnumChatFormatting.RED
            EnumDyeColor.SILVER -> EnumChatFormatting.GRAY
            EnumDyeColor.GRAY -> EnumChatFormatting.GRAY
            EnumDyeColor.GREEN -> EnumChatFormatting.DARK_GREEN
            EnumDyeColor.LIME -> EnumChatFormatting.GREEN
            EnumDyeColor.BLUE -> EnumChatFormatting.BLUE
            EnumDyeColor.PURPLE -> EnumChatFormatting.DARK_PURPLE
            EnumDyeColor.YELLOW -> EnumChatFormatting.YELLOW
            else -> null
        }
        val otherColor =
            (color as AccessorEnumDyeColor).chatColor
        printDevMessage("${mappedColor}Mapped color§r, ${otherColor}other color", "livid")
        return mappedColor to otherColor
    }

    fun getLivid(blockColor: EnumDyeColor, mappedColor: EnumChatFormatting?, otherColor: EnumChatFormatting) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityArmorStand) continue
            val fallBackColor = entity.name.startsWith("$otherColor﴾ $otherColor§lLivid")
            if ((mappedColor != null && entity.name.startsWith("$mappedColor﴾ $mappedColor§lLivid")) || fallBackColor) {
                if (fallBackColor && !(mappedColor != null && entity.name.startsWith("$mappedColor﴾ $mappedColor§lLivid"))) {
                    UChat.chat("§bBlock color ${blockColor.name} should be mapped to ${otherColor}${otherColor.name}§b. Please report this to discord.gg/skytils")
                }
                lividTag = entity
                val aabb = AxisAlignedBB(
                    lividTag!!.posX - 0.5,
                    lividTag!!.posY - 2,
                    lividTag!!.posZ - 0.5,
                    lividTag!!.posX + 0.5,
                    lividTag!!.posY,
                    lividTag!!.posZ + 0.5
                )
                livid = mc.theWorld.loadedEntityList.find {
                    val coll = it.entityBoundingBox ?: return@find false
                    return@find it is EntityOtherPlayerMP && it.name.endsWith(" Livid") && aabb.isVecInside(
                        coll.minVec
                    ) && aabb.isVecInside(
                        coll.maxVec
                    )
                }
                foundLivid = true
                return
            }
        }
        printDevMessage("No livid found!", "livid")
    }

    internal class LividGuiElement : GuiElement("Livid HP", x = 0.05f, y = 0.4f) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (toggled && Utils.inDungeons && player != null && world != null) {
                if (lividTag == null) return

                val leftAlign = scaleX < sr.scaledWidth / 2f
                val alignment = if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    lividTag!!.name.replace("§l", ""),
                    if (leftAlign) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    textShadow
                )
            }
        }

        override fun demoRender() {

            val leftAlign = scaleX < sr.scaledWidth / 2f
            val text = "§r§f﴾ Livid §e6.9M§c❤ §f﴿"
            val alignment = if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                text,
                if (leftAlign) 0f else 0f + width,
                0f,
                CommonColors.WHITE,
                alignment,
                textShadow
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§r§f﴾ Livid §e6.9M§c❤ §f﴿")

        override val toggled: Boolean
            get() = Skytils.config.findCorrectLivid

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    init {
        LividGuiElement()
    }
}