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
package gg.skytils.skytilsmod.features.impl.mining

import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.core.DataFetcher
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.GuiManager.createTitle
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.events.impl.BossBarEvent
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.regex.Pattern

object MiningFeatures {

    var fetchurItems = linkedMapOf<String, String>()

    private val eventPattern =
        Regex("(?:PASSIVE )?EVENT (?<event>.+) (?:ACTIVE IN (?<location>.+)|RUNNING) (FOR|for) (?<min>\\d+):(?<sec>\\d+)")
    private var lastJukebox: BlockPos? = null
    private var puzzlerSolution: BlockPos? = null
    private var raffleBox: BlockPos? = null
    private var inRaffle = false

    @SubscribeEvent
    fun onBossBar(event: BossBarEvent.Set) {
        if (!Utils.inSkyblock) return
        val unformatted = event.displayData.displayName.unformattedText.stripControlCodes()
        if (Skytils.config.raffleWarning) {
            if (unformatted.contains("EVENT")) {
                eventPattern.find(unformatted)?.groups?.let {
                    val ev = it["event"]!!.value
                    val seconds = it["min"]!!.value.toInt() * 60 + it["sec"]!!.value.toInt()
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
        if (!Utils.inSkyblock || event.type == 2.toByte()) return
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
            if (unformatted.trim().startsWith("RAFFLE ENDED!")) {
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
            if (SBInfo.mode == SkyblockIsland.DwarvenMines.mode) {
                puzzlerSolution = BlockPos(181, 195, 135)
                val msg = unformatted.substring(15).trim()
                val matcher = Pattern.compile("([▶▲◀▼]+)").matcher(unformatted)
                if (matcher.find()) {
                    val sequence = matcher.group(1).trim()
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
                    UChat.chat("$successPrefix §aMine the block highlighted in §c§lRED§a!")
                }
            }
        }
        if (Skytils.config.fetchurSolver && unformatted.startsWith("[NPC] Fetchur:")) {
            if (fetchurItems.size == 0) {
                UChat.chat("$failPrefix §cSkytils did not load any solutions.")
                DataFetcher.reloadData()
                return
            }
            val solution = fetchurItems.getOrDefault(fetchurItems.keys.find { s: String ->
                unformatted.contains(
                    s
                )
            }, null)
            tickTimer(50) {
                if (solution != null) {
                    UChat.chat("$successPrefix §aFetchur needs: §2${solution}§a!")
                } else {
                    if (unformatted.contains("its") || unformatted.contains("theyre")) {
                        println("Missing Fetchur item: $unformatted")
                        UChat.chat("$failPrefix §cSkytils couldn't determine the Fetchur item. There were ${fetchurItems.size} solutions loaded.")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inSkyblock || event.container !is ContainerChest) return
        if (!event.slot.hasStack) return
        if (Skytils.config.highlightDisabledHOTMPerks && SBInfo.lastOpenContainerName == "Heart of the Mountain") {
            if (ItemUtil.getItemLore(event.slot.stack).any { it == "§c§lDISABLED" }) {
                event.slot highlight Color(255, 0, 0)
            }
        }
        if (Skytils.config.highlightCompletedCommissions && SBInfo.lastOpenContainerName.equals("Commissions")) {
            val item = event.slot.stack
            if (item.displayName.startsWith("§6Commission #") && item.item == Items.writable_book) {
                if (ItemUtil.getItemLore(item).any {
                        it == "§eClick to claim rewards!"
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
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
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
        val matrixStack = UMatrixStack()

        if (Skytils.config.puzzlerSolver && puzzlerSolution != null) {
            val x = puzzlerSolution!!.x - viewerX
            val y = puzzlerSolution!!.y - viewerY
            val z = puzzlerSolution!!.z - viewerZ
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                AxisAlignedBB(x, y, z, x + 1, y + 1.01, z + 1),
                Color(255, 0, 0, 200),
                1f
            )
            GlStateManager.enableCull()
        }
        if (Skytils.config.raffleWaypoint && inRaffle && raffleBox != null) {
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
            RenderUtil.renderWaypointText("Raffle Box", raffleBox!!, event.partialTicks, matrixStack)
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onScoreboardChange(event: PacketEvent.ReceiveEvent) {
        if (
            !Utils.inSkyblock ||
            event.packet !is S3EPacketTeams
        ) return
        if (event.packet.action != 2) return
        if (
            event.packet.players.joinToString(
                " ",
                prefix = event.packet.prefix,
                postfix = event.packet.suffix
            ).contains("12:00am") &&
            Skytils.config.skymallReminder && SBInfo.mode == SkyblockIsland.DwarvenMines.mode
        ) {
            val message = UMessage("§cSkymall reset ")
            message.append(UTextComponent("§b[HOTM]").setClick(MCClickEventAction.RUN_COMMAND, "/hotm"))
            message.chat()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        puzzlerSolution = null
        lastJukebox = null
        raffleBox = null
        inRaffle = false
    }
}
