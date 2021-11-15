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

import gg.essential.universal.UResolution
import net.minecraft.block.BlockDoor
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockSign
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.projectile.EntityFishHook
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.events.impl.GuiContainerEvent.SlotClickEvent
import skytils.skytilsmod.events.impl.GuiRenderItemEvent
import skytils.skytilsmod.events.impl.MainReceivePacketEvent
import skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.ItemUtil.getDisplayName
import skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import skytils.skytilsmod.utils.ItemUtil.getItemLore
import skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import skytils.skytilsmod.utils.RenderUtil.highlight
import skytils.skytilsmod.utils.RenderUtil.renderRarity
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.awt.Color
import java.util.regex.Pattern
import kotlin.math.pow

class ItemFeatures {

    companion object {
        private val candyPattern = Pattern.compile("§a\\((\\d+)/10\\) Pet Candy Used")
        val sellPrices = HashMap<String, Double>()
        val bitCosts = HashMap<String, Int>()
        val hotbarRarityCache = arrayOfNulls<ItemRarity>(9)
        var selectedArrow = ""
        var soulflowAmount = ""
        var stackingEnchantDisplayText = ""
        var lowSoulFlowPinged = false

        init {
            SelectedArrowDisplay()
            StackingEnchantDisplay()
            SoulflowGuiElement()
        }

        val interactables = setOf(
            Blocks.acacia_door,
            Blocks.anvil,
            Blocks.beacon,
            Blocks.bed,
            Blocks.birch_door,
            Blocks.brewing_stand,
            Blocks.command_block,
            Blocks.crafting_table,
            Blocks.chest,
            Blocks.dark_oak_door,
            Blocks.daylight_detector,
            Blocks.daylight_detector_inverted,
            Blocks.dispenser,
            Blocks.dropper,
            Blocks.enchanting_table,
            Blocks.ender_chest,
            Blocks.furnace,
            Blocks.hopper,
            Blocks.jungle_door,
            Blocks.lever,
            Blocks.noteblock,
            Blocks.powered_comparator,
            Blocks.unpowered_comparator,
            Blocks.powered_repeater,
            Blocks.unpowered_repeater,
            Blocks.standing_sign,
            Blocks.wall_sign,
            Blocks.trapdoor,
            Blocks.trapped_chest,
            Blocks.wooden_button,
            Blocks.stone_button,
            Blocks.oak_door,
            Blocks.skull
        )

        var ticks = 0
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (ticks % 4 == 0) {
            if (mc.thePlayer != null && Utils.inSkyblock) {
                val held = mc.thePlayer.inventory.getCurrentItem()
                if (Skytils.config.showItemRarity) {
                    for (i in 0..8) {
                        hotbarRarityCache[i] = ItemUtil.getRarity(mc.thePlayer.inventory.mainInventory[i])
                    }
                }
                if (Skytils.config.stackingEnchantProgressDisplay) {
                    apply {
                        also {
                            val extraAttr = getExtraAttributes(held) ?: return@also
                            val enchantments = extraAttr.getCompoundTag("enchantments")
                            val stacking =
                                EnchantUtil.enchants.find { it is StackingEnchant && extraAttr.hasKey(it.nbtNum) } as? StackingEnchant
                                    ?: return@also

                            val stackingLevel = enchantments.getInteger(stacking.nbtName)
                            val stackingAmount = extraAttr.getInteger(stacking.nbtNum)

                            stackingEnchantDisplayText = buildString {
                                append("§b${stacking.loreName} §e$stackingLevel §f")
                                val nextLevel = stacking.stackLevel.getOrNull(stackingLevel)
                                if (stackingLevel == stacking.maxLevel || nextLevel == null) {
                                    append("(§e${stackingAmount}§f)")
                                } else {
                                    append("(§c${stackingAmount} §f/ §a${NumberUtil.format(nextLevel)}§f)")
                                }
                            }
                            return@apply
                        }
                        stackingEnchantDisplayText = ""
                    }
                }
            }
            ticks = 0
        }
        ticks++
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (Utils.inSkyblock && Skytils.config.showItemRarity && event.slot.hasStack) {
            renderRarity(event.slot.stack, event.slot.xDisplayPosition, event.slot.yDisplayPosition)
        }
        if (event.gui is GuiChest) {
            val gui = event.gui
            val chest = gui.inventorySlots as ContainerChest
            val inv = chest.lowerChestInventory
            val chestName = inv.displayName.unformattedText.trim()
            if (chestName.startsWithAny("Salvage", "Ender Chest") || Utils.equalsOneOf(
                    chestName,
                    "Ophelia",
                    "Trades"
                ) || (chestName.contains("Backpack") && !chestName.endsWith("Recipe"))
            ) {
                if (Skytils.config.highlightSalvageableItems) {
                    if (event.slot.hasStack) {
                        val stack = event.slot.stack
                        val extraAttr = getExtraAttributes(stack)
                        val sbId = getSkyBlockItemID(stack)
                        if (sbId != "ICE_SPRAY_WAND" && extraAttr != null && extraAttr.hasKey("baseStatBoostPercentage") && !extraAttr.hasKey(
                                "dungeon_item_level"
                            )
                        ) {
                            GlStateManager.translate(0f, 0f, 1f)
                            event.slot highlight Color(15, 233, 233)
                            GlStateManager.translate(0f, 0f, -1f)
                        }
                    }
                }
            }
            if (chestName == "Ophelia" || chestName == "Trades") {
                if (Skytils.config.highlightDungeonSellableItems) {
                    if (event.slot.hasStack) {
                        val stack = event.slot.stack
                        if (stack.displayName.contains("Health Potion")) event.slot highlight Color(255, 225, 30)
                        else if (stack.displayName.containsAny(
                                "Defuse Kit", "Lever", "Torch",
                                "Stone Button", "Tripwire Hook", "Journal Entry",
                                "Training Weights", "Mimic Fragment"
                            )
                        ) event.slot highlight Color(255, 50, 150, 255) else {
                            val itemId = AuctionData.getIdentifier(stack) ?: ""
                            if ((itemId.startsWith("ENCHANTED_BOOK-") && itemId.containsAny(
                                    "FEATHER_FALLING",
                                    "BANK",
                                    "NO_PAIN_NO_GAIN",
                                    "INFINITE_QUIVER"
                                )) || itemId == "ENCHANTED_BOOK"
                            ) event.slot highlight Color(50, 50, 255)
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!Utils.inSkyblock) return
        if (event.container is ContainerChest) {
            val chest = event.container
            val inv = chest.lowerChestInventory
            val chestName = inv.displayName.unformattedText.trim()
            if (event.slot != null && event.slot.hasStack) {
                val item: ItemStack = event.slot.stack ?: return
                val extraAttr = getExtraAttributes(item)
                if (Skytils.config.stopClickingNonSalvageable) {
                    if (chestName.startsWith("Salvage") && extraAttr != null) {
                        if (!extraAttr.hasKey("baseStatBoostPercentage") && !item.displayName.contains("Salvage") && !item.displayName.contains(
                                "Essence"
                            )
                        ) {
                            event.isCanceled = true
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inSkyblock) return
        val item = event.itemStack
        val extraAttr = getExtraAttributes(item)
        var itemId = getSkyBlockItemID(extraAttr)
        var isSuperpairsReward = false
        if (item != null && mc.thePlayer.openContainer != null && SBInfo.lastOpenContainerName?.startsWith(
                "Superpairs ("
            ) == true
        ) {
            if (getDisplayName(item).stripControlCodes() == "Enchanted Book") {
                val lore = getItemLore(item)
                if (lore.size >= 3) {
                    if (lore[0] == "§8Item Reward" && lore[1].isEmpty()) {
                        val line2 = lore[2].stripControlCodes()
                        val enchantName =
                            line2.substringBeforeLast(" ").replace(" ", "_").uppercase()
                        itemId = "ENCHANTED_BOOK-" + enchantName + "-" + item.stackSize
                        isSuperpairsReward = true
                    }
                }
            }
        }
        if (itemId != null) {
            if (Skytils.config.showLowestBINPrice || Skytils.config.showCoinsPerBit) {
                val auctionIdentifier = if (isSuperpairsReward) itemId else AuctionData.getIdentifier(item)
                if (auctionIdentifier != null) {
                    // this might actually have multiple items as the price
                    val valuePer = AuctionData.lowestBINs[auctionIdentifier]
                    if (valuePer != null) {
                        if (Skytils.config.showLowestBINPrice) {
                            val total =
                                if (isSuperpairsReward) NumberUtil.nf.format(valuePer) else NumberUtil.nf.format(
                                    valuePer * item!!.stackSize
                                )
                            event.toolTip.add(
                                "§6Lowest BIN Price: §b$total" + if (item!!.stackSize > 1 && !isSuperpairsReward) " §7(" + NumberUtil.nf.format(
                                    valuePer
                                ) + " each§7)" else ""
                            )
                        }
                        if (Skytils.config.showCoinsPerBit) {
                            var bitValue = bitCosts.getOrDefault(auctionIdentifier, -1)
                            if (bitValue == -1 && SBInfo.lastOpenContainerName == "Community Shop" || SBInfo.lastOpenContainerName?.startsWith(
                                    "Bits Shop - "
                                ) == true
                            ) {
                                val lore = getItemLore(item!!)
                                for (i in lore.indices) {
                                    val line = lore[i]
                                    if (line == "§7Cost" && i + 3 < lore.size && lore[i + 3] == "§eClick to trade!") {
                                        val bits = lore[i + 1]
                                        if (bits.startsWith("§b") && bits.endsWith(" Bits")) {
                                            bitValue = bits.replace("[^0-9]".toRegex(), "").toInt()
                                            bitCosts[auctionIdentifier] = bitValue
                                            break
                                        }
                                    }
                                }
                            }
                            if (bitValue != -1) event.toolTip.add("§6Coin/Bit: §b" + NumberUtil.nf.format(valuePer / bitValue))
                        }
                    }
                }
            }
            if (Skytils.config.showNPCSellPrice) {
                val valuePer = sellPrices[itemId]
                if (valuePer != null) event.toolTip.add(
                    "§6NPC Value: §b" + NumberUtil.nf.format(valuePer * item!!.stackSize) + if (item.stackSize > 1) " §7(" + NumberUtil.nf.format(
                        valuePer
                    ) + " each§7)" else ""
                )
            }
        }
        if (Skytils.config.showRadioactiveBonus && itemId == "TARANTULA_HELMET") {
            val bonus = try {
                (TabListUtils.tabEntries[68].text.substringAfter("❁").removeSuffix("§r").toInt()
                    .coerceAtMost(1000) / 10).toString()
            } catch (e: Exception) {
                "Error"
            }
            for (i in event.toolTip.indices) {
                val line = event.toolTip[i]
                if (line.contains("§7Crit Damage:")) {
                    event.toolTip.add(i + 1, "§8Radioactive Bonus: §c+${bonus}%")
                    break
                }
            }
        }
        if (itemId == "PREHISTORIC_EGG" && extraAttr != null) {
            event.toolTip.add((event.toolTip.indexOfFirst { it.contains("Legendary Armadillo") } + 1),
                "§7Blocks Walked: §c${extraAttr.getInteger("blocks_walked")}")
        }
        if (Skytils.config.showGemstones && extraAttr?.hasKey("gems") == true) {
            val gems = extraAttr.getCompoundTag("gems")
            event.toolTip.add("§bGemstones: ")
            event.toolTip.addAll(gems.keySet.filter { !it.endsWith("_gem") && it != "unlocked_slots" }.map {
                "  §6- ${
                    gems.getString(it).toTitleCase()
                } ${
                    gems.getString("${it}_gem").ifEmpty { it.substringBeforeLast("_") }.toTitleCase()
                }"
            })
        }
        if (DevTools.getToggle("nbt") && Keyboard.isKeyDown(46) && GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown() && !GuiScreen.isAltKeyDown()) {
            GuiScreen.setClipboardString(event.itemStack?.tagCompound?.toString())
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inSkyblock || mc.theWorld == null) return
        event.packet.apply {
            if (this is S2APacketParticles) {
                if (type == EnumParticleTypes.EXPLOSION_LARGE && Skytils.config.hideImplosionParticles) {
                    if (isLongDistance && count == 8 && speed == 8f && xOffset == 0f && yOffset == 0f && zOffset == 0f) {
                        val dist = (if (DungeonFeatures.hasBossSpawned && Utils.equalsOneOf(
                                DungeonFeatures.dungeonFloor,
                                "F7",
                                "M7"
                            )
                        ) 4f else 11f).pow(2f)

                        if (mc.theWorld.playerEntities.any {
                                it.heldItem != null && it.uniqueID.version() == 4 && it.getDistanceSq(
                                    x,
                                    y,
                                    z
                                ) <= dist && getDisplayName(it.heldItem).stripControlCodes().containsAny(
                                    "Necron's Blade", "Scylla", "Astraea", "Hyperion", "Valkyrie"
                                )
                            }) {
                            event.isCanceled = true
                        }
                    }
                }
            }
            if (this is S2FPacketSetSlot && func_149175_c() == 0) {
                if (mc.thePlayer == null || (!Utils.inSkyblock && mc.thePlayer.ticksExisted > 1)) return
                val slot = func_149173_d()

                val item = func_149174_e() ?: return
                val extraAttr = getExtraAttributes(item) ?: return
                val itemId = getSkyBlockItemID(extraAttr) ?: return

                if (itemId == "ARROW_SWAPPER") {
                    selectedArrow = getItemLore(item).find {
                        it.startsWith("§aSelected: §")
                    }?.substringAfter("§aSelected: ") ?: "§cUnknown"
                }
                if (Utils.equalsOneOf(itemId, "SOULFLOW_PILE", "SOULFLOW_BATTERY", "SOULFLOW_SUPERCELL")) {
                    getItemLore(item).find {
                        it.startsWith("§7Internalized: ")
                    }?.substringAfter("§7Internalized: ")?.let { s ->
                        soulflowAmount = s
                        s.drop(2).filter { it.isDigit() }.toIntOrNull()?.let {
                            if (Skytils.config.lowSoulflowPing > 0) {
                                if (it <= Skytils.config.lowSoulflowPing && !lowSoulFlowPinged) {
                                    GuiManager.createTitle("§cLow Soulflow", 20)
                                    lowSoulFlowPinged = true
                                } else if (it > Skytils.config.lowSoulflowPing) {
                                    lowSoulFlowPinged = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onEntitySpawn(event: EntityJoinWorldEvent) {
        if (!Utils.inSkyblock) return
        if (event.entity !is EntityFishHook || !Skytils.config.hideFishingHooks) return
        if ((event.entity as EntityFishHook).angler is EntityOtherPlayerMP) {
            event.entity.setDead()
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (!Utils.inSkyblock) return
        if (event.entity !== mc.thePlayer) return
        val item = event.entityPlayer.heldItem
        val itemId = getSkyBlockItemID(item) ?: return
        if (Skytils.config.preventPlacingWeapons && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && (Utils.equalsOneOf(
                itemId,
                "FLOWER_OF_TRUTH",
                "BAT_WAND",
                "WEIRD_TUBA"
            ))
        ) {
            val block = mc.theWorld.getBlockState(event.pos)
            if (!interactables.contains(block.block) || Utils.inDungeons && (block.block === Blocks.coal_block || block.block === Blocks.stained_hardened_clay)) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.Post) {
        val item = event.stack ?: return
        if (!Utils.inSkyblock || item.stackSize != 1 || item.tagCompound?.hasKey("SkytilsNoItemOverlay") == true) return
        var stackTip = ""
        val extraAttributes = getExtraAttributes(item)
        if (extraAttributes != null) {
            if (Skytils.config.showPotionTier && extraAttributes.hasKey("potion_level")) {
                stackTip = extraAttributes.getInteger("potion_level").toString()
            } else if ((Skytils.config.showEnchantedBookTier || Skytils.config.showEnchantedBookAbbreviation) && item.item === Items.enchanted_book && extraAttributes.hasKey(
                    "enchantments"
                )
            ) {
                val enchantments = extraAttributes.getCompoundTag("enchantments")
                val enchantmentNames = enchantments.keySet
                if (enchantments.keySet.size == 1) {
                    val name = enchantmentNames.first()
                    if (Skytils.config.showEnchantedBookAbbreviation) {
                        val parts = name.split("_")
                        val prefix: String = if (parts[0] == "ultimate") {
                            "§d§l" + parts.drop(1).joinToString("") { s -> s.substring(0, 1).uppercase() }
                        } else {
                            if (parts.size > 1) {
                                parts.joinToString("") { s -> s.substring(0, 1).uppercase() }
                            } else {
                                parts[0].substring(0, parts[0].length.coerceAtMost(3)).toTitleCase() + "."
                            }
                        }
                        GlStateManager.disableLighting()
                        GlStateManager.disableDepth()
                        GlStateManager.disableBlend()
                        GlStateManager.pushMatrix()
                        GlStateManager.translate(event.x.toFloat(), event.y.toFloat(), 1f)
                        GlStateManager.scale(0.8, 0.8, 1.0)
                        ScreenRenderer.fontRenderer.drawString(
                            prefix,
                            0f,
                            0f,
                            CommonColors.WHITE,
                            TextAlignment.LEFT_RIGHT,
                            TextShadow.NORMAL
                        )
                        GlStateManager.popMatrix()
                        GlStateManager.enableLighting()
                        GlStateManager.enableDepth()
                    }
                    if (Skytils.config.showEnchantedBookTier) stackTip =
                        enchantments.getInteger(name.toString()).toString()
                }
            } else if (Skytils.config.showDungeonItemLevel && extraAttributes.hasKey("dungeon_item_level")) {
                stackTip = extraAttributes.getInteger("dungeon_item_level").toString()
            }
            if (extraAttributes.hasKey("pickonimbus_durability")) {
                RenderUtil.drawDurabilityBar(
                    event.x,
                    event.y,
                    1 - extraAttributes.getInteger("pickonimbus_durability") / 5000.0
                )
            }
        }
        val lore = getItemLore(item)
        if (lore.isNotEmpty()) {
            if (Skytils.config.showPetCandies && item.item === Items.skull) {
                for (line in lore) {
                    val candyLineMatcher = candyPattern.matcher(line)
                    if (candyLineMatcher.matches()) {
                        stackTip = candyLineMatcher.group(1).toString()
                        break
                    }
                }
            }
        }
        if (stackTip.isNotEmpty()) {
            GlStateManager.disableLighting()
            GlStateManager.disableDepth()
            GlStateManager.disableBlend()
            event.fr.drawStringWithShadow(
                stackTip,
                (event.x + 17 - event.fr.getStringWidth(stackTip)).toFloat(),
                (event.y + 9).toFloat(),
                16777215
            )
            GlStateManager.enableLighting()
            GlStateManager.enableDepth()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.showEtherwarpTeleportPos && mc.thePlayer?.isSneaking == true) {
            val extraAttr = getExtraAttributes(mc.thePlayer.heldItem) ?: return
            if (!extraAttr.getBoolean("ethermerge")) return
            val dist = 57.0 + extraAttr.getInteger("tuned_transmission")
            val vec3 = mc.thePlayer.getPositionEyes(event.partialTicks)
            val vec31 = mc.thePlayer.getLook(event.partialTicks)
            val vec32 = vec3.addVector(
                vec31.xCoord * dist,
                vec31.yCoord * dist,
                vec31.zCoord * dist
            )
            val obj = mc.theWorld.rayTraceBlocks(vec3, vec32, true, false, true) ?: return
            val block = obj.blockPos ?: return
            val state = mc.theWorld.getBlockState(block)
            if (isValidEtherwarpPos(obj)) {
                val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
                GlStateManager.disableCull()
                GlStateManager.disableDepth()
                GlStateManager.enableBlend()
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                state.block.setBlockBoundsBasedOnState(mc.theWorld, block)
                RenderUtil.drawFilledBoundingBox(
                    state.block.getSelectedBoundingBox(mc.theWorld, block)
                        .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                        .offset(-viewerX, -viewerY, -viewerZ),
                    Skytils.config.showEtherwarpTeleportPosColor
                )
                GlStateManager.disableBlend()
                GlStateManager.enableCull()
                GlStateManager.enableDepth()
            }
        }
    }

    private fun isValidEtherwarpPos(obj: MovingObjectPosition): Boolean {
        val pos = obj.blockPos
        val sideHit = obj.sideHit

        return mc.theWorld.getBlockState(pos).block.material.isSolid && (1..2).all {
            val newPos = pos.up(it)
            val newBlock = mc.theWorld.getBlockState(newPos)
            if (sideHit === EnumFacing.UP && (Utils.equalsOneOf(
                    newBlock.block,
                    Blocks.fire,
                    Blocks.skull
                ) || newBlock.block is BlockLiquid)
            ) return@all false
            if (sideHit !== EnumFacing.UP && newBlock.block is BlockSign) return@all false
            if (newBlock.block is BlockLadder || newBlock.block is BlockDoor) return@all false
            return@all newBlock.block.isPassable(mc.theWorld, newPos)
        }
    }

    class SelectedArrowDisplay : GuiElement("Arrow Swapper Display", FloatPair(0.65f, 0.85f)) {
        override fun render() {
            if (toggled && Utils.inSkyblock) {
                val alignment =
                    if (actualX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    selectedArrow,
                    if (actualX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val alignment =
                if (actualX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "§aSelected: §rSkytils Arrow",
                if (actualX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                0f,
                CommonColors.RAINBOW,
                alignment,
                TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§aSelected: §rSkytils Arrow")
        override val toggled: Boolean
            get() = Skytils.config.showSelectedArrowDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class StackingEnchantDisplay : GuiElement("Stacking Enchant Display", FloatPair(0.65f, 0.85f)) {
        override fun render() {
            if (toggled && Utils.inSkyblock && stackingEnchantDisplayText.isNotBlank()) {
                val alignment =
                    if (actualX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    stackingEnchantDisplayText,
                    if (actualX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val alignment =
                if (actualX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Expertise 10: Maxed",
                if (actualX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                0f,
                CommonColors.RAINBOW,
                alignment,
                TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Expertise 10 (Maxed)")
        override val toggled: Boolean
            get() = Skytils.config.stackingEnchantProgressDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class SoulflowGuiElement : GuiElement("Soulflow Display", FloatPair(0.65f, 0.85f)) {
        override fun render() {
            if (Utils.inSkyblock && toggled) {
                val alignment =
                    if (actualX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    soulflowAmount,
                    if (actualX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val alignment =
                if (actualX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "§3100⸎ Soulflow",
                if (actualX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                0f,
                CommonColors.WHITE,
                alignment,
                TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§3100⸎ Soulflow")
        override val toggled: Boolean
            get() = Skytils.config.showSoulflowDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}
