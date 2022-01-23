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

import com.google.gson.JsonObject
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UResolution
import net.minecraft.block.*
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
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
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.GuiManager.Companion.createTitle
import skytils.skytilsmod.core.SoundQueue
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.impl.BlockChangeEvent
import skytils.skytilsmod.events.impl.CheckRenderEntityEvent
import skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import skytils.skytilsmod.events.impl.RenderHUDEvent
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.mixins.transformers.accessors.AccessorMinecraft
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import skytils.skytilsmod.utils.NumberUtil.toRoman
import skytils.skytilsmod.utils.RenderUtil.drawFilledBoundingBox
import skytils.skytilsmod.utils.RenderUtil.drawOutlinedBoundingBox
import skytils.skytilsmod.utils.ScoreboardUtil.sidebarLines
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.awt.Color
import kotlin.math.abs
import kotlin.math.floor


class SlayerFeatures {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inSkyblock) return
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null || mc.thePlayer == null) return
        lastTickHasSlayerText = hasSlayerText
        hasSlayerText = sidebarLines.any { it == "Slay the boss!" }
        if (!lastTickHasSlayerText && hasSlayerText) {
            val currentTier =
                sidebarLines.find { it.startsWith("Voidgloom Seraph") }
                    ?.substringAfter("Voidgloom Seraph")?.drop(1)
                    ?: ""
            expectedMaxHp = BossHealths["Voidgloom"]?.get(currentTier)?.asInt
        }
        if (lastYangGlyphSwitchTicks >= 0) lastYangGlyphSwitchTicks++
        if (lastYangGlyphSwitchTicks > 100) lastYangGlyphSwitchTicks = 0
        if (Skytils.config.experimentalYangGlyphDetection && lastYangGlyphSwitchTicks >= 0 && yangGlyphEntity == null && yangGlyph == null && slayerEntity != null) {
            val suspect = mc.theWorld.getEntitiesWithinAABB(
                EntityArmorStand::class.java,
                slayerEntity!!.entityBoundingBox.expand(20.69, 20.69, 20.69)
            ) { e ->
                e as EntityArmorStand
                e.ticksExisted <= 300 && lastYangGlyphSwitchTicks + 5 > e.ticksExisted && e.inventory[4]?.item == Item.getItemFromBlock(
                    Blocks.beacon
                )
            }.minByOrNull {
                (abs(lastYangGlyphSwitchTicks - it.ticksExisted) * 10) + slayerEntity!!.getDistanceSqToEntity(it)
            }
            if (suspect != null) {
                printDevMessage(
                    "Found suspect glyph, $lastYangGlyphSwitchTicks switched, ${suspect.ticksExisted} existed, ${
                        slayerEntity!!.getDistanceSqToEntity(
                            suspect
                        )
                    } distance", "slayer", "seraph", "seraphGlyph"
                )
                yangGlyphEntity = suspect
            }
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
                        val isDanger = when {
                            blockUnder.block === Blocks.stone_slab && blockUnder.getValue(BlockHalfStoneSlab.VARIANT) == BlockStoneSlab.EnumType.QUARTZ -> true
                            blockUnder.block === Blocks.quartz_stairs || blockUnder.block === Blocks.acacia_stairs -> true
                            blockUnder.block === Blocks.wooden_slab && blockUnder.getValue(BlockHalfWoodSlab.VARIANT) == BlockPlanks.EnumType.ACACIA -> true
                            blockUnder.block === Blocks.stained_hardened_clay -> {
                                val color = Blocks.stained_hardened_clay.getMetaFromState(blockUnder)
                                color == 0 || color == 8 || color == 14
                            }
                            blockUnder.block === Blocks.bedrock -> true
                            else -> false
                        }
                        if (isDanger) {
                            SoundQueue.addToQueue("random.orb", 1f)
                        }
                    }
                }
            }
            if (Skytils.config.showRNGMeter) {
                for ((index, line) in sidebarLines.withIndex()) {
                    if (line == "Slayer Quest") {
                        val boss = sidebarLines.elementAtOrNull(index - 1) ?: continue
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
                val x =
                    entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (mc as AccessorMinecraft).timer.renderPartialTicks
                val y =
                    entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (mc as AccessorMinecraft).timer.renderPartialTicks
                val z =
                    entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (mc as AccessorMinecraft).timer.renderPartialTicks
                if (ZOMBIE_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(x - 0.5, y - 2, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        (mc as AccessorMinecraft).timer.renderPartialTicks
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
                        ), Color(0, 255, 255, 255), 3f, (mc as AccessorMinecraft).timer.renderPartialTicks
                    )
                } else if (WOLF_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(x - 0.5, y - 1, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        (mc as AccessorMinecraft).timer.renderPartialTicks
                    )
                } else if (ENDERMAN_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(x - 0.5, y - 3, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        (mc as AccessorMinecraft).timer.renderPartialTicks
                    )
                }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock) return
        val packet = event.packet
        if (packet is S1CPacketEntityMetadata) {
            if (packet.entityId == slayerEntity?.entityId && slayerEntity is EntityEnderman) {
                (slayerEntity as EntityEnderman).apply {
                    if (heldBlockState?.block == Blocks.beacon && ((packet.func_149376_c()
                            .find { it.dataValueId == 16 } ?: return@apply).`object` as Short).toInt().and(65535)
                            .and(4095) == 0
                    ) {
                        lastYangGlyphSwitch = System.currentTimeMillis()
                        lastYangGlyphSwitchTicks = 0
                        thrownBoundingBox = entityBoundingBox
                        if (Skytils.config.yangGlyphPing && !Skytils.config.yangGlyphPingOnLand) createTitle(
                            "§cYang Glyph!",
                            30
                        )
                    }
                }
            }
        }

        if (packet is S29PacketSoundEffect) {
            if (Skytils.config.slayerMinibossSpawnAlert && slayerEntity == null && packet.soundName == "random.explode" && packet.volume == 0.6f && packet.pitch == 9 / 7f && GuiManager.title != "§cMINIBOSS" && sidebarLines.any {
                    it.contains("Slayer Quest")
                }) {
                createTitle("§cMINIBOSS", 20)
            }
        }

        if (packet is S02PacketChat) {
            val unformatted = packet.chatComponent.unformattedText.stripControlCodes()
            if (unformatted.trim().startsWith("RNGesus Meter")) {
                val rngMeter =
                    unformatted.filter { it.isDigit() || it == '.' }.toFloat()
                for ((index, line) in sidebarLines.withIndex()) {
                    if (line == "Slayer Quest") {
                        val boss = sidebarLines.elementAtOrNull(index - 1) ?: continue
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
            yangGlyphEntity = null
            return
        }
        if (slayerEntity == null) return
        if (event.update.block is BlockBeacon) {
            if (yangGlyphEntity != null) {
                printDevMessage("Glyph Entity exists", "slayer", "seraph", "seraphGlyph")
                if (event.update.block is BlockBeacon && yangGlyphEntity!!.position.distanceSq(event.pos) <= 3.5 * 3.5) {
                    printDevMessage("Beacon entity near beacon block!", "slayer", "seraph", "seraphGlyph")
                    yangGlyph = event.pos
                    yangGlyphEntity = null
                    if (Skytils.config.yangGlyphPing && Skytils.config.yangGlyphPingOnLand) createTitle(
                        "§cYang Glyph!",
                        30
                    )
                    lastYangGlyphSwitchTicks = -1
                }
            }
            if (Skytils.config.experimentalYangGlyphDetection && yangGlyph == null && slayerEntity != null) {
                if (lastYangGlyphSwitchTicks in 0..5 && slayerEntity!!.getDistanceSq(event.pos) <= 5 * 5) {
                    if (Skytils.config.yangGlyphPing && Skytils.config.yangGlyphPingOnLand) createTitle(
                        "§cYang Glyph!",
                        30
                    )
                    printDevMessage(
                        "Beacon was close to slayer, $lastYangGlyphSwitchTicks", "slayer", "seraph", "seraphGlyph"
                    )
                    yangGlyph = event.pos
                    lastYangGlyphSwitchTicks = -1
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
                    AxisAlignedBB(x - 0.25, y + 0.5, z - 0.25, x + 0.25, y + 1.5, z + 0.25),
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
                if (slayerEntity != null) {
                    if (e.inventory[4]?.item == Item.getItemFromBlock(Blocks.beacon)) {
                        val time = System.currentTimeMillis() - 50
                        printDevMessage(
                            "Found beacon armor stand, time diff ${time - lastYangGlyphSwitch}",
                            "slayer",
                            "seraph",
                            "seraphGlyph"
                        )
                        if (lastYangGlyphSwitch != -1L && time - lastYangGlyphSwitch < 300 && e.entityBoundingBox.expand(
                                4.5,
                                4.0,
                                4.5
                            )
                                .intersectsWith(thrownBoundingBox ?: e.entityBoundingBox)
                        ) {
                            printDevMessage(
                                "Beacon armor stand is close to slayer entity",
                                "slayer",
                                "seraph",
                                "seraphGlyph"
                            )
                            yangGlyphEntity = e
                            lastYangGlyphSwitch = -1L
                            lastYangGlyphSwitchTicks = -1
                        }
                    } else if (e.entityBoundingBox.expand(2.0, 3.0, 2.0)
                            .intersectsWith(slayerEntity!!.entityBoundingBox)
                    ) {
                        printDevMessage("Found nearby armor stand", "slayer", "seraph", "seraphGlyph", "seraphFixation")
                        for (item in e.inventory) {
                            if (item == null) continue
                            if (item.item == Items.skull) {
                                if (ItemUtil.getSkullTexture(item) == "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0=") {
                                    nukekebiHeads.add(e)
                                }
                                break
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
    fun onClick(event: InputEvent.MouseInputEvent) {
        if (!Utils.inSkyblock || mc.pointedEntity == null || Skytils.config.slayerCarryMode == 0 || Mouse.getEventButton() != 2 || !Mouse.getEventButtonState() || mc.currentScreen != null || mc.thePlayer == null) return
        processSlayerEntity(mc.pointedEntity, false)
    }

    @SubscribeEvent
    fun onAttack(event: AttackEntityEvent) {
        if (!hasSlayerText || !Utils.inSkyblock || event.entity != mc.thePlayer || event.target !is EntityEnderman || !Skytils.config.useSlayerHitMethod) return
        val enderman = event.target as EntityEnderman
        if ((if (MayorInfo.mayorPerks.contains("DOUBLE MOBS HP!!!")) 2 else 1) * floor(enderman.baseMaxHealth).toInt() == expectedMaxHp) {
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
            hitMap.remove(event.entity)
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        slayerEntity = null
        slayerNameEntity = null
        slayerTimerEntity = null
        hitMap.clear()
        yangGlyph = null
        yangGlyphEntity = null
        lastYangGlyphSwitch = -1
        lastYangGlyphSwitchTicks = -1
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


    @SubscribeEvent
    fun onRenderHud(event: RenderHUDEvent) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.pointYangGlyph) {
            val pos = yangGlyph.toVec3()?.addVector(0.5, 0.5, 0.5)
                ?: yangGlyphEntity?.run { if (this.isEntityAlive) this else null }?.positionVector ?: return
            val x = UResolution.scaledWidth / 2.0
            val y = UResolution.scaledHeight / 2.0
            val angle: Double = -(MathHelper.atan2(
                pos.xCoord - mc.thePlayer.posX,
                pos.zCoord - mc.thePlayer.posZ
            ) * 57.29577951308232) - mc.thePlayer.rotationYaw
            GlStateManager.pushMatrix()
            GlStateManager.translate(x, y, 0.0)
            GlStateManager.rotate(angle.toFloat(), 0f, 0f, 1f)
            GlStateManager.translate(-x, -y, 0.0)
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            val tes = Tessellator.getInstance()
            val wr = tes.worldRenderer
            Skytils.config.yangGlyphColor.withAlpha(255).bindColor()
            GL11.glLineWidth(5f)
            wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            wr.pos(x + 10, y + 45, 0.0).endVertex()
            wr.pos(x, y, 0.0).endVertex()
            wr.pos(x - 10, y + 45, 0.0).endVertex()
            tes.draw()

            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
        }
    }

    private class RNGMeter(val max: Float, val current: Float, val name: IChatComponent) : IBossDisplayData {

        override fun getMaxHealth(): Float = max

        override fun getHealth(): Float = current

        override fun getDisplayName() = name

    }

    class SlayerDisplayElement : GuiElement("Slayer Display", FloatPair(150, 20)) {
        override fun render() {
            if (Utils.inSkyblock) {
                val leftAlign = actualX < UResolution.scaledWidth / 2f
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
                        lastYangGlyphSwitch = -1L
                        lastYangGlyphSwitchTicks = -1
                        yangGlyph = null
                        yangGlyphEntity = null
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
                val leftAlign = actualX < UResolution.scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                if (slayerNameEntity != null) {
                    if (slayerNameEntity!!.displayName.formattedText.dropLastWhile { it == 's' }
                            .endsWith(" Hit")) {
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
            val leftAlign = actualX < UResolution.scaledWidth / 2f
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
        private val upgradeBonusRegex =
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
                        val leftAlign = actualX < UResolution.scaledWidth / 2f
                        if (!leftAlign) {
                            val longest = fontRenderer.getStringWidth(((armors.maxByOrNull { it.second.length }
                                ?: (null to ""))).second)
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
            val leftAlign = actualX < UResolution.scaledWidth / 2f
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
        private val timerRegex = Regex("§c\\d+:\\d+§r")
        var slayerEntity: Entity? = null
        var slayerNameEntity: EntityArmorStand? = null
        var slayerTimerEntity: EntityArmorStand? = null
        var yangGlyphEntity: EntityArmorStand? = null
        var hasSlayerText = false
        private var lastTickHasSlayerText = false
        var expectedMaxHp: Int? = null
        private val hitMap = HashMap<EntityEnderman, Int>()
        var yangGlyph: BlockPos? = null
        var lastYangGlyphSwitch: Long = -1L
        var lastYangGlyphSwitchTicks = -1
        var thrownBoundingBox: AxisAlignedBB? = null
        private val nukekebiHeads = arrayListOf<EntityArmorStand>()
        var BossHealths = HashMap<String, JsonObject>()

        fun processSlayerEntity(entity: Entity, countTime: Boolean = true) {
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
                    val currentTier =
                        sidebarLines.find { it.startsWith("Revenant Horror ") }
                            ?.substringAfter("Revenant Horror ")
                            ?: Skytils.config.slayerCarryMode.toRoman()
                    for (nearby in nearbyArmorStands) {
                        if (nearby.displayName.formattedText.startsWith("§8[§7Lv")) continue
                        if (nearby.displayName.formattedText.startsWith("§c☠ §bRevenant Horror") ||
                            nearby.displayName.formattedText.startsWith("§c☠ §fAtoned Horror")
                        ) {
                            if ((if (MayorInfo.mayorPerks.contains("DOUBLE MOBS HP!!!")) 2 else 1) * (BossHealths["Revenant"]?.get(
                                    currentTier
                                )?.asInt ?: 0)
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
                        if (!countTime && nearby.displayName.formattedText.matches(timerRegex)) {
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
                detectSlayerEntities(
                    entity,
                    "Tarantula Broodfather",
                    if (countTime) "§c02:59§r" else null,
                    "§5☠ §4Tarantula Broodfather"
                )
            } else if (entity is EntityWolf) {
                detectSlayerEntities(
                    entity,
                    "Sven Packmaster",
                    if (countTime) "§c03:59§r" else null,
                    "§c☠ §fSven Packmaster"
                )
            } else if (entity is EntityEnderman) {
                detectSlayerEntities(
                    entity,
                    "Voidgloom Seraph",
                    if (countTime) "§c03:59§r" else null,
                    "§c☠ §bVoidgloom Seraph"
                )
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
                            sidebarLines.find { it.startsWith(name) }?.substringAfter(name)?.drop(1)
                                ?: Skytils.config.slayerCarryMode.toRoman()
                        val expectedHealth =
                            (if (MayorInfo.mayorPerks.contains("DOUBLE MOBS HP!!!")) 2 else 1) * (BossHealths[name.substringBefore(
                                " "
                            )]?.get(currentTier)?.asInt ?: 0)
                        printDevMessage(
                            "expected tier $currentTier, hp $expectedHealth - spawned hp ${entity.baseMaxHealth}",
                            "slayer"
                        )
                        if (expectedHealth == floor(entity.baseMaxHealth).toInt()) {
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