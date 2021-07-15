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
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.BossStatus
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityGuardian
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntityWolf
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.event.world.WorldEvent
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
import skytils.skytilsmod.events.CheckRenderEntityEvent
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import skytils.skytilsmod.utils.RenderUtil.drawFilledBoundingBox
import skytils.skytilsmod.utils.RenderUtil.drawOutlinedBoundingBox
import skytils.skytilsmod.utils.ScoreboardUtil.cleanSB
import skytils.skytilsmod.utils.ScoreboardUtil.sidebarLines
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.awt.Color
import kotlin.math.floor


class SlayerFeatures {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inSkyblock) return
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null || mc.thePlayer == null) return
        lastTickHasSlayerText = hasSlayerText
        hasSlayerText = sidebarLines.any { cleanSB(it) == "Slay the boss!" }
        if (!lastTickHasSlayerText && hasSlayerText) {
            val currentTier =
                sidebarLines.map { cleanSB(it) }.find { it.startsWith("Voidgloom Seraph") }
                    ?.substringAfter("Voidgloom Seraph")?.drop(1)
                    ?: ""
            expectedMaxHp = BossHealths["Voidgloom"]?.get(currentTier)?.asInt
        }
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
        if (yangGlyph != null && event.pos == yangGlyph && event.old.block is BlockBeacon && event.update.block is BlockAir) {
            yangGlyph = null
            return
        }
        if (slayerEntity == null) return
        if (yangGlyphEntity != null) {
            printDevMessage("Glyph Entity exists", "slayer", "seraph", "seraphGlyph")
            if (event.update.block is BlockBeacon && yangGlyphEntity!!.position.distanceSq(event.pos) < 9) {
                printDevMessage("Beacon entity near beacon block!", "slayer", "seraph", "seraphGlyph")
                yangGlyph = event.pos
                yangGlyphEntity = null
                if (Skytils.config.yangGlyphPing) {
                    createTitle("§cYang Glyph!", 30)
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.highlightYangGlyph && yangGlyph != null) {
            GlStateManager.disableCull()
            GlStateManager.disableDepth()
            val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
            val x = yangGlyph!!.x - viewerX
            val y = yangGlyph!!.y - viewerY
            val z = yangGlyph!!.z - viewerZ
            drawFilledBoundingBox(
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                Skytils.config.yangGlyphColor,
                1f
            )
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
        if (Skytils.config.highlightNukekebiHeads && nukekebiHeads.isNotEmpty()) {
            for (head in nukekebiHeads.also { list -> list.removeAll { it.isDead } }) {
                GlStateManager.disableCull()
                GlStateManager.disableDepth()
                val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
                val x = head.posX - viewerX
                val y = head.posY - viewerY
                val z = head.posZ - viewerZ
                drawFilledBoundingBox(
                    AxisAlignedBB(x, y, z, x + 0.5, y + 1.975, z + 0.5),
                    Skytils.config.nukekebiHeadColor,
                    1f
                )
                GlStateManager.enableDepth()
                GlStateManager.enableCull()
            }
        }
    }

    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityArmorStand && slayerEntity != null) {
            TickTask(1) {
                val e = event.entity as EntityArmorStand
                if (slayerEntity != null && e.entityBoundingBox.expand(2.0, 3.0, 2.0)
                        .intersectsWith(slayerEntity!!.entityBoundingBox)
                ) {
                    printDevMessage("Found nearby armor stand", "slayer", "seraph", "seraphGlyph", "seraphFixation")
                    for (item in e.inventory) {
                        if (item == null) continue
                        if (item.item == Item.getItemFromBlock(Blocks.beacon)) {
                            printDevMessage("Beacon armor stand is close to slayer entity", "slayer", "seraph", "seraphGlyph")
                            yangGlyphEntity = e
                        } else if (item.item == Items.skull) {
                            if (ItemUtil.getSkullTexture(item) == "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0=") {
                                nukekebiHeads.add(e)
                            }
                        }
                    }
                }
            }
        }
        if (!hasSlayerText) return
        if (slayerEntity != null) {
            printDevMessage("boss not null", "slayer", "seraph")
            return
        }
        processSlayerEntity(event.entity)
    }

    @SubscribeEvent
    fun onAttack(event: AttackEntityEvent) {
        if (!hasSlayerText || !Utils.inSkyblock || event.entity != mc.thePlayer || event.target !is EntityEnderman || !Skytils.config.useSlayerHitMethod) return
        val enderman = event.target as EntityEnderman
        if (floor(enderman.baseMaxHealth).toInt() == expectedMaxHp) {
            printDevMessage("A valid enderman was attacked", "slayer", "seraph", "seraphHit")
            hitMap.compute(enderman) { _, int ->
                return@compute (int ?: 0).inc()
            }
            if (slayerEntity !is EntityEnderman) {
                detectSlayerEntities(enderman, "Voidgloom Seraph", null, "§c☠ §bVoidgloom Seraph")
            } else if (enderman != slayerEntity && hitMap[enderman]!! - (hitMap[slayerEntity as EntityEnderman]
                    ?: 0) >= 10
            ) {
                printDevMessage("Processing new entity", "slayer", "seraph", "seraphHit")
                detectSlayerEntities(enderman, "Voidgloom Seraph", null, "§c☠ §bVoidgloom Seraph")
            }
        }
    }

    @SubscribeEvent
    fun onDeath(event: LivingDeathEvent) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityEnderman) {
            printDevMessage("An Enderman died", "slayer", "seraph", "seraphHit")
            hitMap.remove(event.entity)
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        slayerEntity = null
        slayerNameEntity = null
        slayerTimerEntity = null
        hitMap.clear()
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!Skytils.config.hideOthersBrokenHeartRadiation || !event.entity.isInvisible || event.entity !is EntityGuardian) return
        if (slayerEntity != null && slayerEntity is EntityEnderman) {
            if (slayerEntity!!.isRiding) {
                printDevMessage("Slayer is Riding", "slayer", "seraph", "seraphRadiation")
                if (event.entity.getDistanceSqToEntity(slayerEntity!!) > 2 * 2) {
                    printDevMessage("Guardian too far", "slayer", "seraph", "seraphRadiation")
                    event.isCanceled = true
                }
            } else {
                printDevMessage("Slayer not riding, removing guardian", "slayer", "seraph", "seraphRadiation")
                event.isCanceled = true
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
                val leftAlign = actualX < ScaledResolution(mc).scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                if (slayerTimerEntity != null) {
                    if (slayerTimerEntity!!.isDead) {
                        printDevMessage("timer died", "slayer", "seraph")
                        slayerTimerEntity = null
                    } else if (toggled) {
                        ScreenRenderer.fontRenderer.drawString(
                            slayerTimerEntity!!.displayName.formattedText,
                            if (leftAlign) 0f else width.toFloat(),
                            0f,
                            CommonColors.WHITE,
                            alignment,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                    }
                }
                if (slayerNameEntity != null) {
                    if (slayerNameEntity!!.isDead) {
                        printDevMessage("name died", "slayer", "seraph")
                        slayerNameEntity = null
                    } else if (toggled) {
                        ScreenRenderer.fontRenderer.drawString(
                            slayerNameEntity!!.displayName.formattedText,
                            if (leftAlign) 0f else width.toFloat(),
                            10f,
                            CommonColors.WHITE,
                            alignment,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                    }
                }
                if (slayerEntity != null) {
                    if (slayerEntity!!.isDead) {
                        printDevMessage("slayer died", "slayer", "seraph")
                        if (Skytils.config.slayerTimeToKill) {
                            mc.thePlayer.addChatComponentMessage(ChatComponentText("§9§lSkytils §8» §bSlayer took §f${slayerEntity!!.ticksExisted / 20f}§bs to kill"))
                        }
                        slayerEntity = null
                        nukekebiHeads.clear()
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

    class SeraphDisplayElement : GuiElement("Seraph Display", FloatPair(20, 20)) {
        override fun render() {
            if (toggled && Utils.inSkyblock && slayerEntity != null && slayerEntity is EntityEnderman) {
                val leftAlign = actualX < ScaledResolution(mc).scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                if (slayerNameEntity != null) {
                    if (slayerNameEntity!!.displayName.formattedText.contains("Hits")) {
                        ScreenRenderer.fontRenderer.drawString(
                            "§dShield Phase",
                            if (leftAlign) 0f else width.toFloat(),
                            0f,
                            CommonColors.WHITE,
                            alignment,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                    } else {
                        ScreenRenderer.fontRenderer.drawString(
                            "§6Damage Phase",
                            if (leftAlign) 0f else width.toFloat(),
                            0f,
                            CommonColors.WHITE,
                            alignment,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                    }
                }
                val heldBlock: IBlockState? = (slayerEntity as EntityEnderman).heldBlockState
                if (heldBlock != null && heldBlock.block is BlockBeacon) {
                    ScreenRenderer.fontRenderer.drawString(
                        "§cHolding beacon!",
                        if (leftAlign) 0f else width.toFloat(),
                        10f,
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                } else {
                    ScreenRenderer.fontRenderer.drawString(
                        "§bHolding nothing!",
                        if (leftAlign) 0f else width.toFloat(),
                        10f,
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                }
                if (yangGlyph != null) {
                    ScreenRenderer.fontRenderer.drawString(
                        "§cYang Glyph placed!",
                        if (leftAlign) 0f else width.toFloat(),
                        20f,
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                } else {
                    ScreenRenderer.fontRenderer.drawString(
                        "§bNo yang glyph",
                        if (leftAlign) 0f else width.toFloat(),
                        20f,
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                }
                if (nukekebiHeads.size > 0) {
                    ScreenRenderer.fontRenderer.drawString(
                        "§dNukekebi Heads: §c${nukekebiHeads.size}",
                        if (leftAlign) 0f else width.toFloat(),
                        30f,
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                } else {
                    ScreenRenderer.fontRenderer.drawString(
                        "§bNo Nukekebi Heads",
                        if (leftAlign) 0f else width.toFloat(),
                        30f,
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                }
            }
        }

        override fun demoRender() {
            val leftAlign = actualX < ScaledResolution(mc).scaledWidth / 2f
            val alignment =
                if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "§dShield Phase",
                if (leftAlign) 0f else width.toFloat(),
                0f,
                CommonColors.WHITE,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
            ScreenRenderer.fontRenderer.drawString(
                "§bHolding beacon!",
                if (leftAlign) 0f else width.toFloat(),
                10f,
                CommonColors.WHITE,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
            ScreenRenderer.fontRenderer.drawString(
                "§cNo yang glyph",
                if (leftAlign) 0f else width.toFloat(),
                20f,
                CommonColors.WHITE,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT + 20
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§bHolding beacon!")

        override val toggled: Boolean
            get() = Skytils.config.showSeraphDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class SlayerArmorDisplayElement : GuiElement("Slayer Armor Display", FloatPair(150, 20)) {
        val upgradeBonusRegex =
            Regex("§7Next Upgrade: §a\\+(?<nextDefense>[\\d,]+?)❈ §8\\(§a(?<kills>[\\d,]+)§7/§c(?<nextKills>[\\d,]+)§8\\)")

        override fun render() {
            if (Utils.inSkyblock && toggled && mc.thePlayer != null) {
                ScreenRenderer.apply {
                    val armors = arrayListOf<Pair<ItemStack, String>>()
                    (3 downTo 0).map { mc.thePlayer.getCurrentArmor(it) }.forEach { armor ->
                        if (armor == null) return@forEach
                        val extraAttr = ItemUtil.getExtraAttributes(armor) ?: return@forEach
                        val killsKey =
                            extraAttr.keySet.find { it.endsWith("_kills") && extraAttr.getTagId(it) == ItemUtil.NBT_INTEGER.toByte() }
                        if (killsKey.isNullOrEmpty()) return@forEach
                        for (lore in ItemUtil.getItemLore(armor).asReversed()) {
                            if (lore == "§a§lMAXED OUT! NICE!") {
                                val kills = extraAttr.getInteger(killsKey)
                                armors.add(armor to "§a§lMAX §b(§f${NumberUtil.nf.format(kills)}§b)")
                                break
                            } else if (lore.startsWith("§7Next Upgrade:")) {
                                val match = upgradeBonusRegex.find(lore) ?: return@forEach
                                val currentKills =
                                    match.groups["kills"]!!.value.replace(",", "").toDoubleOrNull() ?: return@forEach
                                val nextKills = match.groups["nextKills"]!!.value.replace(",", "").toDoubleOrNull()
                                    ?: return@forEach
                                val percentToNext = (currentKills / nextKills * 100).roundToPrecision(1)
                                armors.add(armor to "§e$percentToNext% §b(§f${NumberUtil.nf.format(currentKills)}§b)")
                            }
                        }
                    }

                    if (armors.isNotEmpty()) {
                        val leftAlign = actualX < ScaledResolution(SlayerFeatures.mc).scaledWidth / 2f
                        if (!leftAlign) {
                            val longest = fontRenderer.getStringWidth((armors.maxByOrNull { it.second.length }
                                ?: null to "").second)
                            armors.forEachIndexed { index, pair ->
                                RenderUtil.renderItem(pair.first, longest + 2, index * 16)
                                fontRenderer.drawString(
                                    pair.second,
                                    longest - fontRenderer.getStringWidth(pair.second).toFloat(),
                                    index * 16 + 4.5f
                                )
                            }
                        } else {
                            armors.forEachIndexed { index, pair ->
                                RenderUtil.renderItem(pair.first, 0, index * 16)
                                fontRenderer.drawString(
                                    pair.second,
                                    18f,
                                    index * 16 + 4.5f
                                )
                            }
                        }
                    }
                }
            }
        }

        override fun demoRender() {
            val leftAlign = actualX < ScaledResolution(mc).scaledWidth / 2f
            val text = "§e99.9% §b(§f199§b)"
            if (leftAlign) {
                RenderUtil.renderItem(ItemStack(Items.leather_chestplate), 0, 0)
                ScreenRenderer.fontRenderer.drawString(
                    text,
                    18f,
                    4.5f
                )
            } else {
                ScreenRenderer.fontRenderer.drawString(
                    text,
                    0f,
                    4.5f
                )
                RenderUtil.renderItem(
                    ItemStack(Items.leather_chestplate),
                    ScreenRenderer.fontRenderer.getStringWidth(text) + 2,
                    0
                )
            }
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT + 5
        override val width: Int
            get() = 18 + ScreenRenderer.fontRenderer.getStringWidth("§e99.9% §b(§f199§b)")

        override val toggled: Boolean
            get() = Skytils.config.showSlayerArmorKills

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
        private val timerRegex = Regex("§c\\d+:\\d+§r")
        var slayerEntity: Entity? = null
        var slayerNameEntity: EntityArmorStand? = null
        var slayerTimerEntity: EntityArmorStand? = null
        var yangGlyphEntity: EntityArmorStand? = null
        var hasSlayerText = false
        private var lastTickHasSlayerText = false
        var expectedMaxHp: Int? = null
        private val hitMap = HashMap<EntityEnderman, Int>()
        private var yangGlyph: BlockPos? = null
        private val nukekebiHeads = arrayListOf<EntityArmorStand>()
        var BossHealths = HashMap<String, JsonObject>()

        fun processSlayerEntity(entity: Entity) {
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
                            val currentTier =
                                sidebarLines.map { cleanSB(it) }.find { it.startsWith("Revenant Horror ") }
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

        private fun detectSlayerEntities(entity: EntityLiving, name: String, timer: String?, nameStart: String) {
            TickTask(5) {
                val nearbyArmorStands = entity.entityWorld.getEntitiesInAABBexcluding(
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
                val potentialTimerEntities = arrayListOf<EntityArmorStand>()
                val potentialNameEntities = arrayListOf<EntityArmorStand>()
                for (nearby in nearbyArmorStands) {
                    if (nearby.displayName.formattedText.startsWith("§8[§7Lv")) continue
                    if (nearby.displayName.formattedText.startsWith(nameStart)
                    ) {
                        val currentTier =
                            sidebarLines.map { cleanSB(it) }.find { it.startsWith(name) }?.substringAfter(name)?.drop(1)
                                ?: ""
                        printDevMessage("expected tier $currentTier - spawned hp ${floor(entity.baseMaxHealth).toInt()}", "slayer")
                        if (BossHealths[name.substringBefore(" ")]?.get(currentTier)?.asInt
                            == floor(entity.baseMaxHealth).toInt()
                        ) {
                            printDevMessage("hp matched", "slayer")
                            potentialNameEntities.add(nearby as EntityArmorStand)
                        }
                        continue
                    }
                    if (nearby.displayName.formattedText == timer) {
                        printDevMessage("timer matched", "slayer")
                        potentialTimerEntities.add(nearby as EntityArmorStand)
                        continue
                    }
                    if (timer == null && nearby.displayName.formattedText.matches(timerRegex)) {
                        printDevMessage("timer regex matched", "slayer")
                        potentialTimerEntities.add(nearby as EntityArmorStand)
                        continue
                    }
                }
                if (potentialNameEntities.size == 1 && potentialTimerEntities.size == 1) {
                    slayerEntity = entity
                    slayerNameEntity = potentialNameEntities.first()
                    slayerTimerEntity = potentialTimerEntities.first()
                }
            }
        }


        init {
            SlayerArmorDisplayElement()
            SlayerDisplayElement()
            SeraphDisplayElement()
        }
    }
}