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
package skytils.skytilsmod.features.impl.misc

import com.google.gson.JsonObject
import net.minecraft.block.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.BossStatus
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntityWolf
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.GuiManager.Companion.createTitle
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.BlockChangeEvent
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.RenderUtil.drawFilledBoundingBox
import skytils.skytilsmod.utils.RenderUtil.drawOutlinedBoundingBox
import skytils.skytilsmod.utils.ScoreboardUtil.cleanSB
import skytils.skytilsmod.utils.ScoreboardUtil.sidebarLines
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.Utils.printDebugMessage
import skytils.skytilsmod.utils.baseMaxHealth
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import skytils.skytilsmod.utils.stripControlCodes
import java.awt.Color
import kotlin.math.floor


class SlayerFeatures {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inSkyblock) return
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null || mc.thePlayer == null) return
        val hasSlayerText = sidebarLines.any { cleanSB(it) == "Slay the boss!" }
        if (ticks % 4 == 0) {
            if (Skytils.config.rev5TNTPing) {
                if (hasSlayerText) {
                    var under: BlockPos? = null
                    if (mc.thePlayer.onGround) {
                        under = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)
                    } else {
                        for (i in (mc.thePlayer.posY - 0.5f).toInt() downTo 0 step 1) {
                            val test = BlockPos(mc.thePlayer.posX, i.toDouble(), mc.thePlayer.posZ)
                            if (mc.theWorld.getBlockState(test).block !== Blocks.air) {
                                under = test
                                break
                            }
                        }
                    }
                    if (under != null) {
                        val blockUnder = mc.theWorld.getBlockState(under)
                        var isDanger = false
                        if (blockUnder.block === Blocks.stone_slab && blockUnder.getValue(BlockHalfStoneSlab.VARIANT) == BlockStoneSlab.EnumType.QUARTZ) {
                            isDanger = true
                        } else if (blockUnder.block === Blocks.quartz_stairs || blockUnder.block === Blocks.acacia_stairs) {
                            isDanger = true
                        } else if (blockUnder.block === Blocks.wooden_slab && blockUnder.getValue(BlockHalfWoodSlab.VARIANT) == BlockPlanks.EnumType.ACACIA) {
                            isDanger = true
                        } else if (blockUnder.block === Blocks.stained_hardened_clay) {
                            val color = Blocks.stained_hardened_clay.getMetaFromState(blockUnder)
                            if (color == 0 || color == 8 || color == 14) isDanger = true
                        } else if (blockUnder.block === Blocks.bedrock) {
                            isDanger = true
                        }
                        if (isDanger) {
                            mc.thePlayer.playSound("random.orb", 1f, 1f)
                        }
                    }
                }
            }
            if (Skytils.config.showRNGMeter) {
                for (index in 0 until sidebarLines.size - 1) {
                    if (cleanSB(sidebarLines[index]) == "Slayer Quest") {
                        val boss = cleanSB(sidebarLines[(index - 1).coerceAtLeast(0)].stripControlCodes())
                        if (boss.startsWith("Revenant Horror")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.revRNG,
                                    ChatComponentText("§2§lRevenant Horror RNG§r - §d${Skytils.config.revRNG}%")
                                ), true
                            )
                            break
                        }
                        if (boss.startsWith("Tarantula Broodfather")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.taraRNG,
                                    ChatComponentText("§8§lTarantula Broodfather RNG§r - §d${Skytils.config.taraRNG}%")
                                ), true
                            )
                            break
                        }
                        if (boss.startsWith("Sven Packmaster")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.svenRNG,
                                    ChatComponentText("§7§lSven Packmaster RNG§r - §d${Skytils.config.svenRNG}%")
                                ), true
                            )
                            break
                        }
                        if (boss.startsWith("Voidgloom Seraph")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.voidRNG,
                                    ChatComponentText("§5§lVoidgloom Seraph RNG§r - §d${Skytils.config.voidRNG}%")
                                ), true
                            )
                            break
                        }
                    }
                }
            }
            ticks = 0
        }
        ticks++
    }

    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<EntityLivingBase>) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityArmorStand) {
            val entity = event.entity as EntityArmorStand
            if (!entity.hasCustomName()) return
            val name = entity.displayName.unformattedText
            if (Skytils.config.slayerBossHitbox && name.endsWith("§c❤") && !name.endsWith("§e0§c❤") && !mc.renderManager.isDebugBoundingBox) {
                val x = entity.posX
                val y = entity.posY
                val z = entity.posZ
                if (ZOMBIE_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(x - 0.5, y - 2, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        1f
                    )
                } else if (SPIDER_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(
                            x - 0.625,
                            y - 1,
                            z - 0.625,
                            x + 0.625,
                            y - 0.25,
                            z + 0.625
                        ), Color(0, 255, 255, 255), 3f, 1f
                    )
                } else if (WOLF_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(x - 0.5, y - 1, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        1f
                    )
                } else if (ENDERMAN_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(x - 0.5, y - 3, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        1f
                    )
                }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock) return
        if (event.packet is S29PacketSoundEffect) {
            val packet = event.packet
            if (Skytils.config.slayerMinibossSpawnAlert && packet.soundName == "random.explode" && packet.volume == 0.6f && packet.pitch == 9 / 7f && GuiManager.title != "§cMINIBOSS") {
                createTitle("§cMINIBOSS", 20)
            }
        }
        if (event.packet is S02PacketChat) {
            if (event.packet.chatComponent.unformattedText.stripControlCodes().trim().startsWith("RNGesus Meter")) {
                val rngMeter =
                    event.packet.chatComponent.unformattedText.stripControlCodes().replace(RNGRegex, "").toFloat()
                for (index in 0 until sidebarLines.size - 1) {
                    if (cleanSB(sidebarLines[index]) == "Slayer Quest") {
                        val boss = cleanSB(sidebarLines[(index - 1).coerceAtLeast(0)].stripControlCodes())
                        if (boss.startsWith("Revenant Horror")) {
                            Skytils.config.revRNG = rngMeter
                            break
                        }
                        if (boss.startsWith("Tarantula Broodfather")) {
                            Skytils.config.taraRNG = rngMeter
                            break
                        }
                        if (boss.startsWith("Sven Packmaster")) {
                            Skytils.config.svenRNG = rngMeter
                            break
                        }
                        if (boss.startsWith("Voidgloom Seraph")) {
                            Skytils.config.voidRNG = rngMeter
                            break
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (event.old.block is BlockBeacon && event.update.block is BlockAir) {
            yangGlyph = null
        }
        if (slayerEntity == null) return
        if (event.update.block is BlockBeacon && event.pos.distanceSq(
                slayerEntity!!.posX,
                slayerEntity!!.posY,
                slayerEntity!!.posZ
            ) < 225
        ) {
            yangGlyph = event.pos
            if (Skytils.config.yangGlyphPing) {
                createTitle("§cYang Glyph!", 30)
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (Skytils.config.highlightYangGlyph && yangGlyph != null) {
            GlStateManager.disableCull()
            GlStateManager.disableDepth()
            val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
            val x = yangGlyph!!.x - viewerX
            val y = yangGlyph!!.y - viewerY
            val z = yangGlyph!!.z - viewerZ
            drawFilledBoundingBox(
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                Color(65, 102, 245),
                0.5f
            )
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        if (!sidebarLines.any { cleanSB(it) == "Slay the boss!" }) return
        if (slayerEntity != null) {
            printDebugMessage(
                "boss not null"
            )
            return
        }
        val entity = event.entity
        if (entity is EntityZombie) {
            TickTask(5) {
                val nearbyArmorStands = entity.getEntityWorld().getEntitiesInAABBexcluding(
                    entity, entity.entityBoundingBox.expand(0.2, 3.0, 0.2)
                ) { nearbyEntity: Entity? ->
                    if (nearbyEntity is EntityArmorStand) {
                        if (nearbyEntity.isInvisible && nearbyEntity.hasCustomName()) {
                            if (nearbyEntity.inventory.any { it != null }) {
                                // armor stand has equipment, abort!
                                return@getEntitiesInAABBexcluding false
                            }
                            // armor stand has a custom name, is invisible and has no equipment -> probably a "name tag"-armor stand
                            return@getEntitiesInAABBexcluding true
                        }
                    }
                    false
                }
                var isSlayer = 0
                for (nearby in nearbyArmorStands) {
                    if (nearby.displayName.formattedText.startsWith("§8[§7Lv")) continue
                    if (nearby.displayName.formattedText.startsWith("§c☠ §bRevenant Horror") ||
                        nearby.displayName.formattedText.startsWith("§c☠ §fAtoned Horror")
                    ) {
                        val currentTier = sidebarLines.map { cleanSB(it) }.find { it.startsWith("Revenant Horror ") }
                            ?.substringAfter("Revenant Horror ") ?: ""
                        if (BossHealths["Revenant"]?.get(currentTier)?.asInt
                            == entity.baseMaxHealth.toInt()
                        ) {
                            slayerNameEntity = nearby as EntityArmorStand
                            isSlayer++
                        }
                        continue
                    }
                    if (nearby.displayName.formattedText == "§c02:59§r") {
                        slayerTimerEntity = nearby as EntityArmorStand
                        isSlayer++
                        continue
                    }
                }
                if (isSlayer == 2) {
                    slayerEntity = entity
                } else {
                    slayerTimerEntity = null
                    slayerNameEntity = null
                }
            }
        } else if (entity is EntitySpider) {
            detectSlayerEntities(entity, "Tarantula Broodfather", "§c02:59§r", "§5☠ §4Tarantula Broodfather")
        } else if (entity is EntityWolf) {
            detectSlayerEntities(entity, "Sven Packmaster", "§c03:59§r", "§c☠ §fSven Packmaster")
        } else if (entity is EntityEnderman) {
            detectSlayerEntities(entity, "Voidgloom Seraph", "§c03:59§r", "§c☠ §bVoidgloom Seraph")
        }
    }

    private fun detectSlayerEntities(entity: EntityLiving, name: String, timer: String, nameStart: String) {
        TickTask(5) {
            val nearbyArmorStands = entity.getEntityWorld().getEntitiesInAABBexcluding(
                entity, entity.entityBoundingBox.expand(0.2, 3.0, 0.2)
            ) { nearbyEntity: Entity? ->
                if (nearbyEntity is EntityArmorStand) {
                    if (nearbyEntity.isInvisible && nearbyEntity.hasCustomName()) {
                        if (nearbyEntity.inventory.any { it != null }) {
                            // armor stand has equipment, abort!
                            return@getEntitiesInAABBexcluding false
                        }
                        // armor stand has a custom name, is invisible and has no equipment -> probably a "name tag"-armor stand
                        return@getEntitiesInAABBexcluding true
                    }
                }
                false
            }
            var isSlayer = 0
            for (nearby in nearbyArmorStands) {
                if (nearby.displayName.formattedText.startsWith("§8[§7Lv")) continue
                if (nearby.displayName.formattedText.startsWith(nameStart)
                ) {
                    val currentTier =
                        sidebarLines.map { cleanSB(it) }.find { it.startsWith(name) }?.substringAfter(name)?.drop(1)
                            ?: ""
                    printDebugMessage(
                        "expected tier $currentTier - spawned hp ${floor(entity.baseMaxHealth).toInt()}"
                    )
                    if (BossHealths[name.substringBefore(" ")]?.get(currentTier)?.asInt
                        == floor(entity.baseMaxHealth).toInt()
                    ) {
                        printDebugMessage(
                            "hp matched"
                        )
                        slayerNameEntity = nearby as EntityArmorStand
                        isSlayer++
                    }
                    continue
                }
                if (nearby.displayName.formattedText == timer) {
                    printDebugMessage(
                        "timer matched"
                    )
                    slayerTimerEntity = nearby as EntityArmorStand
                    isSlayer++
                    continue
                }
            }
            if (isSlayer == 2) {
                slayerEntity = entity
            } else {
                slayerTimerEntity = null
                slayerNameEntity = null
            }
        }
    }

    private class RNGMeter(val max: Float, val current: Float, val name: IChatComponent) : IBossDisplayData {

        override fun getMaxHealth(): Float {
            return max
        }

        override fun getHealth(): Float {
            return current
        }

        override fun getDisplayName(): IChatComponent {
            return name
        }

    }

    class SlayerDisplayElement : GuiElement("Slayer Display", FloatPair(150, 20)) {
        override fun render() {
            if (Utils.inSkyblock) {
                if (slayerTimerEntity != null) {
                    if (slayerTimerEntity!!.isDead) {
                        printDebugMessage(
                            "timer died"

                        )
                        slayerTimerEntity = null
                    } else if (toggled) {
                        ScreenRenderer.fontRenderer.drawString(
                            slayerTimerEntity!!.displayName.formattedText,
                            0f,
                            0f,
                            CommonColors.WHITE,
                            SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                    }
                }
                if (slayerNameEntity != null) {
                    if (slayerNameEntity!!.isDead) {
                        printDebugMessage(
                            "name died"

                        )
                        slayerNameEntity = null
                    } else if (toggled) {
                        ScreenRenderer.fontRenderer.drawString(
                            slayerNameEntity!!.displayName.formattedText,
                            0f,
                            10f,
                            CommonColors.WHITE,
                            SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                    }
                }
                if (slayerEntity != null) {
                    if (slayerEntity!!.isDead) {
                        printDebugMessage(
                            "slayer died"

                        )
                        slayerEntity = null
                    }
                }
            }
        }

        override fun demoRender() {
            ScreenRenderer.fontRenderer.drawString(
                "§c02:59§r",
                0f,
                0f,
                CommonColors.WHITE,
                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                SmartFontRenderer.TextShadow.NORMAL
            )
            ScreenRenderer.fontRenderer.drawString(
                "§c☠ §bRevenant Horror §a500§c❤§r",
                0f,
                10f,
                CommonColors.WHITE,
                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * 2 + 1
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§c☠ §bRevenant Horror §a500§c❤§r")

        override val toggled: Boolean
            get() = Skytils.config.showSlayerDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private var ticks = 0
        private val ZOMBIE_MINIBOSSES = arrayOf(
            "§cRevenant Sycophant",
            "§cRevenant Champion",
            "§4Deformed Revenant",
            "§cAtoned Champion",
            "§4Atoned Revenant"
        )
        private val SPIDER_MINIBOSSES = arrayOf("§cTarantula Vermin", "§cTarantula Beast", "§4Mutant Tarantula")
        private val WOLF_MINIBOSSES = arrayOf("§cPack Enforcer", "§cSven Follower", "§4Sven Alpha")
        private val ENDERMAN_MINIBOSSES = arrayOf("Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac")
        private val RNGRegex = Regex("[^0-9.]")
        private var slayerEntity: Entity? = null
        private var slayerNameEntity: EntityArmorStand? = null
        private var slayerTimerEntity: EntityArmorStand? = null
        private var yangGlyph: BlockPos? = null
        var BossHealths = HashMap<String, JsonObject>()

        init {
            SlayerDisplayElement()
        }
    }
}