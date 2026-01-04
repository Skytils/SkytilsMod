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

import com.mojang.blaze3d.opengl.GlStateManager
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.impl.entity.EntityAttackEvent
import gg.skytils.event.impl.entity.EntityJoinWorldEvent
import gg.skytils.event.impl.entity.LivingEntityDeathEvent
import gg.skytils.event.impl.play.MouseInputEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.render.CheckRenderEntityEvent
import gg.skytils.event.impl.render.LivingEntityPreRenderEvent
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.impl.world.BlockStateUpdateEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod._event.RenderHUDEvent
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.GuiManager.createTitle
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.features.impl.handlers.PotionEffectTimers
import gg.skytils.skytilsmod.features.impl.slayer.base.Slayer
import gg.skytils.skytilsmod.features.impl.slayer.base.ThrowingSlayer
import gg.skytils.skytilsmod.features.impl.slayer.impl.BloodfiendSlayer
import gg.skytils.skytilsmod.features.impl.slayer.impl.DemonlordSlayer
import gg.skytils.skytilsmod.features.impl.slayer.impl.RevenantSlayer
import gg.skytils.skytilsmod.features.impl.slayer.impl.SeraphSlayer
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.NumberUtil.toRoman
import gg.skytils.skytilsmod.utils.RenderUtil.drawFilledBoundingBox
import gg.skytils.skytilsmod.utils.RenderUtil.drawOutlinedBoundingBox
import gg.skytils.skytilsmod.utils.ScoreboardUtil.sidebarLines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.passive.WolfEntity
import net.minecraft.block.Blocks
import net.minecraft.entity.mob.BlazeEntity
import net.minecraft.entity.mob.EndermanEntity
import net.minecraft.entity.mob.GuardianEntity
import net.minecraft.entity.mob.SpiderEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import java.awt.Color
import java.util.concurrent.Executors
import kotlin.math.floor


object SlayerFeatures : EventSubscriber, CoroutineScope {
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
    private val hitMap = HashMap<LivingEntity, Int>()
    var BossHealths = HashMap<String, HashMap<String, Int>>()

    fun processSlayerEntity(entity: Entity) {
        slayer = try {
            when (entity) {
                is ZombieEntity -> RevenantSlayer(entity)
                is SpiderEntity -> Slayer(entity, "Tarantula Broodfather", "§5☠ §4Tarantula Broodfather")
                is WolfEntity -> Slayer(entity, "Sven Packmaster", "§c☠ §fSven Packmaster")
                is EndermanEntity -> SeraphSlayer(entity)
                is BlazeEntity -> DemonlordSlayer(entity)
                is OtherClientPlayerEntity -> {
                    if (entity.name.string == "Bloodfiend ") {
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
//        SlayerArmorDisplayElement()
//        SlayerDisplayElement()
//        SeraphDisplayElement()
//        TotemDisplayElement
    }

    // TODO: Fix later
//    init {
//        tickTimer(4, repeats = true) {
//            if (Utils.inSkyblock && Skytils.config.showRNGMeter) {
//                for ((index, line) in sidebarLines.withIndex()) {
//                    if (line == "Slayer Quest") {
//                        val boss = sidebarLines.elementAtOrNull(index + 1) ?: continue
//                        if (boss.startsWith("Revenant Horror")) {
//                            class_0_1003.method_0_3293(
//                                RNGMeter(
//                                    100f,
//                                    Skytils.config.revRNG,
//                                    LiteralTextContent("§2§lRevenant Horror RNG§r - §d${Skytils.config.revRNG}%")
//                                ), true
//                            )
//                            break
//                        }
//                        if (boss.startsWith("Tarantula Broodfather")) {
//                            class_0_1003.method_0_3293(
//                                RNGMeter(
//                                    100f,
//                                    Skytils.config.taraRNG,
//                                    LiteralTextContent("§8§lTarantula Broodfather RNG§r - §d${Skytils.config.taraRNG}%")
//                                ), true
//                            )
//                            break
//                        }
//                        if (boss.startsWith("Sven Packmaster")) {
//                            class_0_1003.method_0_3293(
//                                RNGMeter(
//                                    100f,
//                                    Skytils.config.svenRNG,
//                                    LiteralTextContent("§7§lSven Packmaster RNG§r - §d${Skytils.config.svenRNG}%")
//                                ), true
//                            )
//                            break
//                        }
//                        if (boss.startsWith("Voidgloom Seraph")) {
//                            class_0_1003.method_0_3293(
//                                RNGMeter(
//                                    100f,
//                                    Skytils.config.voidRNG,
//                                    LiteralTextContent("§5§lVoidgloom Seraph RNG§r - §d${Skytils.config.voidRNG}%")
//                                ), true
//                            )
//                            break
//                        }
//                        if (boss.startsWith("Inferno Demonlord")) {
//                            class_0_1003.method_0_3293(
//                                RNGMeter(
//                                    100f,
//                                    Skytils.config.blazeRNG,
//                                    LiteralTextContent("§c§lInferno Demonlord RNG§r - §d${Skytils.config.blazeRNG}%")
//                                ), true
//                            )
//                            break
//                        }
//                        if (boss.startsWith("Riftstalker Bloodfiend")) {
//                            class_0_1003.method_0_3293(
//                                RNGMeter(
//                                    100f,
//                                    Skytils.config.vampRNG,
//                                    LiteralTextContent("§4§lRiftstalker Bloodfiend RNG§r - §d${Skytils.config.vampRNG}%")
//                                ), true
//                            )
//                            break
//                        }
//                    }
//                }
//            }
//        }
//    }

    override fun setup() {
        register(::onTick)
        register(::onRenderLivingPre)
        register(::onReceivePacket)
        register(::onBlockChange)
        register(::onWorldRender)
        register(::onEntityJoinWorld)
        register(::onClick)
        register(::onAttack)
        register(::onDeath)
        register(::onWorldLoad)
        register(::onCheckRender)
        register(::onRenderHud)
    }

    fun onTick(event: TickEvent) {
        if (!Utils.inSkyblock) return
        if (mc.world == null || mc.player == null) return
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

    fun onRenderLivingPre(event: LivingEntityPreRenderEvent<*, *, *>) {
        if (!Utils.inSkyblock) return
        if (event.entity is ArmorStandEntity) {
            val entity = event.entity as ArmorStandEntity
            if (!entity.hasCustomName()) return
            val name = entity.displayName?.string ?: return
            if (Skytils.config.slayerBossHitbox && name.endsWith("§c❤") && !name.endsWith("§e0§c❤") && !event.hitboxesEnabled) {
                val (x, y, z) = RenderUtil.fixRenderPos(event.x, event.y, event.z)
                if (ZOMBIE_MINIBOSSES.any { name.contains(it) } || BLAZE_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        Box(x - 0.5, y - 2, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        RenderUtil.getPartialTicks()
                    )
                } else if (SPIDER_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        Box(
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
                        Box(x - 0.5, y - 1, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        RenderUtil.getPartialTicks()
                    )
                } else if (ENDERMAN_MINIBOSSES.any { name.contains(it) }) {
                    drawOutlinedBoundingBox(
                        Box(x - 0.5, y - 3, z - 0.5, x + 0.5, y, z + 0.5),
                        Color(0, 255, 255, 255),
                        3f,
                        RenderUtil.getPartialTicks()
                    )
                }
            }
        }
    }

    fun onReceivePacket(event: PacketReceiveEvent<*>) {
        if (!Utils.inSkyblock) return
        val packet = event.packet
        if (packet is EntityTrackerUpdateS2CPacket) {
            (slayer as? SeraphSlayer)?.run {
                if (packet.id() == entity.id) {
                    if (entity.carriedBlock?.block == Blocks.BEACON  && ((packet.trackedValues?.find { it.id() == 16 } ?: return)
                            .value() as Short).toInt().and(65535)
                            .and(4095) == 0
                    ) {
                        lastYangGlyphSwitch = System.currentTimeMillis()
                        lastYangGlyphSwitchTicks = 0
                        thrownBoundingBox = entity.boundingBox
                        if (Skytils.config.yangGlyphPing && !Skytils.config.yangGlyphPingOnLand) createTitle(
                            "§cYang Glyph!",
                            30
                        )
                        yangGlyphAdrenalineStressCount = lastYangGlyphSwitch + 6000L
                    }
                }
            }
            if (Skytils.config.totemPing != 0 && packet.id() == (slayer as? DemonlordSlayer)?.totemEntity?.id) {
                ((packet.trackedValues?.find { it.id() == 2 } ?: return).value() as String).let { name ->
                    printDevMessage({ "totem name updating: $name" }, "totem")
                    totemRegex.matchEntire(name)?.run {
                        printDevMessage({ "time ${groups["time"]}" }, "totem")
                        if (groups["time"]?.value?.toIntOrNull() == Skytils.config.totemPing)
                            createTitle("Totem!", 20)
                    }
                }
            } else if (packet.id() == (slayer as? DemonlordSlayer)?.entity?.id &&
                (((packet.trackedValues?.find { it.id() == 0 }
                    ?: return).value() as Byte).toInt() and 0x20) == 0 &&
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
                val newName = packet.trackedValues?.find { it.id() == 2 }?.value() as? String ?: return
                if (packet.id() == nameEntity?.id) {
                    nameEntityChanged(newName)
                } else if (packet.id() == timerEntity?.id) {
                    timerEntityChanged(newName)
                }
            }

            if (hasSlayerText && slayer == null && Skytils.config.useNametagHitMethod) {
                val entity = mc.world?.getEntityById(packet.id())
                printDevMessage("entity is null?", "slayerspam")
                if (entity != null && entity is ArmorStandEntity) {
                    val name = packet.trackedValues?.find { it.id() == 2 }?.value() as? String ?: return
                    if (name != null) {
                        printDevMessage({ "Checking entity nametag $name, was empty ${entity.customName?.string?.isEmpty()}" }, "slayerspam")
                        if (name.startsWith("§eSpawned by: ") && name.endsWith(mc.player!!.name.string)) {
                            printDevMessage("Detected spawned text", "slayerspam", "slayer")
                            mc.world?.getOtherEntities(entity, entity.boundingBox.expand(0.5, 0.5, 0.5))?.filter {
                                it is MobEntity && (if (MayorInfo.allPerks.contains("DOUBLE MOBS HP!!!")) 2 else 1) * floor(it.baseMaxHealth).toInt() == expectedMaxHp
                            }?.minByOrNull {
                                it.squaredDistanceTo(entity)
                            }?.let {
                                printDevMessage("Found entity from nametag", "slayerspam", "slayer")
                                processSlayerEntity(it)
                            }
                        }
                    }
                }
            }
        }

        if (packet is PlaySoundS2CPacket) {
            if (Skytils.config.slayerMinibossSpawnAlert && slayerEntity == null && packet.sound.value() == SoundEvents.ENTITY_GENERIC_EXPLODE.value() && packet.volume == 0.6f && packet.pitch == 9 / 7f && GuiManager.title != "§cMINIBOSS" && sidebarLines.any {
                    it.contains("Slayer Quest")
                }) {
                createTitle("§cMINIBOSS", 20)
            }
        }

        if (packet is ChatMessageS2CPacket) {
            val unformatted = (packet.unsignedContent ?: return).string.stripControlCodes()
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

    fun onBlockChange(event: BlockStateUpdateEvent) {
        (slayer as? ThrowingSlayer)?.blockChange(event)
    }

    fun onWorldRender(event: WorldDrawEvent) {
        if (!Utils.inSkyblock) return
        val matrixStack = UMatrixStack()
        (slayer as? SeraphSlayer)?.run {
            if (Skytils.config.highlightYangGlyph) {
                thrownLocation?.let { yangGlyph ->
                    GlStateManager._disableCull() // is disabling cull even needed here?
                    UGraphics.disableDepth()
                    val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
                    val x = yangGlyph.x - viewerX
                    val y = yangGlyph.y - viewerY
                    val z = yangGlyph.z - viewerZ
                    drawFilledBoundingBox(
                        matrixStack,
                        Box(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                        Skytils.config.yangGlyphColor,
                        1f
                    )
                    UGraphics.enableDepth()
                    GlStateManager._enableCull()
                }
            }
            if (Skytils.config.highlightNukekebiHeads && nukekebiSkulls.isNotEmpty()) {
                nukekebiSkulls.also { it.removeAll { it.isRemoved } }.forEach { head ->
                    GlStateManager._disableCull() // same for this cull call?
                    UGraphics.disableDepth()
                    val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
                    val x = head.x - viewerX
                    val y = head.y - viewerY
                    val z = head.z - viewerZ
                    drawFilledBoundingBox(
                        matrixStack,
                        Box(x - 0.25, y + 0.5, z - 0.25, x + 0.25, y + 1.5, z + 0.25),
                        Skytils.config.nukekebiHeadColor,
                        1f
                    )
                    UGraphics.enableDepth()
                    GlStateManager._enableCull()
                }
            }
        }
    }

    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        if (!Utils.inSkyblock) return
        (slayer as? ThrowingSlayer)?.run { entityJoinWorld(event) }
        if (!hasSlayerText) return
        if (slayer != null) {
            printDevMessage("boss not null", "slayerspam", "seraphspam")
            return
        }
        if (!Skytils.config.useNametagHitMethod) {
            processSlayerEntity(event.entity)
        }
    }

    fun onClick(event: MouseInputEvent) {
        if (!Utils.inSkyblock || Skytils.config.slayerCarryMode == 0 || event.button != 2|| mc.currentScreen != null || mc.player == null) return
        mc.targetedEntity?.let(::processSlayerEntity)
    }

    fun onAttack(event: EntityAttackEvent) {
        val entity = event.target as? LivingEntity ?: return

        if (!hasSlayerText || !Utils.inSkyblock || event.entity != mc.player || !Skytils.config.useSlayerHitMethod) return
        if ((if (MayorInfo.allPerks.contains("DOUBLE MOBS HP!!!")) 2 else 1) * floor(entity.baseMaxHealth).toInt() == expectedMaxHp) {
            printDevMessage("A valid entity was attacked", "slayer", "seraph", "seraphHit")
            hitMap.compute(entity) { _, int ->
                return@compute (int ?: 0).inc()
            }
            if (entity != slayer?.entity && (hitMap[entity]
                    ?: 0) - ((slayer?.entity as? EndermanEntity)?.let { hitMap[it] } ?: 0) >= 10
            ) {
                printDevMessage("processing new entity", "slayer")
                processSlayerEntity(entity)
            }
        }
    }

    fun onDeath(event: LivingEntityDeathEvent) {
        if (!Utils.inSkyblock) return
        if (event.entity is EndermanEntity) {
            hitMap.remove(event.entity)
        }
    }

    fun onWorldLoad(event: WorldUnloadEvent) {
        slayer = null
        hitMap.clear()
    }

    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (Skytils.config.hideOthersBrokenHeartRadiation && event.entity.isInvisible && event.entity is GuardianEntity) {
            (slayer as? SeraphSlayer)?.run {
                if (entity.hasVehicle()) {
                    printDevMessage("Slayer is Riding", "slayer", "seraph", "seraphRadiation")
                    if (event.entity.squaredDistanceTo(entity) > 3.5 * 3.5) {
                        printDevMessage("Guardian too far", "slayer", "seraph", "seraphRadiation")
                        event.cancelled = true
                    }
                } else {
                    printDevMessage("Slayer not riding, removing guardian", "slayer", "seraph", "seraphRadiation")
                    event.cancelled = true
                }
            }
        }
        if (Skytils.config.ignorePacifiedBlazes && event.entity is BlazeEntity && PotionEffectTimers.potionEffectTimers.containsKey(
                "Smoldering Polarization"
            )
        ) {
            (slayer as? DemonlordSlayer)?.run {
                if (event.entity.squaredDistanceTo(mc.cameraEntity) > 3 * 3 && event.entity != entity) {
                    event.cancelled = true
                }
            }
        }
    }

    fun onRenderHud(event: RenderHUDEvent) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.pointYangGlyph) {
            // FIXME
//            val pos = (slayer as? SeraphSlayer)?.thrownLocation.toVec3()?.add(0.5, 0.5, 0.5)
//                ?: (slayer as? SeraphSlayer)?.thrownEntity?.run { if (this.isAlive) this else null }?.pos
//                ?: return
//            val x = UResolution.scaledWidth / 2.0
//            val y = UResolution.scaledHeight / 2.0
//            val player = mc.player ?: return
//            val angle: Double = -(MathHelper.atan2(
//                pos.x - player.x,
//                pos.z - player.z
//            ) * 57.29577951308232) - player.yaw
//            val matrixStack = UMatrixStack.Compat.get()
//            matrixStack.push()
//            matrixStack.translate(x, y, 0.0)
//            matrixStack.rotate(angle.toFloat(), 0f, 0f, 1f)
//            matrixStack.translate(-x, -y, 0.0)
//            GlStateManager._enableBlend()
//            // disable texture 2d
//            GlStateManager._blendFuncSeparate(770, 771, 1, 0)
//
//            val vertexConsumer = UMinecraft.getMinecraft().bufferBuilders.entityVertexConsumers
//            val tes = Tessellator.getInstance()
//            val wr = vertexConsumer.getBuffer(RenderLayer.getSolid())
//            Skytils.config.yangGlyphColor.withAlpha(255).bindColor()
//            GL11.glLineWidth(5f)
//            wr.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION)
//            wr.vertex(x + 10, y + 45, 0.0).next()
//            wr.vertex(x, y, 0.0).next()
//            wr.vertex(x - 10, y + 45, 0.0).next()
//            tes.draw()
//
//            RenderSystem.method_4397()
//            RenderSystem.disableBlend()
//            RenderSystem.popMatrix()
        }
    }

//    private class RNGMeter(val max: Float, val current: Float, val name: Text) : BossEntity {
//
//        override fun getMaxHealth(): Float = max
//
//        override fun getHealth(): Float = current
//
//        override fun getDisplayName() = name
//
//    }

    // TODO: fix later
//    class SlayerDisplayElement : GuiElement("Slayer Display", x = 150, y = 20) {
//        override fun render() {
//            if (Utils.inSkyblock) {
//                val leftAlign = scaleX < UResolution.scaledWidth / 2f
//                val alignment =
//                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
//                slayer?.run {
//                    timerEntity?.run {
//                        if (isRemoved) {
//                            printDevMessage("timer died", "slayer", "seraph")
//                            timerEntity = null
//                        } else if (toggled) {
//                            ScreenRenderer.fontRenderer.drawString(
//                                displayName.method_10865(),
//                                if (leftAlign) 0f else width,
//                                0f,
//                                CommonColors.WHITE,
//                                alignment,
//                                textShadow
//                            )
//                        }
//                    }
//                    nameEntity?.run {
//                        if (isRemoved) {
//                            printDevMessage("name died", "slayer", "seraph")
//                            nameEntity = null
//                        } else if (toggled) {
//                            ScreenRenderer.fontRenderer.drawString(
//                                displayName.method_10865(),
//                                if (leftAlign) 0f else width,
//                                10f,
//                                CommonColors.WHITE,
//                                alignment,
//                                textShadow
//                            )
//                        }
//                    }
//                    if (entity.isRemoved) {
//                        printDevMessage("slayer died", "slayer", "seraph")
//                        if (Skytils.config.slayerTimeToKill) {
//                            UChat.chat("$prefix §bSlayer took §f${slayerEntity!!.age / 20f}§bs to kill")
//                        }
//                        slayer = null
//                    }
//                }
//            }
//        }
//
//        override fun demoRender() {
//            ScreenRenderer.fontRenderer.drawString(
//                "§c02:59§r",
//                0f,
//                0f,
//                CommonColors.WHITE,
//                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
//                textShadow
//            )
//            ScreenRenderer.fontRenderer.drawString(
//                "§c☠ §bRevenant Horror §a500§c❤§r",
//                0f,
//                10f,
//                CommonColors.WHITE,
//                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
//                textShadow
//            )
//        }
//
//        override val height: Int
//            get() = ScreenRenderer.fontRenderer.field_0_2811 * 2 + 1
//        override val width: Int
//            get() = ScreenRenderer.fontRenderer.getWidth("§c☠ §bRevenant Horror §a500§c❤§r")
//
//        override val toggled: Boolean
//            get() = Skytils.config.showSlayerDisplay
//
//        init {
//            Skytils.guiManager.registerElement(this)
//        }
//    }

//    class SeraphDisplayElement : GuiElement("Seraph Display", x = 20, y = 20) {
//        override fun render() {
//            if (toggled && Utils.inSkyblock && slayerEntity != null && slayerEntity is EndermanEntity) {
//                val leftAlign = scaleX < UResolution.scaledWidth / 2f
//                val alignment =
//                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
//                (slayer as? SeraphSlayer)?.run {
//                    nameEntity?.run {
//                        ScreenRenderer.fontRenderer.drawString(
//                            if (hitPhase)
//                                "§dShield Phase"
//                            else
//                                "§6Damage Phase",
//                            if (leftAlign) 0f else width,
//                            0f,
//                            CommonColors.WHITE,
//                            alignment,
//                            textShadow
//                        )
//                    }
//                    entity.carriedBlock?.takeIf { it.block is BeaconBlock }?.run {
//                        ScreenRenderer.fontRenderer.drawString(
//                            "§cHolding beacon!",
//                            if (leftAlign) 0f else width.toFloat(),
//                            10f,
//                            CommonColors.WHITE,
//                            alignment,
//                            textShadow
//                        )
//                    } ?: if (lastYangGlyphSwitchTicks != -1) {
//                        ScreenRenderer.fontRenderer.drawString(
//                            "§cBeacon thrown! ${(System.currentTimeMillis() - yangGlyphAdrenalineStressCount) / 1000f}s",
//                            if (leftAlign) 0f else width.toFloat(),
//                            10f,
//                            CommonColors.WHITE,
//                            alignment,
//                            textShadow
//                        )
//                    } else {
//                        ScreenRenderer.fontRenderer.drawString(
//                            "§bHolding nothing!",
//                            if (leftAlign) 0f else width.toFloat(),
//                            10f,
//                            CommonColors.WHITE,
//                            alignment,
//                            textShadow
//                        )
//                    }
//                    ScreenRenderer.fontRenderer.drawString(
//                        if (thrownLocation != null)
//                            "§cYang Glyph placed! ${(System.currentTimeMillis() - yangGlyphAdrenalineStressCount) / 1000f}s"
//                        else
//                            "§bNo yang glyph",
//                        if (leftAlign) 0f else width.toFloat(),
//                        20f,
//                        CommonColors.WHITE,
//                        alignment,
//                        textShadow
//                    )
//                    ScreenRenderer.fontRenderer.drawString(
//                        if (nukekebiSkulls.size > 0)
//                            "§dNukekebi Heads: §c${nukekebiSkulls.size}"
//                        else
//                            "§bNo Nukekebi Heads",
//                        if (leftAlign) 0f else width.toFloat(),
//                        30f,
//                        CommonColors.WHITE,
//                        alignment,
//                        textShadow
//                    )
//                }
//            }
//        }
//
//        override fun demoRender() {
//            val leftAlign = scaleX < UResolution.scaledWidth / 2f
//            val alignment =
//                if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
//            ScreenRenderer.fontRenderer.drawString(
//                "§dShield Phase",
//                if (leftAlign) 0f else width.toFloat(),
//                0f,
//                CommonColors.WHITE,
//                alignment,
//                textShadow
//            )
//            ScreenRenderer.fontRenderer.drawString(
//                "§bHolding beacon!",
//                if (leftAlign) 0f else width.toFloat(),
//                10f,
//                CommonColors.WHITE,
//                alignment,
//                textShadow
//            )
//            ScreenRenderer.fontRenderer.drawString(
//                "§cNo yang glyph",
//                if (leftAlign) 0f else width.toFloat(),
//                20f,
//                CommonColors.WHITE,
//                alignment,
//                textShadow
//            )
//        }
//
//        override val height: Int
//            get() = ScreenRenderer.fontRenderer.field_0_2811 + 20
//        override val width: Int
//            get() = ScreenRenderer.fontRenderer.getWidth("§bHolding beacon!")
//
//        override val toggled: Boolean
//            get() = Skytils.config.showSeraphDisplay
//
//        init {
//            Skytils.guiManager.registerElement(this)
//        }
//    }

//    object TotemDisplayElement : GuiElement("Totem Display", x = 20, y = 50) {
//        override fun render() {
//            (slayer as? DemonlordSlayer)?.totemEntity?.run {
//                val leftAlign = scaleX < UResolution.scaledWidth / 2f
//                val alignment =
//                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
//                ScreenRenderer.fontRenderer.drawString(
//                    displayName.method_10865(),
//                    if (leftAlign) 0f else width,
//                    0f,
//                    CommonColors.WHITE,
//                    alignment,
//                    textShadow
//                )
//            }
//        }
//
//        override fun demoRender() {
//            val leftAlign = scaleX < UResolution.scaledWidth / 2f
//            val alignment =
//                if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
//            ScreenRenderer.fontRenderer.drawString(
//                "§6§l5s §c§l5 hits",
//                if (leftAlign) 0f else width.toFloat(),
//                0f,
//                CommonColors.WHITE,
//                alignment,
//                textShadow
//            )
//        }
//
//        override val height: Int
//            get() = ScreenRenderer.fontRenderer.field_0_2811
//        override val width: Int
//            get() = ScreenRenderer.fontRenderer.getWidth("§6§l5s §c§l5 hits")
//
//        override val toggled: Boolean
//            get() = Skytils.config.showTotemDisplay
//
//        init {
//            Skytils.guiManager.registerElement(this)
//        }
//    }

//    class SlayerArmorDisplayElement : GuiElement("Slayer Armor Display", x = 150, y = 20) {
//        private val upgradeBonusRegex =
//            Regex("§7Next Upgrade: §a\\+(?<nextDefense>[\\d,]+?)❈ §8\\(§a(?<kills>[\\d,]+)§7/§c(?<nextKills>[\\d,]+)§8\\)")
//
//        override fun render() {
//            if (Utils.inSkyblock && toggled && mc.player != null) {
//                ScreenRenderer.apply {
//                    val armors = ArrayList<Pair<ItemStack, String>>(4)
//                    (3 downTo 0).map { mc.player.method_0_7157(it) }.forEach { armor ->
//                        if (armor == null) return@forEach
//                        val extraAttr = ItemUtil.getExtraAttributes(armor) ?: return@forEach
//                        val killsKey =
//                            extraAttr.keys.find { it.endsWith("_kills") && extraAttr.getType(it) == ItemUtil.NBT_INTEGER.toByte() }
//                        if (killsKey.isNullOrEmpty()) return@forEach
//                        for (lore in ItemUtil.getItemLore(armor).asReversed()) {
//                            if (lore == "§a§lMAXED OUT! NICE!") {
//                                val kills = extraAttr.getInt(killsKey)
//                                armors.add(armor to "§a§lMAX §b(§f${NumberUtil.nf.format(kills)}§b)")
//                                break
//                            } else if (lore.startsWith("§7Next Upgrade:")) {
//                                val match = upgradeBonusRegex.find(lore) ?: return@forEach
//                                val currentKills =
//                                    match.groups["kills"]!!.value.replace(",", "").toDoubleOrNull() ?: return@forEach
//                                val nextKills = match.groups["nextKills"]!!.value.replace(",", "").toDoubleOrNull()
//                                    ?: return@forEach
//                                val percentToNext = (currentKills / nextKills * 100).roundToPrecision(1)
//                                armors.add(armor to "§e$percentToNext% §b(§f${NumberUtil.nf.format(currentKills)}§b)")
//                            }
//                        }
//                    }
//
//                    if (armors.isNotEmpty()) {
//                        val leftAlign = scaleX < UResolution.scaledWidth / 2f
//                        if (!leftAlign) {
//                            val longest = fontRenderer.getWidth(((armors.maxByOrNull { it.second.length }
//                                ?: (null to ""))).second)
//                            armors.forEachIndexed { index, pair ->
//                                RenderUtil.renderItem(pair.first, longest + 2, index * 16)
//                                fontRenderer.drawString(
//                                    pair.second,
//                                    longest - fontRenderer.getWidth(pair.second).toFloat(),
//                                    index * 16 + 4.5f
//                                )
//                            }
//                        } else {
//                            armors.forEachIndexed { index, pair ->
//                                RenderUtil.renderItem(pair.first, 0, index * 16)
//                                fontRenderer.drawString(
//                                    pair.second,
//                                    18f,
//                                    index * 16 + 4.5f
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        override fun demoRender() {
//            val leftAlign = scaleX < UResolution.scaledWidth / 2f
//            val text = "§e99.9% §b(§f199§b)"
//            if (leftAlign) {
//                RenderUtil.renderItem(ItemStack(Items.field_8577), 0, 0)
//                ScreenRenderer.fontRenderer.drawString(
//                    text,
//                    18f,
//                    4.5f
//                )
//            } else {
//                ScreenRenderer.fontRenderer.drawString(
//                    text,
//                    0f,
//                    4.5f
//                )
//                RenderUtil.renderItem(
//                    ItemStack(Items.field_8577),
//                    ScreenRenderer.fontRenderer.getWidth(text) + 2,
//                    0
//                )
//            }
//        }
//
//        override val height: Int
//            get() = ScreenRenderer.fontRenderer.field_0_2811 + 5
//        override val width: Int
//            get() = 18 + ScreenRenderer.fontRenderer.getWidth("§e99.9% §b(§f199§b)")
//
//        override val toggled: Boolean
//            get() = Skytils.config.showSlayerArmorKills
//
//        init {
//            Skytils.guiManager.registerElement(this)
//        }
//    }

}
