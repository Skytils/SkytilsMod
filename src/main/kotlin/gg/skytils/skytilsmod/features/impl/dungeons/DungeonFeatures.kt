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
package gg.skytils.skytilsmod.features.impl.dungeons

import gg.essential.api.EssentialAPI
import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.*
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent.SlotClickEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEnumDyeColor
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.Utils.equalsOneOf
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.block.BlockStainedGlass
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.BossStatus
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.entity.passive.EntityBat
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemSkull
import net.minecraft.network.play.server.*
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.world.World
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object DungeonFeatures {
    private val deathOrPuzzleFail =
        Regex("^ ☠ .+ and became a ghost\\.$|^PUZZLE FAIL! .+$|^\\[STATUE] Oruo the Omniscient: .+ chose the wrong answer!")
    private val thornMissMessages = arrayOf(
        "chickens",
        "shot",
        "dodg", "thumbs",
        "aim"
    )
    var dungeonFloor: String? = null
        set(value) {
            field = value
            dungeonFloorNumber = value?.drop(1)?.ifEmpty { "0" }?.toIntOrNull()
        }
    var dungeonFloorNumber: Int? = null
        private set
    var hasBossSpawned = false
    private var isInTerracottaPhase = false
    private var terracottaEndTime = -1.0
    private var rerollClicks = 0
    private var foundLivid = false
    var livid: Entity? = null
    private var lividTag: Entity? = null
    private var lividJob: Job? = null
    private var alertedSpiritPet = false
    private const val SPIRIT_PET_TEXTURE =
        "ewogICJ0aW1lc3RhbXAiIDogMTU5NTg2MjAyNjE5OSwKICAicHJvZmlsZUlkIiA6ICI0ZWQ4MjMzNzFhMmU0YmI3YTVlYWJmY2ZmZGE4NDk1NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJGaXJlYnlyZDg4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhkOWNjYzY3MDY3N2QwY2ViYWFkNDA1OGQ2YWFmOWFjZmFiMDlhYmVhNWQ4NjM3OWEwNTk5MDJmMmZlMjI2NTUiCiAgICB9CiAgfQp9"
    private var lastLitUpTime = -1L
    private val lastBlockPos = BlockPos(7, 77, 34)
    private var startWithoutFullParty = false
    private var blazes = 0
    var hasClearedText = false
    private var terracottaSpawns = hashMapOf<BlockPos, Long>()
    private val dungeonMobSpawns = setOf(
        "Lurker",
        "Dreadlord",
        "Souleater",
        "Zombie",
        "Skeleton",
        "Skeletor",
        "Sniper",
        "Super Archer",
        "Spider",
        "Fels",
        "Withermancer"
    )

    init {
        DungeonSecretDisplay
        LividGuiElement()
        SpiritBearSpawnTimer()
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (hasBossSpawned && Skytils.config.spiritBearTimer && dungeonFloor?.endsWith('4') == true) {
            if (event.pos == lastBlockPos) {
                lastLitUpTime =
                    if (event.update.block === Blocks.sea_lantern && event.old.block === Blocks.coal_block) System.currentTimeMillis() else -1L
                printDevMessage("change light $lastLitUpTime", "spiritbear")
            }
        } else if (isInTerracottaPhase && Skytils.config.terracottaRespawnTimer && dungeonFloor?.endsWith('6') == true) {
            if (event.old.block == Blocks.air && event.update.block == Blocks.flower_pot) {
                // TODO: verify M6 time
                terracottaSpawns[event.pos] = System.currentTimeMillis() + if (dungeonFloor == "F6") 15000 else 12000
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onReceivePacketHighest(event: ReceiveEvent) {
        event.apply {
            if (hasBossSpawned && Skytils.config.spiritBearTimer && dungeonFloor?.endsWith('4') == true) {
                when (packet) {
                    is S23PacketBlockChange -> {
                        if (packet.blockPosition == lastBlockPos) {
                            lastLitUpTime =
                                if (packet.blockState.block === Blocks.sea_lantern) System.currentTimeMillis() else -1L
                            printDevMessage("light $lastLitUpTime", "spiritbear")
                        }
                    }

                    is S02PacketChat -> {
                        if (lastLitUpTime != -1L && packet.chatComponent.formattedText == "§r§a§lA §r§5§lSpirit Bear §r§a§lhas appeared!§r") {
                            printDevMessage("chat ${System.currentTimeMillis() - lastLitUpTime}", "spiritbear")
                            lastLitUpTime = -1L
                        }
                    }

                    is S0CPacketSpawnPlayer -> {
                        if (lastLitUpTime != -1L && packet.player.version() == 2) {
                            printDevMessage("spawn ${System.currentTimeMillis() - lastLitUpTime}", "spiritbear")
                            //lastLitUpTime = -1L
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return
        if (Utils.inDungeons) {
            if (dungeonFloor == null) {
                ScoreboardUtil.sidebarLines.find {
                    it.contains("The Catacombs (")
                }?.let {
                    dungeonFloor = it.substringAfter("(").substringBefore(")")
                    ScoreCalculation.floorReq.set(
                        ScoreCalculation.floorRequirements[dungeonFloor]
                            ?: ScoreCalculation.floorRequirements["default"]!!
                    )
                }
            }
            if (!hasClearedText) {
                hasClearedText = ScoreboardUtil.sidebarLines.any { it.startsWith("Cleared: ") }
            }
            if (isInTerracottaPhase && terracottaEndTime > 0 && Skytils.config.showSadanInterest) {
                val timeLeft = terracottaEndTime - System.currentTimeMillis()
                    .toDouble() / 1000f
                if (timeLeft >= 0) {
                    BossStatus.healthScale = timeLeft.toFloat() / if (dungeonFloor == "F6") 105 else 115
                    BossStatus.statusBarTime = 100
                    BossStatus.bossName = "§r§c§lSadan's Interest: §r§6" + timeLeft.toInt() + "s"
                    BossStatus.hasColorModifier = false
                } else {
                    terracottaEndTime = -2.0
                }
            }

            if (Skytils.config.spiritPetWarning && !alertedSpiritPet && DungeonTimer.dungeonStartTime == -1L && mc.theWorld.loadedEntityList.any {
                    if (it !is EntityArmorStand || it.hasCustomName()) return@any false
                    val item = it.heldItem ?: return@any false
                    if (item.item !is ItemSkull) return@any false
                    return@any ItemUtil.getSkullTexture(item) == SPIRIT_PET_TEXTURE
                }) {
                UChat.chat(
                    "$prefix §cSomeone in your party has a Spirit Pet equipped!"
                )
                GuiManager.createTitle("Spirit Pet", 20)
                alertedSpiritPet = true
            }

            if (Skytils.config.findCorrectLivid && !foundLivid) {
                if (equalsOneOf(dungeonFloor, "F5", "M5")) {
                    when (Skytils.config.lividFinderType) {
                        1 -> {
                            val loadedLivids = mc.theWorld.loadedEntityList.filter {
                                val name = it.name
                                name.contains("Livid") && name.length > 5 && name[1] == name[5]
                            }
                            if (loadedLivids.size > 8) {
                                lividTag = loadedLivids[0]
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
                            }
                        }

                        0 -> {
                            if (hasBossSpawned && mc.thePlayer.isPotionActive(Potion.blindness) && (lividJob == null || lividJob?.isCancelled == true || lividJob?.isCompleted == true)) {
                                lividJob = Skytils.launch {
                                    while (mc.thePlayer.isPotionActive(Potion.blindness)) {
                                        delay(1)
                                    }
                                    val state = mc.theWorld.getBlockState(BlockPos(6, 109, 43))
                                    val color = state.getValue(BlockStainedGlass.COLOR)
                                    val a = when (color) {
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
                                    for (entity in mc.theWorld.loadedEntityList) {
                                        if (entity !is EntityArmorStand) continue
                                        val fallBackColor = entity.name.startsWith("$otherColor﴾ $otherColor§lLivid")
                                        if ((a != null && entity.name.startsWith("$a﴾ $a§lLivid")) || fallBackColor) {
                                            if (fallBackColor && !(a != null && entity.name.startsWith("$a﴾ $a§lLivid"))) {
                                                UChat.chat("§bBlock color ${color.name} should be mapped to ${otherColor}${otherColor.name}§b. Please report this to discord.gg/skytils")
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
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onBossBarSet(event: BossBarEvent.Set) {
        if (!Utils.inDungeons) return
        val displayData = event.displayData
        val unformatted = event.displayData.displayName.unformattedText.stripControlCodes()
        if (equalsOneOf(dungeonFloor, "F7", "M7")) {
            if (equalsOneOf(unformatted, "Maxor", "Storm", "Goldor", "Necron")) {
                when (Skytils.config.necronHealth) {
                    2 -> {
                        BossStatus.healthScale = displayData.health / displayData.maxHealth
                        BossStatus.statusBarTime = 100
                        BossStatus.bossName = displayData.displayName.formattedText + "§r§8 - §r§d" + String.format(
                            "%.1f",
                            BossStatus.healthScale * 100
                        ) + "%"
                        BossStatus.hasColorModifier = event.hasColorModifier
                        event.isCanceled = true
                    }

                    1 -> {
                        BossStatus.healthScale = displayData.health / displayData.maxHealth
                        BossStatus.statusBarTime = 100
                        val isMaster = dungeonFloor == "M7"
                        val health = when (unformatted) {
                            "Maxor" -> if (isMaster) 800_000_000 else 100_000_000
                            "Storm" -> if (isMaster) 1_000_000_000 else 400_000_000
                            "Goldor" -> if (isMaster) 1_200_000_000 else 750_000_000
                            "Necron" -> if (isMaster) 1_400_000_000 else 1_000_000_000
                            else -> 69
                        }
                        BossStatus.bossName = displayData.displayName.formattedText + "§r§8 - §r§a" + NumberUtil.format(
                            (BossStatus.healthScale * health).toLong()
                        ) + "§r§8/§r§a${NumberUtil.format(health)}§r§c❤"
                        BossStatus.hasColorModifier = event.hasColorModifier
                        event.isCanceled = true
                    }

                    0 -> {
                    }
                }
            }
            return
        }
        if (equalsOneOf(dungeonFloor, "F6", "M6")) {
            if (terracottaEndTime == -1.0) {
                if (unformatted.contains("Sadan's Interest Level")) {
                    val length = if (dungeonFloor == "F6") 105 else 115
                    terracottaEndTime = System.currentTimeMillis().toDouble() / 1000f + length
                }
            } else if (terracottaEndTime > 0 && Skytils.config.showSadanInterest) {
                event.isCanceled = true
            }
            return
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onDeath(event: LivingDeathEvent) {
        if (!Utils.inDungeons) return
        if (event.entityLiving is EntityOtherPlayerMP && terracottaEndTime > 0 && event.entityLiving.name == "Terracotta ") {
            //for some reason this event fires twice for players
            printDevMessage("terracotta died", "terracotta")
            terracottaEndTime -= 1
        }
        if (event.entity is EntityBlaze && ++blazes == 10 && Skytils.config.sayBlazeDone) {
            Skytils.sendMessageQueue.add("/pc Blaze Done")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (Utils.inDungeons) {
            if (Skytils.config.autoCopyFailToClipboard) {
                if (deathOrPuzzleFail.containsMatchIn(unformatted) || (unformatted.startsWith("[CROWD]") && thornMissMessages.any {
                        unformatted.contains(
                            it,
                            true
                        )
                    } && DungeonListener.team.keys.any { unformatted.contains(it) })) {
                    if (!unformatted.contains("disconnect")) {
                        GuiScreen.setClipboardString(unformatted)
                        UChat.chat("$prefix §aCopied fail to clipboard.")
                    }
                    event.message.chatStyle
                        .setChatHoverEvent(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ChatComponentText("§aClick to copy to clipboard.")
                            )
                        ).chatClickEvent =
                        ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skytilscopy $unformatted")
                }
            }
            if (Skytils.config.hideF4Spam && unformatted.startsWith("[CROWD]") && thornMissMessages.none {
                    unformatted.contains(
                        it,
                        true
                    )
                }
            ) {
                event.isCanceled = true
            }
            if (unformatted.startsWith("[BOSS]") && unformatted.contains(":")) {
                val bossName = unformatted.substringAfter("[BOSS] ").substringBefore(":").trim()
                if (!hasBossSpawned && bossName != "The Watcher" && dungeonFloor != null && Utils.checkBossName(
                        dungeonFloor!!,
                        bossName
                    )
                ) {
                    hasBossSpawned = true
                }
                if (bossName == "Sadan") {
                    if (unformatted.contains("So you made it all the way here")) {
                        isInTerracottaPhase = true
                    } else if (unformatted.contains("ENOUGH!") || unformatted.contains("It was inevitable.")) {
                        isInTerracottaPhase = false
                        terracottaSpawns.clear()
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        if (event.message.startsWith("/skytilscopy") && !event.addToChat) {
            UChat.chat("$prefix §aCopied to clipboard.")
            GuiScreen.setClipboardString(event.message.substring("/skytilscopy ".length))
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!Utils.inDungeons) return
        if (Skytils.config.hideArcherBonePassive && event.entity is EntityItem && event.entity.entityItem.itemDamage == 15 && event.entity.entityItem.item === Items.dye)
            event.isCanceled = true
    }

    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<*>) {
        if (Utils.inDungeons) {
            val matrixStack = UMatrixStack()
            if (Skytils.config.boxSpiritBow && hasBossSpawned && event.entity.isInvisible && equalsOneOf(
                    dungeonFloor,
                    "F4",
                    "M4"
                ) && event.entity is EntityArmorStand && event.entity.heldItem?.item == Items.bow
            ) {
                GlStateManager.disableCull()
                GlStateManager.disableDepth()
                val (vx, vy, vz) = RenderUtil.getViewerPos(RenderUtil.getPartialTicks())
                val (x, y, z) = RenderUtil.fixRenderPos(event.x - vx, event.y - vy, event.z - vz)
                RenderUtil.drawFilledBoundingBox(
                    matrixStack,
                    AxisAlignedBB(x, y, z, x + 0.75, y + 1.975, z + 0.75),
                    Color(255, 0, 255, 200),
                    1f
                )
                GlStateManager.enableDepth()
                GlStateManager.enableCull()
            }
            if (event.entity is EntityArmorStand && event.entity.hasCustomName()) {
                if (Skytils.config.hideWitherMinerNametags) {
                    val name = event.entity.customNameTag.stripControlCodes()
                    if (name.contains("Wither Miner") || name.contains("Wither Guard") || name.contains("Apostle")) {
                        mc.theWorld.removeEntity(event.entity)
                    }
                }
                if (Skytils.config.hideF4Nametags) {
                    val name = event.entity.customNameTag.stripControlCodes()
                    if (name.contains("Spirit") && !name.contains("Spirit Bear")) {
                        mc.theWorld.removeEntity(event.entity)
                    }
                }
                if (Skytils.config.hideTerracotaNametags) {
                    val name = event.entity.customNameTag.stripControlCodes()
                    if (name.contains("Terracotta ")) mc.theWorld.removeEntity(event.entity)
                }
                if (Skytils.config.hideNonStarredNametags) {
                    val name = event.entity.customNameTag.stripControlCodes()
                    if (!name.startsWith("✯ ") && name.contains("❤") && dungeonMobSpawns.any { it in name }) {
                        mc.theWorld.removeEntity(event.entity)
                    }
                }
                if (Skytils.config.hideFairies && event.entity.heldItem != null && ItemUtil.getSkullTexture(event.entity.heldItem) == "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjM2UzMWNmYzY2NzMzMjc1YzQyZmNmYjVkOWE0NDM0MmQ2NDNiNTVjZDE0YzljNzdkMjczYTIzNTIifX19") {
                    event.isCanceled = true
                }
            }
            if (!mc.renderManager.isDebugBoundingBox) {
                if (!event.entity.isInvisible) {
                    if (event.entity is EntityBat && Skytils.config.showBatHitboxes && !hasBossSpawned &&
                        if (MayorInfo.currentMayor == "Derpy") equalsOneOf(
                            event.entity.maxHealth,
                            200f,
                            800f
                        ) else equalsOneOf(
                            event.entity.maxHealth,
                            100f,
                            400f
                        )
                    ) {
                        RenderUtil.drawOutlinedBoundingBox(
                            event.entity.entityBoundingBox,
                            Color(0, 255, 255, 255),
                            3f,
                            RenderUtil.getPartialTicks()
                        )
                    } else if (event.entity is EntitySkeleton && Skytils.config.boxSkeletonMasters && ItemUtil.getSkyBlockItemID(
                            event.entity.getCurrentArmor(0)
                        ) == "SKELETON_MASTER_BOOTS"
                    ) {
                        RenderUtil.drawOutlinedBoundingBox(
                            event.entity.entityBoundingBox,
                            Color(255, 107, 11, 255),
                            3f,
                            RenderUtil.getPartialTicks()
                        )
                    } else if (hasBossSpawned && Skytils.config.boxSpiritBears && event.entity.name == "Spirit Bear" && event.entity is EntityOtherPlayerMP) {
                        val (x, y, z) = RenderUtil.fixRenderPos(event.x, event.y, event.z)
                        val aabb = AxisAlignedBB(
                            x - 0.5,
                            y,
                            z - 0.5,
                            x + 0.5,
                            y + 2,
                            z + 0.5
                        )
                        RenderUtil.drawOutlinedBoundingBox(
                            aabb,
                            Color(121, 11, 255, 255),
                            3f,
                            RenderUtil.getPartialTicks()
                        )
                    }
                } else {
                    if (!hasBossSpawned && Skytils.config.boxStarredMobs && event.entity is EntityArmorStand && event.entity.hasCustomName() && event.entity.alwaysRenderNameTag) {
                        val name = event.entity.name
                        if (name.startsWith("§6✯ ") && name.endsWith("§c❤")) {
                            val (x, y, z) = RenderUtil.fixRenderPos(event.x, event.y, event.z)
                            val color = Skytils.config.boxStarredMobsColor
                            if ("Spider" in name) {
                                RenderUtil.drawOutlinedBoundingBox(
                                    AxisAlignedBB(
                                        x - 0.625,
                                        y - 1,
                                        z - 0.625,
                                        x + 0.625,
                                        y - 0.25,
                                        z + 0.625
                                    ),
                                    color,
                                    3f,
                                    RenderUtil.getPartialTicks()
                                )
                            } else if ("Fels" in name || "Withermancer" in name) {
                                RenderUtil.drawOutlinedBoundingBox(
                                    AxisAlignedBB(x - 0.5, y - 3, z - 0.5, x + 0.5, y, z + 0.5),
                                    color,
                                    3f,
                                    RenderUtil.getPartialTicks()
                                )
                            } else {
                                RenderUtil.drawOutlinedBoundingBox(
                                    AxisAlignedBB(x - 0.5, y - 2, z - 0.5, x + 0.5, y, z + 0.5),
                                    color,
                                    3f,
                                    RenderUtil.getPartialTicks()
                                )
                            }
                        }
                    }
                }
            }
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
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        val stack = UMatrixStack()
        GlStateManager.disableCull()
        GlStateManager.disableDepth()
        terracottaSpawns.entries.removeAll {
            val diff = it.value - System.currentTimeMillis()
            RenderUtil.drawLabel(
                it.key.middleVec(),
                "${"%.2f".format(diff / 1000.0)}s",
                Color.WHITE,
                event.partialTicks,
                stack
            )
            return@removeAll diff < 0
        }
        GlStateManager.enableCull()
        GlStateManager.enableDepth()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onReceivePacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inSkyblock) return
        if (event.packet is S45PacketTitle) {
            val packet = event.packet
            if (packet.message != null && mc.thePlayer != null) {
                val unformatted = packet.message.unformattedText.stripControlCodes()
                if (Skytils.config.hideTerminalCompletionTitles && Utils.inDungeons && !unformatted.contains(mc.thePlayer.name) && (unformatted.contains(
                        "activated a terminal!"
                    ) || unformatted.contains("completed a device!") || unformatted.contains("activated a lever!"))
                ) {
                    event.isCanceled = true
                    runCatching {
                        val slash = unformatted.indexOf("/")
                        val numBeforeSlash = unformatted[slash - 1].digitToInt()
                        val numAfterSlash = unformatted[slash + 1].digitToInt()
                        if (numBeforeSlash == 0 || numBeforeSlash == numAfterSlash) {
                            event.isCanceled = false
                        }
                    }
                }
            }
        }
        if (event.packet is S29PacketSoundEffect) {
            val packet = event.packet
            if (Skytils.config.disableTerracottaSounds && isInTerracottaPhase) {
                val sound = packet.soundName
                val pitch = packet.pitch
                val volume = packet.volume
                if (sound == "game.player.hurt" && pitch == 0f && volume == 0f) event.isCanceled = true
                if (sound == "random.eat" && pitch == 0.6984127f && volume == 1f) event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        rerollClicks = 0
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: SlotClickEvent) {
        if (!Utils.inDungeons) return
        if (event.container is ContainerChest) {
            val chestName = event.chestName
            when {
                chestName.endsWith(" Chest") -> {
                    if (Skytils.config.kismetRerollConfirm > 0 && event.slotId == 50) {
                        rerollClicks++
                        val neededClicks = Skytils.config.kismetRerollConfirm - rerollClicks
                        if (neededClicks > 0) {
                            event.isCanceled = true
                        }
                    }
                }

                chestName == "Ready Up" -> {
                    if (!startWithoutFullParty && Skytils.config.noChildLeftBehind) {
                        val teamCount =
                            (DungeonListener.partyCountPattern.find(TabListUtils.tabEntries[0].second)?.groupValues?.get(
                                1
                            )?.toIntOrNull() ?: 0)
                        if (teamCount < 5) {
                            event.isCanceled = true
                            EssentialAPI.getNotifications()
                                .push(
                                    "Party only has $teamCount members!",
                                    "Click me to disable this warning.",
                                    4f,
                                    action = {
                                        startWithoutFullParty = true
                                    })
                        }
                    }
                }
            }

        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        if (event.itemStack != null) {
            if (Utils.inDungeons && Skytils.config.kismetRerollConfirm > 0 && ItemUtil.getDisplayName(event.itemStack)
                    .contains("Reroll") && SBInfo.lastOpenContainerName?.endsWith(" Chest") == true
            ) {
                for (i in event.toolTip.indices) {
                    if (event.toolTip[i].contains("Click to reroll")) {
                        val neededClicks = Skytils.config.kismetRerollConfirm - rerollClicks
                        event.toolTip[i] = "§eClick §a$neededClicks§e times to reroll this chest!"
                        break
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        dungeonFloor = null
        hasBossSpawned = false
        isInTerracottaPhase = false
        terracottaEndTime = -1.0
        lividTag = null
        livid = null
        foundLivid = false
        alertedSpiritPet = false
        lastLitUpTime = -1L
        startWithoutFullParty = false
        blazes = 0
        hasClearedText = false
        terracottaSpawns.clear()
    }

    class SpiritBearSpawnTimer : GuiElement("Spirit Bear Spawn Timer", x = 0.05f, y = 0.4f) {
        override fun render() {
            if (toggled && lastLitUpTime != -1L) {
                val time = lastLitUpTime + 3400
                val diff = time - System.currentTimeMillis()
                if (diff < 0) {
                    lastLitUpTime = -1
                }

                val leftAlign = scaleX < sr.scaledWidth / 2f
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    "Spirit Bear ${diff / 1000f}s",
                    if (leftAlign) 0f else width.toFloat(),
                    0f,
                    CommonColors.PURPLE,
                    alignment,
                    textShadow
                )
            }
        }

        override fun demoRender() {

            val leftAlign = scaleX < sr.scaledWidth / 2f
            val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Spirit Bear: 3.4s",
                if (leftAlign) 0f else 0f + width,
                0f,
                CommonColors.PURPLE,
                alignment,
                textShadow
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Spirit Bear: 3.4s")

        override val toggled: Boolean
            get() = Skytils.config.spiritBearTimer

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    internal class LividGuiElement : GuiElement("Livid HP", x = 0.05f, y = 0.4f) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (toggled && Utils.inDungeons && player != null && world != null) {
                if (lividTag == null) return

                val leftAlign = scaleX < sr.scaledWidth / 2f
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
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
            val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
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

    object DungeonSecretDisplay : GuiElement("Dungeon Secret Display", x = 0.05f, y = 0.4f) {
        var secrets = -1
        var maxSecrets = -1

        override fun render() {
            if (toggled && Utils.inDungeons && maxSecrets > 0) {
                val leftAlign = scaleX < sr.scaledWidth / 2f
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                val color = when (secrets / maxSecrets.toDouble()) {
                    in 0.0..0.5 -> CommonColors.RED
                    in 0.5..0.75 -> CommonColors.YELLOW
                    else -> CommonColors.GREEN
                }

                ScreenRenderer.fontRenderer.drawString(
                    "Secrets: ${secrets}/${maxSecrets}",
                    if (leftAlign) 0f else 0f + width,
                    0f,
                    color,
                    alignment,
                    textShadow
                )
            }
        }

        override fun demoRender() {
            val leftAlign = scaleX < sr.scaledWidth / 2f
            val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Secrets: 0/0",
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
            get() = ScreenRenderer.fontRenderer.getStringWidth("Secrets: 0/0")

        override val toggled: Boolean
            get() = Skytils.config.dungeonSecretDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}
