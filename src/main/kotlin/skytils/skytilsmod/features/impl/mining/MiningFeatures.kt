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
package skytils.skytilsmod.features.impl.mining

import net.minecraft.block.BlockCarpet
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.DataFetcher
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.GuiManager.Companion.createTitle
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.BossBarEvent
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.events.RenderBlockInWorldEvent
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.RenderUtil.highlight
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import java.awt.Color
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.min

class MiningFeatures {
    @SubscribeEvent
    fun onBossBar(event: BossBarEvent.Set) {
        if (!Utils.inSkyblock) return
        val unformatted = event.displayData.displayName.unformattedText.stripControlCodes()
        if (Skytils.config.raffleWarning) {
            if (unformatted.contains("EVENT")) {
                val matcher = EVENT_PATTERN.matcher(unformatted)
                if (matcher.find()) {
                    val ev = matcher.group("event")
                    val seconds = matcher.group("min").toInt() * 60 + matcher.group("sec").toInt()
                    if (ev == "RAFFLE") {
                        if (seconds <= 15 && GuiManager.title != "§cRaffle ending in §a" + seconds + "s") {
                            createTitle("§cRaffle ending in §a" + seconds + "s", 20)
                        }
                        if (seconds > 1) {
                            inRaffle = true
                        }
                    }
                }
            }
        }
    }

    private val xyzPattern: Pattern =
        Pattern.compile(".*(?<user>[a-zA-Z0-9_]{3,16}):.* (?<x>[0-9]{1,3}) (?<y>[0-9]{1,3}) (?<z>[0-9]{1,3}).*")
    private val xzPattern: Pattern =
        Pattern.compile(".*(?<user>[a-zA-Z0-9_]{3,16}):.* (?<x>[0-9]{1,3}) (?<z>[0-9]{1,3}).*")

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type.toInt() == 2) return
        val formatted = event.message.formattedText
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (Skytils.config.powerGhastPing) {
            if (unformatted.startsWith("Find the Powder Ghast near the")) {
                createTitle("§cPOWDER GHAST", 20)
            }
        }
        if (Skytils.config.raffleWaypoint && inRaffle) {
            if ((formatted.startsWith("§r§eYou registered §r§a") && formatted.endsWith("§r§ein the raffle event!§r")) || formatted == "§r§7No tickets to put in the box...§r") {
                raffleBox = lastJukebox
            }
            if (unformatted.trim { it <= ' ' }.startsWith("RAFFLE ENDED!")) {
                inRaffle = false
            }
        }
        if (Skytils.config.puzzlerSolver && unformatted.startsWith("[NPC] Puzzler:")) {
            if (unformatted.contains("Nice")) {
                puzzlerSolution = null
                return
            }
            if (unformatted.contains("Wrong") || unformatted.contains("Come") || !unformatted.contains("▶") && !unformatted.contains(
                    "▲"
                ) && !unformatted.contains("◀") && !unformatted.contains("▼")
            ) return
            if (SBInfo.mode == SBInfo.SkyblockIsland.DwarvenMines.mode) {
                puzzlerSolution = BlockPos(181, 195, 135)
                val msg = unformatted.substring(15).trim { it <= ' ' }
                val matcher = Pattern.compile("([▶▲◀▼]+)").matcher(unformatted)
                if (matcher.find()) {
                    val sequence = matcher.group(1).trim { it <= ' ' }
                    if (sequence.length != msg.length) {
                        println(String.format("%s - %s | %s - %s", sequence, msg, sequence.length, unformatted.length))
                    }
                    for (c in sequence.toCharArray()) {
                        when (c.toString()) {
                            "▲" -> puzzlerSolution = puzzlerSolution!!.south()
                            "▶" -> puzzlerSolution = puzzlerSolution!!.west()
                            "◀" -> puzzlerSolution = puzzlerSolution!!.east()
                            "▼" -> puzzlerSolution = puzzlerSolution!!.north()
                            else -> println("Invalid Puzzler character: $c")
                        }
                    }
                    println("Puzzler Solution: $puzzlerSolution")
                    mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.GREEN.toString() + "Mine the block highlighted in " + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "RED" + EnumChatFormatting.GREEN + "!"))
                }
            }
        }
        if (Skytils.config.fetchurSolver && unformatted.startsWith("[NPC] Fetchur:")) {
            if (fetchurItems.size == 0) {
                mc.thePlayer.addChatMessage(ChatComponentText("§cSkytils did not load any solutions."))
                DataFetcher.reloadData()
                return
            }
            val solution = fetchurItems.getOrDefault(fetchurItems.keys.find { s: String ->
                unformatted.contains(
                    s
                )
            }, null)
            TickTask(50) {
                if (solution != null) {
                    mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.GREEN.toString() + "Fetchur needs: " + EnumChatFormatting.DARK_GREEN + EnumChatFormatting.BOLD + solution + EnumChatFormatting.GREEN + "!"))
                } else {
                    if (unformatted.contains("its") || unformatted.contains("theyre")) {
                        println("Missing Fetchur item: $unformatted")
                        mc.thePlayer.addChatMessage(
                            ChatComponentText(
                                String.format(
                                    "§cSkytils couldn't determine the Fetchur item. There were %s solutions loaded.",
                                    fetchurItems.size
                                )
                            )
                        )
                    }
                }
            }
        }
        if (Skytils.config.hollowChatCoords && SBInfo.mode == SBInfo.SkyblockIsland.CrystalHollows.mode) {
            val xyzMatcher: Matcher = xyzPattern.matcher(unformatted)
            val xzMatcher: Matcher = xzPattern.matcher(unformatted)
            if (xyzMatcher.matches())
                waypointChatMessage(xyzMatcher.group("x"), xyzMatcher.group("y"), xyzMatcher.group("z"))
            else if (xzMatcher.matches())
                waypointChatMessage(xzMatcher.group("x"), "100", xzMatcher.group("z"))
        }
    }

    fun waypointChatMessage(x: String, y: String, z: String) {
        if (cityLoc.exists() && templeLoc.exists() && minesLoc.exists() && denLoc.exists() && balLoc.exists()) return
        val component = ChatComponentText(
            EnumChatFormatting.DARK_AQUA.toString() + "Skytils > " + EnumChatFormatting.YELLOW +
                    "Found coordinates in a chat message, click a button to set a waypoint.\n"
        )
        val city = ChatComponentText(EnumChatFormatting.WHITE.toString() + "[Lost Precursor City] ").setChatStyle(
            ChatStyle()
                .setChatClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/\$setwaypoint city $x $y $z"
                    )
                ).setChatHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText(EnumChatFormatting.YELLOW.toString() + "set waypoint for Lost Precursor City")
                    )
                )
        )
        val temple = ChatComponentText(EnumChatFormatting.GREEN.toString() + "[Jungle Temple] ").setChatStyle(
            ChatStyle()
                .setChatClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/\$setwaypoint temple $x $y $z"
                    )
                ).setChatHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText(EnumChatFormatting.YELLOW.toString() + "set waypoint for Jungle Temple")
                    )
                )
        )
        val den = ChatComponentText(EnumChatFormatting.YELLOW.toString() + "[Goblin Queen's Den] ").setChatStyle(
            ChatStyle()
                .setChatClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/\$setwaypoint den $x $y $z"
                    )
                ).setChatHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText(EnumChatFormatting.YELLOW.toString() + "set waypoint for Goblin Queen's Den")
                    )
                )
        )
        val mines = ChatComponentText(EnumChatFormatting.BLUE.toString() + "[Mines of Divan] ").setChatStyle(
            ChatStyle()
                .setChatClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/\$setwaypoint mines $x $y $z"
                    )
                ).setChatHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText(EnumChatFormatting.YELLOW.toString() + "set waypoint for Mines of Divan")
                    )
                )
        )
        val bal = ChatComponentText(EnumChatFormatting.RED.toString() + "[Khazad-dûm]").setChatStyle(
            ChatStyle()
                .setChatClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/\$setwaypoint bal $x $y $z"
                    )
                ).setChatHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText(EnumChatFormatting.YELLOW.toString() + "set waypoint for Khazad-dûm")
                    )
                )
        )
        if (!cityLoc.exists())
            component.appendSibling(city)
        if (!templeLoc.exists())
            component.appendSibling(temple)
        if (!denLoc.exists())
            component.appendSibling(den)
        if (!minesLoc.exists())
            component.appendSibling(mines)
        if (!balLoc.exists())
            component.appendSibling(bal)
        mc.thePlayer.addChatMessage(component)
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inSkyblock || event.container !is ContainerChest) return
        if (event.slot.hasStack && SBInfo.lastOpenContainerName.equals("Commissions") && Skytils.config.highlightCompletedComissions) {
            val item = event.slot.stack
            if (item.displayName.startsWith("§6Commission #") && item.item == Items.writable_book) {
                if (ItemUtil.getItemLore(item).any {
                        it == "§7§eClick to claim rewards!"
                    }) {
                    event.slot highlight Color(255, 0, 0)
                }
            }
        }
    }

    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!Utils.inSkyblock) return
        if (event.entity !== mc.thePlayer) return
        val item = event.entityPlayer.heldItem
        val itemId = ItemUtil.getSkyBlockItemID(item)
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            if (SBInfo.mode?.startsWith("dynamic") == true) {
                if (Skytils.config.noPickaxeAbilityOnPrivateIsland && itemId != null && (itemId.contains("PICKAXE") || itemId.contains(
                        "DRILL"
                    ))
                ) {
                    event.isCanceled =
                        event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.pos == null || mc.theWorld.getBlockState(
                            event.pos
                        ).block !== Blocks.chest && mc.theWorld.getBlockState(event.pos).block !== Blocks.trapped_chest
                }
            }
            if (Skytils.config.raffleWaypoint && inRaffle && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                val block = event.world.getBlockState(event.pos)
                if (block.block === Blocks.jukebox) {
                    lastJukebox = event.pos
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)

        if (Skytils.config.puzzlerSolver && puzzlerSolution != null) {
            val x = puzzlerSolution!!.x - viewerX
            val y = puzzlerSolution!!.y - viewerY
            val z = puzzlerSolution!!.z - viewerZ
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(AxisAlignedBB(x, y, z, x + 1, y + 1.01, z + 1), Color(255, 0, 0, 200), 1f)
            GlStateManager.enableCull()
        }
        if (Skytils.config.raffleWaypoint && inRaffle && raffleBox != null) {
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
            RenderUtil.renderWaypointText("Raffle Box", raffleBox!!, event.partialTicks)
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
        if (Skytils.config.hollowWaypoints && SBInfo.mode == SBInfo.SkyblockIsland.CrystalHollows.mode) {
            cityLoc.drawWayPoint("Lost Precursor City", event.partialTicks)
            templeLoc.drawWayPoint("Jungle Temple", event.partialTicks)
            denLoc.drawWayPoint("Goblin Queen's Den", event.partialTicks)
            minesLoc.drawWayPoint("Mines of Divan", event.partialTicks)
            balLoc.drawWayPoint("Khazad-dûm", event.partialTicks)
        }
    }

    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<EntityLivingBase?>) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityCreeper && event.entity.isInvisible) {
            val entity = event.entity as EntityCreeper
            if (Skytils.config.showGhosts && event.entity.maxHealth == 1024f && entity.powered) {
                event.entity.isInvisible = false
            }
        }
    }

    @SubscribeEvent
    fun onRenderLivingPost(event: RenderLivingEvent.Specials.Pre<EntityLivingBase?>) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.showGhostHealth && event.entity is EntityCreeper && event.entity.maxHealth == 1024f) {
            val entity = event.entity as EntityCreeper
            if (entity.powered) {
                val healthText = "§cGhost §a" + NumberUtil.format(event.entity.health.toLong()) + "§f/§a1M§c ❤"
                RenderUtil.draw3DString(
                    Vec3(
                        event.entity.posX,
                        event.entity.posY + event.entity.eyeHeight + 0.5,
                        event.entity.posZ
                    ), healthText, Color(255, 255, 255), 1f
                )
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inSkyblock || event.phase != TickEvent.Phase.START) return
        if (Skytils.config.skymallReminder && SBInfo.mode == SBInfo.SkyblockIsland.DwarvenMines.mode && SBInfo.time == "12:00am" && GuiManager.title != "§cSKYMALL RESET"
        ) {
            createTitle("§cSKYMALL RESET", 20)
        }
        if ((Skytils.config.hollowWaypoints || Skytils.config.hollowMap) && SBInfo.mode == SBInfo.SkyblockIsland.CrystalHollows.mode) {
            val scoreboard = ScoreboardUtil.sidebarLines
            for (element in scoreboard) {
                if (element.startsWith(" §7⏣ ")) {
                    val clean = ScoreboardUtil.cleanSB(element)
                    when {
                        clean.contains("Lost Precursor City") -> cityLoc.set()
                        clean.contains("Jungle Temple") -> templeLoc.set()
                        clean.contains("Goblin Queen's Den") -> denLoc.set()
                        clean.contains("Mines of Divan") -> minesLoc.set()
                        clean.contains("Khazad-dm") -> balLoc.set()
                    }
                    break
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        puzzlerSolution = null
        lastJukebox = null
        raffleBox = null
        inRaffle = false
        cityLoc.reset()
        templeLoc.reset()
        denLoc.reset()
        minesLoc.reset()
        balLoc.reset()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGetBlockModel(event: RenderBlockInWorldEvent) {
        if (!Utils.inSkyblock || SBInfo.mode != SBInfo.SkyblockIsland.DwarvenMines.mode || event.state == null) return
        val state = event.state!!
        if (Skytils.config.recolorCarpets && state.block === Blocks.carpet && Utils.equalsOneOf(
                state.getValue(
                    BlockCarpet.COLOR
                ), EnumDyeColor.GRAY, EnumDyeColor.LIGHT_BLUE, EnumDyeColor.YELLOW
            )
        ) {
            event.state = state.withProperty(BlockCarpet.COLOR, EnumDyeColor.RED)
        }
    }

    class CrystalHollowsMap : GuiElement(name = "Crystal Hollows Map", fp = FloatPair(0, 0), scale = 0.1f) {
        override fun render() {
            if (!toggled || mc.thePlayer == null) return
            RenderUtil.renderTexture(ResourceLocation("skytils", "crystalhollowsmap.png"), 0, 0, 1000, 1000)
            cityLoc.drawRect(Color.WHITE.rgb)
            templeLoc.drawRect(Color.GREEN.rgb)
            denLoc.drawRect(Color.YELLOW.rgb)
            minesLoc.drawRect(Color.BLUE.rgb)
            balLoc.drawRect(Color.RED.rgb)
            val x = mc.thePlayer.posX.coerceIn(0.0, 1000.0)
            val y = mc.thePlayer.posZ.coerceIn(0.0, 1000.0)
            RenderUtil.drawRect(x - 10, y - 10, x + 10, y + 10, Color.RED.rgb)
        }

        override fun demoRender() {
            Gui.drawRect(0, 0, 1000, 1000, Color.RED.rgb)
            fr.drawString("Crystal Hollows Map", 500f, 500f, alignment = SmartFontRenderer.TextAlignment.MIDDLE)
        }

        override val toggled: Boolean
            get() = Skytils.config.crystalHollowsMap && SBInfo.mode == SBInfo.SkyblockIsland.CrystalHollows.mode
        override val height: Int
            get() = 1000
        override val width: Int
            get() = 1000

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    init {
        CrystalHollowsMap()
    }

    class LocationObject {
        var locX: Double? = null
        var locY: Double? = null
        var locZ: Double? = null
        private var locMinX: Double = 1100.0
        private var locMinY: Double = 1100.0
        private var locMinZ: Double = 1100.0
        private var locMaxX: Double = -100.0
        private var locMaxY: Double = -100.0
        private var locMaxZ: Double = -100.0

        fun reset() {
            locX = null
            locY = null
            locZ = null
            locMinX = 1100.0
            locMinY = 1100.0
            locMinZ = 1100.0
            locMaxX = -100.0
            locMaxY = -100.0
            locMaxZ = -100.0
        }

        fun set() {
            locMinX = mc.thePlayer.posX.coerceIn(0.0, 1000.0).coerceAtMost(locMinX)
            locMinY = mc.thePlayer.posY.coerceIn(0.0, 1000.0).coerceAtMost(locMinY)
            locMinZ = mc.thePlayer.posZ.coerceIn(0.0, 1000.0).coerceAtMost(locMinZ)
            locMaxX = mc.thePlayer.posX.coerceIn(0.0, 1000.0).coerceAtLeast(locMaxX)
            locMaxY = mc.thePlayer.posY.coerceIn(0.0, 1000.0).coerceAtLeast(locMaxY)
            locMaxZ = mc.thePlayer.posZ.coerceIn(0.0, 1000.0).coerceAtLeast(locMaxZ)
            locX = (locMinX + locMaxX) / 2
            locY = (locMinY + locMaxY) / 2
            locZ = (locMinZ + locMaxZ) / 2
        }

        fun exists(): Boolean {
            return locX != null && locY != null && locZ != null
        }

        fun drawWayPoint(text: String, partialTicks: Float) {
            if (exists())
                RenderUtil.renderWaypointText(text, locX!!, locY!!, locZ!!, partialTicks)
        }

        fun drawRect(color: Int) {
            if (exists())
                RenderUtil.drawRect(locX!! - 50, locZ!! - 50, locX!! + 50, locZ!! + 50, color)
        }
    }

    companion object {
        var fetchurItems = LinkedHashMap<String, String>()
        private val mc = Minecraft.getMinecraft()
        private val EVENT_PATTERN =
            Pattern.compile("(?:PASSIVE )?EVENT (?<event>.+) (?:ACTIVE IN (?<location>.+)|RUNNING) (FOR|for) (?<min>\\d+):(?<sec>\\d+)")
        private var lastJukebox: BlockPos? = null
        private var puzzlerSolution: BlockPos? = null
        private var raffleBox: BlockPos? = null
        private var inRaffle = false
        var cityLoc: LocationObject = LocationObject()
        var templeLoc: LocationObject = LocationObject()
        var denLoc: LocationObject = LocationObject()
        var minesLoc: LocationObject = LocationObject()
        var balLoc: LocationObject = LocationObject()
    }
}