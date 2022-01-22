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

import gg.essential.api.EssentialAPI
import gg.essential.universal.UChat
import gg.essential.universal.UResolution
import net.minecraft.block.BlockStainedGlass
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.BossStatus
import net.minecraft.entity.item.EntityArmorStand
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
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.impl.BossBarEvent
import skytils.skytilsmod.events.impl.GuiContainerEvent.SlotClickEvent
import skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import skytils.skytilsmod.events.impl.SendChatMessageEvent
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.mixins.transformers.accessors.AccessorEnumDyeColor
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.awt.Color
import java.util.concurrent.Future
import java.util.regex.Pattern

class DungeonFeatures {
    companion object {
        private val mc = Minecraft.getMinecraft()
        private val deathOrPuzzleFail =
            Pattern.compile("^ ☠ .+ and became a ghost\\.$|^PUZZLE FAIL! .+$|^\\[STATUE] Oruo the Omniscient: .+ chose the wrong answer!")
        private val thornMissMessages = arrayOf(
            "chickens",
            "shot",
            "dodg", "thumbs",
            "aim"
        )
        var dungeonFloor: String? = null
        var hasBossSpawned = false
        private var isInTerracottaPhase = false
        private var terracottaEndTime = -1.0
        private var rerollClicks = 0
        private var foundLivid = false
        private var livid: Entity? = null
        private var lividTag: Entity? = null
        private var lividJob: Future<*>? = null
        private var alertedSpiritPet = false
        private const val SPIRIT_PET_TEXTURE =
            "ewogICJ0aW1lc3RhbXAiIDogMTU5NTg2MjAyNjE5OSwKICAicHJvZmlsZUlkIiA6ICI0ZWQ4MjMzNzFhMmU0YmI3YTVlYWJmY2ZmZGE4NDk1NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJGaXJlYnlyZDg4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhkOWNjYzY3MDY3N2QwY2ViYWFkNDA1OGQ2YWFmOWFjZmFiMDlhYmVhNWQ4NjM3OWEwNTk5MDJmMmZlMjI2NTUiCiAgICB9CiAgfQp9"
        private var lastLitUpTime = -1L
        private val lastBlockPos = BlockPos(207, 77, 234)
        private var startWithoutFullParty = false

        init {
            LividGuiElement()
            SpiritBearSpawnTimer()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onReceivePacketHighest(event: ReceiveEvent) {
        event.apply {
            if (hasBossSpawned && Skytils.config.spiritBearTimer && dungeonFloor?.endsWith('4') == true) {
                when (packet) {
                    is S23PacketBlockChange -> {
                        if (packet.blockPosition == lastBlockPos) {
                            val time = System.currentTimeMillis()
                            lastLitUpTime = if (packet.blockState.block === Blocks.sea_lantern) time else -1L
                            printDevMessage("light $lastLitUpTime", "spiritbear")
                        }
                    }
                    is S02PacketChat -> {
                        if (lastLitUpTime != -1L && packet.chatComponent.formattedText == "§r§a§lA §r§5§lSpirit Bear §r§a§lhas appeared!§r") {
                            printDevMessage("chat ${System.currentTimeMillis() - lastLitUpTime}")
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
                for (line in ScoreboardUtil.sidebarLines) {
                    if (line.contains("The Catacombs (")) {
                        dungeonFloor = line.substringAfter("(").substringBefore(")")
                        ScoreCalculation.floorReq.set(
                            ScoreCalculation.floorRequirements[dungeonFloor]
                                ?: ScoreCalculation.floorRequirements["default"]!!
                        )
                        break
                    }
                }
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
                    "Someone in your party has a Spirit Pet equipped!"
                )
                GuiManager.createTitle("Spirit Pet", 20)
                alertedSpiritPet = true
            }

            if (Skytils.config.findCorrectLivid && !foundLivid) {
                if (Utils.equalsOneOf(dungeonFloor, "F5", "M5")) {
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
                            if (hasBossSpawned && mc.thePlayer.isPotionActive(Potion.blindness) && (lividJob == null || lividJob?.isCancelled == true || lividJob?.isDone == true)) {
                                lividJob = Skytils.threadPool.submit {
                                    while (mc.thePlayer.isPotionActive(Potion.blindness)) {
                                        Thread.sleep(10)
                                    }
                                    val state = mc.theWorld.getBlockState(BlockPos(205, 109, 242))
                                    val color = state.getValue(BlockStainedGlass.COLOR)
                                    color as AccessorEnumDyeColor
                                    val a = when (color) {
                                        EnumDyeColor.WHITE -> EnumChatFormatting.WHITE
                                        EnumDyeColor.MAGENTA -> EnumChatFormatting.LIGHT_PURPLE
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
                                    val otherColor = color.chatColor
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
        if (dungeonFloor == "F7") {
            if (unformatted.contains("Necron")) {
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
                        BossStatus.bossName = displayData.displayName.formattedText + "§r§8 - §r§a" + NumberUtil.format(
                            (BossStatus.healthScale * 1000000000).toLong()
                        ) + "§r§8/§r§a1B§r§c❤"
                        BossStatus.hasColorModifier = event.hasColorModifier
                        event.isCanceled = true
                    }
                    0 -> {
                    }
                }
            }
            return
        }
        if (Utils.equalsOneOf(dungeonFloor, "F6", "M6")) {
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
        if (!Utils.inSkyblock) return
        if (event.entityLiving is EntityOtherPlayerMP && terracottaEndTime > 0 && event.entityLiving.name == "Terracotta ") {
            //for some reason this event fires twice for players
            printDevMessage("terracotta died", "terracotta")
            terracottaEndTime -= 1
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (Utils.inDungeons) {
            if (Skytils.config.autoCopyFailToClipboard) {
                val deathFailMatcher = deathOrPuzzleFail.matcher(unformatted)
                if (deathFailMatcher.find() || (unformatted.startsWith("[CROWD]") && thornMissMessages.any {
                        unformatted.contains(
                            it,
                            true
                        )
                    } && DungeonListener.team.any { unformatted.contains(it.playerName) })) {
                    if (!unformatted.contains("disconnect")) {
                        GuiScreen.setClipboardString(unformatted)
                        UChat.chat("§9§lSkytils §8» §aCopied fail to clipboard.")
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
                    if (unformatted.contains("So you made it all the way here")) isInTerracottaPhase = true
                    if (unformatted.contains("ENOUGH!") || unformatted.contains("It was inevitable.")) isInTerracottaPhase =
                        false
                }
            }
        }
    }

    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        if (event.message.startsWith("/skytilscopy") && !event.addToChat) {
            UChat.chat("§9§lSkytils §8» §aCopied to clipboard.")
            GuiScreen.setClipboardString(event.message.substring("/skytilscopy ".length))
            event.isCanceled = true
        }
    }

    // Show hidden fels
    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<*>) {
        if (Utils.inDungeons) {
            if (Skytils.config.boxSpiritBow && Utils.equalsOneOf(
                    dungeonFloor,
                    "F4",
                    "M4"
                ) && hasBossSpawned && event.entity is EntityArmorStand && event.entity.isInvisible && event.entity.heldItem?.item == Items.bow
            ) {
                GlStateManager.disableCull()
                GlStateManager.disableDepth()
                val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(1f)
                val x = event.entity.posX - viewerX
                val y = event.entity.posY - viewerY
                val z = event.entity.posZ - viewerZ
                RenderUtil.drawFilledBoundingBox(
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
                    if (!name.startsWith("✯ ") && name.contains("❤")) if (name.contains("Lurker") || name.contains("Dreadlord") || name.contains(
                            "Souleater"
                        ) || name.contains("Zombie") || name.contains("Skeleton") || name.contains("Skeletor") || name.contains(
                            "Sniper"
                        ) || name.contains("Super Archer") || name.contains("Spider") || name.contains("Fels") || name.contains(
                            "Withermancer"
                        )
                    ) mc.theWorld.removeEntity(event.entity)
                }
            }
            if (!mc.renderManager.isDebugBoundingBox && !event.entity.isInvisible) {
                if (event.entity is EntityBat && Skytils.config.showBatHitboxes && !hasBossSpawned &&
                    if (MayorInfo.currentMayor == "Derpy") Utils.equalsOneOf(
                        event.entity.maxHealth,
                        200f,
                        800f
                    ) else Utils.equalsOneOf(
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
                }
                if (event.entity is EntitySkeleton && Skytils.config.boxSkeletonMasters && ItemUtil.getSkyBlockItemID(
                        event.entity.getCurrentArmor(0)
                    ) == "SKELETON_MASTER_BOOTS"
                ) {
                    RenderUtil.drawOutlinedBoundingBox(
                        event.entity.entityBoundingBox,
                        Color(255, 107, 11, 255),
                        3f,
                        RenderUtil.getPartialTicks()
                    )
                }
                if (hasBossSpawned && Skytils.config.boxSpiritBears && event.entity.name == "Spirit Bear" && event.entity is EntityOtherPlayerMP) {
                    RenderUtil.drawOutlinedBoundingBox(
                        event.entity.entityBoundingBox,
                        Color(121, 11, 255, 255),
                        3f,
                        RenderUtil.getPartialTicks()
                    )
                }
            }
            if (event.entity == lividTag) {
                val aabb = AxisAlignedBB(
                    event.entity.posX - 0.5,
                    event.entity.posY - 2,
                    event.entity.posZ - 0.5,
                    event.entity.posX + 0.5,
                    event.entity.posY,
                    event.entity.posZ + 0.5
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
    fun onRenderPlayerPre(event: RenderPlayerEvent.Pre) {
        if (livid != null && event.entityPlayer != livid && event.entityPlayer.uniqueID.version() != 4) {
            GlStateManager.enableBlend()
            GlStateManager.color(1f, 1f, 1f, 0.3f)
        }
    }

    @SubscribeEvent
    fun onRenderPlayerPost(event: RenderPlayerEvent.Post) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableBlend()
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
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
            val chest = event.container
            val chestName = chest.lowerChestInventory.displayName.unformattedText ?: return
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
                chestName == "Start Dungeon?" -> {
                    if (!startWithoutFullParty && Skytils.config.noChildLeftBehind && event.slot?.stack?.displayName == "§aStart Dungeon?") {
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
    }

    class SpiritBearSpawnTimer : GuiElement("Spirit Bear Spawn Timer", FloatPair(0.05f, 0.4f)) {
        override fun render() {
            if (toggled && lastLitUpTime != -1L) {
                val time = lastLitUpTime + 3400
                val diff = time - System.currentTimeMillis()
                if (diff < 0) {
                    lastLitUpTime = -1
                }
                val sr = UResolution
                val leftAlign = actualX < sr.scaledWidth / 2f
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    "Spirit Bear ${diff / 1000f}s",
                    if (leftAlign) 0f else width.toFloat(),
                    0f,
                    CommonColors.PURPLE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val sr = UResolution
            val leftAlign = actualX < sr.scaledWidth / 2f
            val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Spirit Bear: 3.4s",
                if (leftAlign) 0f else 0f + width,
                0f,
                CommonColors.PURPLE,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
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

    internal class LividGuiElement : GuiElement("Livid HP", FloatPair(0.05f, 0.4f)) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (toggled && Utils.inDungeons && player != null && world != null) {
                if (lividTag == null) return
                val sr = UResolution
                val leftAlign = actualX < sr.scaledWidth / 2f
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    lividTag!!.name.replace("§l", ""),
                    if (leftAlign) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val sr = UResolution
            val leftAlign = actualX < sr.scaledWidth / 2f
            val text = "§r§f﴾ Livid §e6.9M§c❤ §f﴿"
            val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                text,
                if (leftAlign) 0f else 0f + width,
                0f,
                CommonColors.WHITE,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
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
}