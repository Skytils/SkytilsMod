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
package skytils.skytilsmod.features.impl.dungeons

import net.minecraft.block.BlockStainedGlass
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.BossStatus
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S45PacketTitle
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
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.BossBarEvent
import skytils.skytilsmod.events.GuiContainerEvent.SlotClickEvent
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.events.SendChatMessageEvent
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
        private val WATCHER_MOBS = arrayOf(
            "Revoker",
            "Psycho",
            "Reaper",
            "Cannibal",
            "Mute",
            "Ooze",
            "Putrid",
            "Freak",
            "Leech",
            "Tear",
            "Parasite",
            "Flamer",
            "Skull",
            "Mr. Dead",
            "Vader",
            "Frost",
            "Walker",
            "Wandering Soul",
            "Bonzo",
            "Scarf",
            "Livid"
        )
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

        init {
            LividGuiElement()
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return
        if (Utils.inDungeons) {
            if (dungeonFloor == null) {
                for (s in ScoreboardUtil.sidebarLines) {
                    val line = ScoreboardUtil.cleanSB(s)
                    if (line.contains("The Catacombs (")) {
                        dungeonFloor = line.substringAfter("(").substringBefore(")")
                        break
                    }
                }
            }
            if (terracottaEndTime > 0 && Skytils.config.showSadanInterest) {
                val timeLeft = terracottaEndTime - System.currentTimeMillis()
                    .toDouble() / 1000f
                if (timeLeft >= 0) {
                    BossStatus.healthScale = timeLeft.toFloat() / 105
                    BossStatus.statusBarTime = 100
                    BossStatus.bossName = "§r§c§lSadan's Interest: §r§6" + timeLeft.toInt() + "s"
                    BossStatus.hasColorModifier = false
                } else {
                    terracottaEndTime = -2.0
                }
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
                                    val a = when (state.getValue(BlockStainedGlass.COLOR).name.lowercase()) {
                                        "white" -> EnumChatFormatting.WHITE
                                        "pink" -> EnumChatFormatting.LIGHT_PURPLE
                                        "red" -> EnumChatFormatting.RED
                                        "silver" -> EnumChatFormatting.GRAY
                                        "gray" -> EnumChatFormatting.GRAY
                                        "green" -> EnumChatFormatting.DARK_GREEN
                                        "lime" -> EnumChatFormatting.GREEN
                                        "blue" -> EnumChatFormatting.BLUE
                                        "purple" -> EnumChatFormatting.DARK_PURPLE
                                        "yellow" -> EnumChatFormatting.YELLOW
                                        else -> null
                                    } ?: return@submit
                                    for (entity in mc.theWorld.loadedEntityList) {
                                        if (entity.name.startsWith("$a﴾ $a§lLivid")) {
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
                    terracottaEndTime = System.currentTimeMillis().toDouble() / 1000f + 105
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
                if (deathFailMatcher.find()) {
                    if (!unformatted.contains("disconnect")) {
                        GuiScreen.setClipboardString(unformatted)
                        mc.thePlayer.addChatMessage(ChatComponentText("§aCopied death/fail to clipboard."))
                    }
                    event.message.chatStyle
                        .setChatHoverEvent(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ChatComponentText("§aClick to copy to clipboard.")
                            )
                        ).chatClickEvent =
                        ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skytilscopyfail $unformatted")
                }
            }
            if (Skytils.config.hideF4Spam && unformatted.startsWith("[CROWD]") && thornMissMessages.none { unformatted.contains(it, true) }
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
        if (event.message.startsWith("/skytilscopyfail") && !event.addToChat) {
            mc.thePlayer.addChatMessage(ChatComponentText("§aCopied selected death/fail to clipboard."))
            GuiScreen.setClipboardString(event.message.substring("/skytilscopyfail ".length))
            event.isCanceled = true
        }
    }

    // Show hidden fels
    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<*>) {
        if (Utils.inDungeons) {
            if (event.entity.isInvisible) {
                if (Skytils.config.showHiddenFels && event.entity is EntityEnderman) {
                    event.entity.isInvisible = false
                }
                if (Skytils.config.showHiddenShadowAssassins && event.entity is EntityPlayer && event.entity.name.contains(
                        "Shadow Assassin"
                    )
                ) {
                    event.entity.isInvisible = false
                }
                if (Skytils.config.showStealthyBloodMobs && event.entity is EntityPlayer) {
                    for (name in WATCHER_MOBS) {
                        if (event.entity.name.trim { it <= ' ' } == name) {
                            event.entity.isInvisible = false
                            break
                        }
                    }
                }
            }
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
            if (event.entity is EntityBat && Skytils.config.showBatHitboxes && !hasBossSpawned && Utils.equalsOneOf(
                    event.entity.maxHealth,
                    100f,
                    400f
                ) && !mc.renderManager.isDebugBoundingBox && !event.entity.isInvisible
            ) {
                RenderUtil.drawOutlinedBoundingBox(
                    event.entity.entityBoundingBox,
                    Color(0, 255, 255, 255),
                    3f,
                    RenderUtil.getPartialTicks()
                )
            }
            if (event.entity is EntitySkeleton && Skytils.config.boxSkeletonMasters && !mc.renderManager.isDebugBoundingBox && !event.entity.isInvisible && ItemUtil.getSkyBlockItemID(
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
            val chestName = chest.lowerChestInventory.displayName.unformattedText
            if (chestName.endsWith(" Chest")) {
                if (Skytils.config.kismetRerollConfirm > 0 && event.slotId == 50) {
                    rerollClicks++
                    val neededClicks = Skytils.config.kismetRerollConfirm - rerollClicks
                    if (neededClicks > 0) {
                        event.isCanceled = true
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
    }

    internal class LividGuiElement : GuiElement("Livid HP", FloatPair(0.05f, 0.4f)) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (toggled && Utils.inDungeons && player != null && world != null) {
                if (lividTag == null) return
                val sr = ScaledResolution(Minecraft.getMinecraft())
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
            val sr = ScaledResolution(Minecraft.getMinecraft())
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