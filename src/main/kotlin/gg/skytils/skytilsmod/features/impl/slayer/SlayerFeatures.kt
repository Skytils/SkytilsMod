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
package gg.skytils.skytilsmod.features.impl.slayer

import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UChat
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.core.*
import gg.skytils.skytilsmod.core.GuiManager.createTitle
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.events.impl.CheckRenderEntityEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import gg.skytils.skytilsmod.events.impl.RenderHUDEvent
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.features.impl.handlers.PotionEffectTimers
import gg.skytils.skytilsmod.features.impl.slayer.base.Slayer
import gg.skytils.skytilsmod.features.impl.slayer.base.ThrowingSlayer
import gg.skytils.skytilsmod.features.impl.slayer.impl.BloodfiendSlayer
import gg.skytils.skytilsmod.features.impl.slayer.impl.DemonlordSlayer
import gg.skytils.skytilsmod.features.impl.slayer.impl.RevenantSlayer
import gg.skytils.skytilsmod.features.impl.slayer.impl.SeraphSlayer
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import gg.skytils.skytilsmod.utils.NumberUtil.toRoman
import gg.skytils.skytilsmod.utils.RenderUtil.drawFilledBoundingBox
import gg.skytils.skytilsmod.utils.RenderUtil.drawOutlinedBoundingBox
import gg.skytils.skytilsmod.utils.ScoreboardUtil.sidebarLines
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import net.minecraft.block.*
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.BossStatus
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.*
import net.minecraft.entity.passive.EntityWolf
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiScreenEvent
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
import java.awt.Color
import java.util.concurrent.Executors
import kotlin.math.floor


object SlayerFeatures : CoroutineScope {
    override val coroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher() + SupervisorJob()

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
    private val BLAZE_MINIBOSSES = arrayOf("Flare Demon", "Kindleheart Demon", "Burningsoul Demon")

    // there might be a point replacing this with §c\d+:\d+(?:§r)?$ and only partially check for matches
    // but that requires a more extensive testing of all skyblock timers,
    // something I am not quite particularly fond of doing
    internal val timerRegex =
        Regex("(?:§[8bef]§l(ASHEN|CRYSTAL|AURIC|SPIRIT)§[8bef] ♨\\d |§4§lIMMUNE )?§c\\d+:\\d+(?:§r)?")
    internal val totemRegex = Regex("§6§l(?<time>\\d+)s §c§l(?<hits>\\d+) hits")
    var slayer: Slayer<*>? = null
        set(value) {
            field?.unset()
            field = value
            value?.set()
        }
    val slayerEntity: Entity?
        get() = slayer?.entity
    var hasSlayerText = false
    private var lastTickHasSlayerText = false
    var expectedMaxHp: Int? = null
    private val hitMap = HashMap<EntityLivingBase, Int>()
    var BossHealths = HashMap<String, HashMap<String, Int>>()
    var maddoxCommand = ""

    fun processSlayerEntity(entity: Entity) {
        slayer = try {
            when (entity) {
                is EntityZombie -> RevenantSlayer(entity)
                is EntitySpider -> Slayer(entity, "Tarantula Broodfather", "§5☠ §4Tarantula Broodfather")
                is EntityWolf -> Slayer(entity, "Sven Packmaster", "§c☠ §fSven Packmaster")
                is EntityEnderman -> SeraphSlayer(entity)
                is EntityBlaze -> DemonlordSlayer(entity)
                is EntityOtherPlayerMP -> {
                    if (entity.name == "Bloodfiend ") {
                        BloodfiendSlayer(entity)
                    } else null
                }

                else -> null
            }
        } catch (e: IllegalStateException) {
            null
        }
    }

    internal fun getTier(name: String): String {
        return sidebarLines.find { it.startsWith(name) }?.substringAfter(name)?.drop(1)
            ?: (if (Skytils.config.slayerCarryMode > 0) Skytils.config.slayerCarryMode.toRoman() else "")
    }

    init {
        SlayerArmorDisplayElement()
        SlayerDisplayElement()
        SeraphDisplayElement()
        TotemDisplayElement
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return

        val formatted = event.message.formattedText

        if (Skytils.config.openMaddoxMenu) {
            if (formatted.contains("§2§l[OPEN MENU]")) {
                val listOfSiblings = event.message.siblings
                maddoxCommand =
                    listOfSiblings.find { it.unformattedText.contains("[OPEN MENU]") }?.chatStyle?.chatClickEvent?.value
                        ?: ""
                UChat.chat("$prefix §bOpen chat then click anywhere on screen to open Maddox Menu.")
            }
        }
    }

    @SubscribeEvent
    fun onMouseInputPost(event: GuiScreenEvent.MouseInputEvent.Post) {
        if (!Utils.inSkyblock) return
        if (Mouse.getEventButton() == 0 && event.gui is GuiChat) {
            if (Skytils.config.openMaddoxMenu && maddoxCommand.isNotBlank()) {
                Skytils.sendMessageQueue.add(maddoxCommand)
                maddoxCommand = ""
            }
        }
    }

    init {
        tickTimer(4, repeats = true) {
            if (Utils.inSkyblock && Skytils.config.showRNGMeter) {
                for ((index, line) in sidebarLines.withIndex()) {
                    if (line == "Slayer Quest") {
                        val boss = sidebarLines.elementAtOrNull(index + 1) ?: continue
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
                        if (boss.startsWith("Inferno Demonlord")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.blazeRNG,
                                    ChatComponentText("§c§lInferno Demonlord RNG§r - §d${Skytils.config.blazeRNG}%")
                                ), true
                            )
                            break
                        }
                        if (boss.startsWith("Riftstalker Bloodfiend")) {
                            BossStatus.setBossStatus(
                                RNGMeter(
                                    100f,
                                    Skytils.config.vampRNG,
                                    ChatComponentText("§4§lRiftstalker Bloodfiend RNG§r - §d${Skytils.config.vampRNG}%")
                                ), true
                            )
                            break
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inSkyblock) return
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null || mc.thePlayer == null) return
        lastTickHasSlayerText = hasSlayerText
        val index = sidebarLines.indexOf("Slay the boss!")
        hasSlayerText = index != -1
        if (!lastTickHasSlayerText && hasSlayerText) {
            sidebarLines.elementAtOrNull(index - 1)?.let {
                val boss = it.substringBefore(" ")
                val tier = it.substringAfterLast(" ")
                expectedMaxHp = BossHealths[boss]?.get(tier) ?: 0
            }
        }
        slayer?.tick(event)
    }

    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<EntityLivingBase>) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityArmorStand) {
            val entity = event.entity as EntityArmorStand
            if (!entity.hasCustomName()) return
            val name = entity.displayName.unformattedText
            if (Skytils.config.slayerBossHitbox && name.endsWith("§c❤") && !name.endsWith("§e0§c❤") && !mc.renderManager.isDebugBoundingBox) {
                val (x, y, z) = RenderUtil.fixRenderPos(event.x, event.y, event.z)
                if (ZOMBIE_MINIBOSSES.any { name.contains(it) } || BLAZE_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(x - 0.5, y - 2, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        RenderUtil.getPartialTicks()
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
                        ),
                        Color(0, 255, 255, 255),
                        3f,
                        RenderUtil.getPartialTicks()
                    )
                } else if (WOLF_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(x - 0.5, y - 1, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        RenderUtil.getPartialTicks()
                    )
                } else if (ENDERMAN_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        AxisAlignedBB(x - 0.5, y - 3, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        RenderUtil.getPartialTicks()
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
            (slayer as? SeraphSlayer)?.run {
                if (packet.entityId == entity.entityId) {
                    if (entity.heldBlockState?.block == Blocks.beacon && ((packet.func_149376_c()
                            .find { it.dataValueId == 16 } ?: return).`object` as Short).toInt().and(65535)
                            .and(4095) == 0
                    ) {
                        lastYangGlyphSwitch = System.currentTimeMillis()
                        lastYangGlyphSwitchTicks = 0
                        thrownBoundingBox = entity.entityBoundingBox
                        if (Skytils.config.yangGlyphPing && !Skytils.config.yangGlyphPingOnLand) createTitle(
                            "§cYang Glyph!",
                            30
                        )
                        yangGlyphAdrenalineStressCount = lastYangGlyphSwitch + 6000L
                    }
                }
            }
            if (Skytils.config.totemPing != 0 && packet.entityId == (slayer as? DemonlordSlayer)?.totemEntity?.entityId) {
                ((packet.func_149376_c().find { it.dataValueId == 2 } ?: return).`object` as String).let { name ->
                    printDevMessage("totem name updating: $name", "totem")
                    totemRegex.matchEntire(name)?.run {
                        printDevMessage("time ${groups["time"]}", "totem")
                        if (groups["time"]?.value?.toIntOrNull() == Skytils.config.totemPing)
                            createTitle("Totem!", 20)
                    }
                }
            } else if (packet.entityId == (slayer as? DemonlordSlayer)?.entity?.entityId &&
                (((packet.func_149376_c().find { it.dataValueId == 0 }
                    ?: return).`object` as Byte).toInt() and 0x20) == 0 &&
                slayer?.entity?.isInvisible == true
            ) {
                slayer?.run {
                    launch {
                        val (n, t) = detectSlayerEntities().first()
                        nameEntity = n
                        timerEntity = t
                    }
                }
            }
            (slayer as? BloodfiendSlayer)?.run {
                val newName = packet.func_149376_c()?.find { it.dataValueId == 2 }?.`object` as? String ?: return
                if (packet.entityId == nameEntity?.entityId) {
                    nameEntityChanged(newName)
                } else if (packet.entityId == timerEntity?.entityId) {
                    timerEntityChanged(newName)
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
                        val boss = sidebarLines.elementAtOrNull(index + 1) ?: continue
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
                        if (boss.startsWith("Inferno Demonlord")) {
                            Skytils.config.blazeRNG = rngMeter
                            break
                        }
                        if (boss.startsWith("Riftstalker Bloodfiend")) {
                            Skytils.config.vampRNG = rngMeter
                            break
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        (slayer as? ThrowingSlayer)?.blockChange(event)
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        val matrixStack = UMatrixStack()
        (slayer as? SeraphSlayer)?.run {
            if (Skytils.config.highlightYangGlyph) {
                thrownLocation?.let { yangGlyph ->
                    GlStateManager.disableCull() // is disabling cull even needed here?
                    UGraphics.disableDepth()
                    val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
                    val x = yangGlyph.x - viewerX
                    val y = yangGlyph.y - viewerY
                    val z = yangGlyph.z - viewerZ
                    drawFilledBoundingBox(
                        matrixStack,
                        AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                        Skytils.config.yangGlyphColor,
                        1f
                    )
                    UGraphics.enableDepth()
                    GlStateManager.enableCull()
                }
            }
            if (Skytils.config.highlightNukekebiHeads && nukekebiSkulls.isNotEmpty()) {
                nukekebiSkulls.also { it.removeAll { it.isDead } }.forEach { head ->
                    GlStateManager.disableCull() // same for this cull call?
                    UGraphics.disableDepth()
                    val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
                    val x = head.posX - viewerX
                    val y = head.posY - viewerY
                    val z = head.posZ - viewerZ
                    drawFilledBoundingBox(
                        matrixStack,
                        AxisAlignedBB(x - 0.25, y + 0.5, z - 0.25, x + 0.25, y + 1.5, z + 0.25),
                        Skytils.config.nukekebiHeadColor,
                        1f
                    )
                    UGraphics.enableDepth()
                    GlStateManager.enableCull()
                }
            }
        }
    }

    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        if (!Utils.inSkyblock) return
        (slayer as? ThrowingSlayer)?.run { entityJoinWorld(event) }
        if (!hasSlayerText) return
        if (slayer != null) {
            printDevMessage("boss not null", "slayerspam", "seraphspam")
            return
        }
        processSlayerEntity(event.entity)
    }

    @SubscribeEvent
    fun onClick(event: InputEvent.MouseInputEvent) {
        if (!Utils.inSkyblock || mc.pointedEntity == null || Skytils.config.slayerCarryMode == 0 || Mouse.getEventButton() != 2 || !Mouse.getEventButtonState() || mc.currentScreen != null || mc.thePlayer == null) return
        processSlayerEntity(mc.pointedEntity)
    }

    @SubscribeEvent
    fun onAttack(event: AttackEntityEvent) {
        val entity = event.target as? EntityLivingBase ?: return

        if (!hasSlayerText || !Utils.inSkyblock || event.entity != mc.thePlayer || !Skytils.config.useSlayerHitMethod) return
        if ((if (MayorInfo.mayorPerks.contains("DOUBLE MOBS HP!!!")) 2 else 1) * floor(entity.baseMaxHealth).toInt() == expectedMaxHp) {
            printDevMessage("A valid entity was attacked", "slayer", "seraph", "seraphHit")
            hitMap.compute(entity) { _, int ->
                return@compute (int ?: 0).inc()
            }
            if (entity != slayer?.entity && (hitMap[entity]
                    ?: 0) - ((slayer?.entity as? EntityEnderman)?.let { hitMap[it] } ?: 0) >= 10
            ) {
                printDevMessage("processing new entity", "slayer")
                processSlayerEntity(entity)
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
        slayer = null
        hitMap.clear()
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (Skytils.config.hideOthersBrokenHeartRadiation && event.entity.isInvisible && event.entity is EntityGuardian) {
            (slayer as? SeraphSlayer)?.run {
                if (entity.isRiding) {
                    printDevMessage("Slayer is Riding", "slayer", "seraph", "seraphRadiation")
                    if (event.entity.getDistanceSqToEntity(entity) > 3.5 * 3.5) {
                        printDevMessage("Guardian too far", "slayer", "seraph", "seraphRadiation")
                        event.isCanceled = true
                    }
                } else {
                    printDevMessage("Slayer not riding, removing guardian", "slayer", "seraph", "seraphRadiation")
                    event.isCanceled = true
                }
            }
        }
        if (Skytils.config.ignorePacifiedBlazes && event.entity is EntityBlaze && PotionEffectTimers.potionEffectTimers.containsKey(
                "Smoldering Polarization"
            )
        ) {
            (slayer as? DemonlordSlayer)?.run {
                if (event.entity.getDistanceSqToEntity(mc.renderViewEntity) > 3 * 3 && event.entity != entity) {
                    event.isCanceled = true
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderHud(event: RenderHUDEvent) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.pointYangGlyph) {
            val pos = (slayer as? SeraphSlayer)?.thrownLocation.toVec3()?.addVector(0.5, 0.5, 0.5)
                ?: (slayer as? SeraphSlayer)?.thrownEntity?.run { if (this.isEntityAlive) this else null }?.positionVector
                ?: return
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

    class SlayerDisplayElement : GuiElement("Slayer Display", x = 150, y = 20) {
        override fun render() {
            if (Utils.inSkyblock) {
                val leftAlign = scaleX < UResolution.scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                slayer?.run {
                    timerEntity?.run {
                        if (isDead) {
                            printDevMessage("timer died", "slayer", "seraph")
                            timerEntity = null
                        } else if (toggled) {
                            ScreenRenderer.fontRenderer.drawString(
                                displayName.formattedText,
                                if (leftAlign) 0f else width,
                                0f,
                                CommonColors.WHITE,
                                alignment,
                                SmartFontRenderer.TextShadow.NORMAL
                            )
                        }
                    }
                    nameEntity?.run {
                        if (isDead) {
                            printDevMessage("name died", "slayer", "seraph")
                            nameEntity = null
                        } else if (toggled) {
                            ScreenRenderer.fontRenderer.drawString(
                                displayName.formattedText,
                                if (leftAlign) 0f else width,
                                10f,
                                CommonColors.WHITE,
                                alignment,
                                SmartFontRenderer.TextShadow.NORMAL
                            )
                        }
                    }
                    if (entity.isDead) {
                        printDevMessage("slayer died", "slayer", "seraph")
                        if (Skytils.config.slayerTimeToKill) {
                            UChat.chat("$prefix §bSlayer took §f${slayerEntity!!.ticksExisted / 20f}§bs to kill")
                        }
                        slayer = null
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

    class SeraphDisplayElement : GuiElement("Seraph Display", x = 20, y = 20) {
        override fun render() {
            if (toggled && Utils.inSkyblock && slayerEntity != null && slayerEntity is EntityEnderman) {
                val leftAlign = scaleX < UResolution.scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                (slayer as? SeraphSlayer)?.run {
                    nameEntity?.run {
                        ScreenRenderer.fontRenderer.drawString(
                            if (hitPhase)
                                "§dShield Phase"
                            else
                                "§6Damage Phase",
                            if (leftAlign) 0f else width,
                            0f,
                            CommonColors.WHITE,
                            alignment,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                    }
                    entity.heldBlockState?.takeIf { it.block is BlockBeacon }?.run {
                        ScreenRenderer.fontRenderer.drawString(
                            "§cHolding beacon!",
                            if (leftAlign) 0f else width.toFloat(),
                            10f,
                            CommonColors.WHITE,
                            alignment,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                    } ?: if (lastYangGlyphSwitchTicks != -1) {
                        ScreenRenderer.fontRenderer.drawString(
                            "§cBeacon thrown! ${(System.currentTimeMillis() - yangGlyphAdrenalineStressCount) / 1000f}s",
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
                    ScreenRenderer.fontRenderer.drawString(
                        if (thrownLocation != null)
                            "§cYang Glyph placed! ${(System.currentTimeMillis() - yangGlyphAdrenalineStressCount) / 1000f}s"
                        else
                            "§bNo yang glyph",
                        if (leftAlign) 0f else width.toFloat(),
                        20f,
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                    ScreenRenderer.fontRenderer.drawString(
                        if (nukekebiSkulls.size > 0)
                            "§dNukekebi Heads: §c${nukekebiSkulls.size}"
                        else
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
            val leftAlign = scaleX < UResolution.scaledWidth / 2f
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

    object TotemDisplayElement : GuiElement("Totem Display", x = 20, y = 50) {
        override fun render() {
            (slayer as? DemonlordSlayer)?.totemEntity?.run {
                val leftAlign = scaleX < UResolution.scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    displayName.formattedText,
                    if (leftAlign) 0f else width,
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val leftAlign = scaleX < UResolution.scaledWidth / 2f
            val alignment =
                if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "§6§l5s §c§l5 hits",
                if (leftAlign) 0f else width.toFloat(),
                0f,
                CommonColors.WHITE,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§6§l5s §c§l5 hits")

        override val toggled: Boolean
            get() = Skytils.config.showTotemDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class SlayerArmorDisplayElement : GuiElement("Slayer Armor Display", x = 150, y = 20) {
        private val upgradeBonusRegex =
            Regex("§7Next Upgrade: §a\\+(?<nextDefense>[\\d,]+?)❈ §8\\(§a(?<kills>[\\d,]+)§7/§c(?<nextKills>[\\d,]+)§8\\)")

        override fun render() {
            if (Utils.inSkyblock && toggled && mc.thePlayer != null) {
                ScreenRenderer.apply {
                    val armors = ArrayList<Pair<ItemStack, String>>(4)
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
                        val leftAlign = scaleX < UResolution.scaledWidth / 2f
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
            val leftAlign = scaleX < UResolution.scaledWidth / 2f
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

}
