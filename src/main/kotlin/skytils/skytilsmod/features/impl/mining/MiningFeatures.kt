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
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityCreeper
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
import skytils.skytilsmod.events.BossBarEvent
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.events.RenderBlockInWorldEvent
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.RenderUtil.highlight
import skytils.skytilsmod.utils.stripControlCodes
import java.awt.Color
import java.util.regex.Pattern

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
            Thread {
                try {
                    Thread.sleep(2500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
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
            }.start()
        }
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
            GlStateManager.enableCull()
            RenderUtil.drawFilledBoundingBox(AxisAlignedBB(x, y, z, x + 1, y + 1.01, z + 1), Color(255, 0, 0, 200), 1f)
            GlStateManager.disableCull()
        }
        if (Skytils.config.raffleWaypoint && inRaffle && raffleBox != null) {
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
            RenderUtil.renderWaypointText("Raffle Box", raffleBox!!, event.partialTicks)
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
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
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        puzzlerSolution = null
        lastJukebox = null
        raffleBox = null
        inRaffle = false
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGetBlockModel(event: RenderBlockInWorldEvent) {
        if (!Utils.inSkyblock || SBInfo.mode != SBInfo.SkyblockIsland.DwarvenMines.mode || event.state == null) return
        val state = event.state!!
        if (Skytils.config.recolorCarpets && state.block === Blocks.carpet && Utils.equalsOneOf(
                state.getValue(
                    BlockCarpet.COLOR
                ), EnumDyeColor.GRAY, EnumDyeColor.LIGHT_BLUE
            )
        ) {
            event.state = state.withProperty(BlockCarpet.COLOR, EnumDyeColor.RED)
        }
    }

    companion object {
        var fetchurItems = LinkedHashMap<String, String>()
        private val mc = Minecraft.getMinecraft()
        private val EVENT_PATTERN =
            Pattern.compile("(?:PASSIVE )?EVENT (?<event>.+) (?:(?:ACTIVE IN (?<location>.+))|(?:RUNNING)) (FOR|for) (?<min>\\d+):(?<sec>\\d+)")
        private var lastJukebox: BlockPos? = null
        private var puzzlerSolution: BlockPos? = null
        private var raffleBox: BlockPos? = null
        private var inRaffle = false
    }
}