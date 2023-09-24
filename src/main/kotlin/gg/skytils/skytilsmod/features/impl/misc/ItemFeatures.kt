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
package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent.SlotClickEvent
import gg.skytils.skytilsmod.events.impl.GuiRenderItemEvent
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.handlers.AuctionData
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiContainer
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.ItemUtil.getDisplayName
import gg.skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import gg.skytils.skytilsmod.utils.ItemUtil.getItemLore
import gg.skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import gg.skytils.skytilsmod.utils.NumberUtil.romanToDecimal
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.RenderUtil.renderRarity
import gg.skytils.skytilsmod.utils.Utils.equalsOneOf
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.block.BlockDoor
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockSign
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.projectile.EntityFishHook
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.util.Constants
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.pow

object ItemFeatures {

    private val candyPattern = Regex("§a\\((\\d+)/10\\) Pet Candy Used")
    private val headPattern =
        Regex("(?:DIAMOND|GOLD)_(?:(BONZO)|(SCARF)|(PROFESSOR)|(THORN)|(LIVID)|(SADAN)|(NECRON))_HEAD")

    // TODO: it is possible for 2 items to have the same name but different material
    val itemIdToNameLookup = hashMapOf<String, String>()
    val sellPrices = HashMap<String, Double>()
    val bitCosts = HashMap<String, Int>()
    val copperCosts = HashMap<String, Int>()
    val hotbarRarityCache = arrayOfNulls<ItemRarity>(9)
    var selectedArrow = ""
    var soulflowAmount = ""
    var stackingEnchantDisplayText = ""
    var lowSoulFlowPinged = false
    var lastShieldUse = -1L
    var lastShieldClick = 0L

    init {
        SelectedArrowDisplay()
        StackingEnchantDisplay()
        SoulflowGuiElement()
        WitherShieldDisplay()
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

    init {
        TickTask(4, repeats = true) {
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
        }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (Utils.inSkyblock && Skytils.config.showItemRarity && event.slot.hasStack) {
            renderRarity(event.slot.stack, event.slot.xDisplayPosition, event.slot.yDisplayPosition)
        }
        if (event.container is ContainerChest) {
            val chestName = event.chestName
            if (chestName.startsWithAny("Salvage", "Ender Chest") || Utils.equalsOneOf(
                    chestName,
                    "Ophelia",
                    "Trades"
                ) || (chestName.contains("Backpack") && !chestName.endsWith("Recipe"))
            ) {
                if (Skytils.config.highlightSalvageableItems) {
                    if (event.slot.hasStack) {
                        val stack = event.slot.stack
                        if (ItemUtil.isSalvageable(stack)) {
                            GlStateManager.translate(0f, 0f, 1f)
                            event.slot highlight Color(15, 233, 233)
                            GlStateManager.translate(0f, 0f, -1f)
                        }
                    }
                }
            }
            if (chestName == "Ophelia" || chestName == "Trades" || chestName == "Booster Cookie") {
                if (Skytils.config.highlightDungeonSellableItems) {
                    if (event.slot.hasStack) {
                        val stack = event.slot.stack
                        if (stack.displayName.containsAny(
                                "Defuse Kit",
                                "Lever",
                                "Torch",
                                "Stone Button",
                                "Tripwire Hook",
                                "Journal Entry",
                                "Training Weights",
                                "Mimic Fragment",
                                "Healing 8 Splash Potion",
                                "Healing VIII Splash Potion",
                                "Premium Flesh"
                            )
                        ) event.slot highlight Color(255, 50, 150, 255)
                    }
                }
            }
            if (Skytils.config.combineHelper && Utils.equalsOneOf(
                    event.chestName,
                    "Anvil",
                    "Attribute Fusion"
                )
            ) {
                val item = event.container.getSlot(29).stack ?: return
                if (event.container.getSlot(33).hasStack) return
                val candidate = event.slot.stack ?: return
                val nbt1 = getExtraAttributes(item) ?: return
                val nbt2 = getExtraAttributes(candidate) ?: return
                val tagName = when (getSkyBlockItemID(nbt1) to getSkyBlockItemID(nbt2)) {
                    "ENCHANTED_BOOK" to "ENCHANTED_BOOK" -> "enchantments"
                    "ATTRIBUTE_SHARD" to "ATTRIBUTE_SHARD" -> "attributes"
                    else -> return
                }
                val typeList = listOf(nbt1, nbt2).map { nbt ->
                    nbt.getCompoundTag(tagName)
                }
                val tierList = typeList.mapNotNull { nbt ->
                    nbt.keySet.takeIf { it.size == 1 }?.first()
                }
                if (tierList.size != 2 || tierList[0] != tierList[1] || typeList[0].getInteger(tierList[0]) != typeList[1].getInteger(
                        tierList[1]
                    )
                ) return

                event.slot highlight Color(17, 252, 243)
            }
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!Utils.inSkyblock) return
        if (event.container is ContainerChest) {
            if (event.slot != null && event.slot.hasStack) {
                val item = event.slot.stack ?: return
                val extraAttr = getExtraAttributes(item)
                if (Skytils.config.stopClickingNonSalvageable) {
                    if (event.chestName.startsWith("Salvage") && extraAttr != null) {
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
                            line2.substringBeforeLast(" ").replace(Regex("[\\s-]"), "_").uppercase()
                        itemId = "ENCHANTED_BOOK-" + enchantName + "-" + item.stackSize
                        isSuperpairsReward = true
                    }
                }
            }
        }
        if (itemId != null) {
            if (Skytils.config.showLowestBINPrice || Skytils.config.showCoinsPerBit || Skytils.config.showCoinsPerCopper) {
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
                        if (Skytils.config.showCoinsPerCopper) {
                            var copperValue = copperCosts.getOrDefault(auctionIdentifier, -1)
                            if (copperValue == -1 && SBInfo.lastOpenContainerName == "SkyMart") {
                                val lore = getItemLore(item!!)
                                for (i in lore.indices) {
                                    val line = lore[i]
                                    if (line == "§7Cost" && i + 3 < lore.size && equalsOneOf(
                                            lore[i + 3],
                                            "§eClick to trade!",
                                            "§cNot unlocked!"
                                        )
                                    ) {
                                        val copper = lore[i + 1]
                                        if (copper.startsWith("§c") && copper.endsWith(" Copper")) {
                                            copperValue = copper.replace("[^0-9]".toRegex(), "").toInt()
                                            copperCosts[auctionIdentifier] = copperValue
                                            break
                                        }
                                    }
                                }
                            }
                            if (copperValue != -1) event.toolTip.add(
                                "§6Coin/Copper: §c" + NumberUtil.nf.format(valuePer / copperValue)
                            )
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
                (TabListUtils.tabEntries[68].second.substringAfter("❁").removeSuffix("§r").toInt()
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
            event.toolTip.addAll(gems.keySet.filterNot { it.endsWith("_gem") || it == "unlocked_slots" }.map {
                val quality = when (val tag: NBTBase? = gems.getTag(it)) {
                    is NBTTagCompound -> tag.getString("quality").toTitleCase().ifEmpty { "Report Unknown" }
                    is NBTTagString -> tag.string.toTitleCase()
                    null -> "Report Issue"
                    else -> "Report Tag $tag"
                }
                "  §6- $quality ${
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
            if (this is S1CPacketEntityMetadata && lastShieldClick != -1L && entityId == mc.thePlayer?.entityId && System.currentTimeMillis() - lastShieldClick <= 500 && func_149376_c()?.any { it.dataValueId == 17 } == true) {
                lastShieldUse = System.currentTimeMillis()
                lastShieldClick = -1
            }
        }
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.SendEvent) {
        if (!Utils.inSkyblock || lastShieldUse != -1L || mc.thePlayer?.heldItem == null) return
        if (event.packet is C08PacketPlayerBlockPlacement && mc.thePlayer.heldItem.item == Items.iron_sword && getExtraAttributes(
                mc.thePlayer.heldItem
            )?.getTagList("ability_scroll", Constants.NBT.TAG_STRING)?.asStringSet()
                ?.contains("WITHER_SHIELD_SCROLL") == true
        ) {
            lastShieldClick = System.currentTimeMillis()
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
                "BOUQUET_OF_LIES",
                "MOODY_GRAPPLESHOT",
                "BAT_WAND",
                "STARRED_BAT_WAND",
                "WEIRD_TUBA",
                "WEIRDER_TUBA"
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
        val matrixStack = UMatrixStack()
        var stackTip = ""
        val lore = getItemLore(item).takeIf { it.isNotEmpty() }
        getExtraAttributes(item)?.let { extraAttributes ->
            val matrixStack = UMatrixStack()
            val itemId = getSkyBlockItemID(extraAttributes)
            if (Skytils.config.showPotionTier && extraAttributes.hasKey("potion_level")) {
                stackTip = extraAttributes.getInteger("potion_level").toString()
            } else if (Skytils.config.showAttributeShardLevel && itemId == "ATTRIBUTE_SHARD") {
                extraAttributes.getCompoundTag("attributes").takeUnless {
                    it.hasNoTags()
                }?.let {
                    /*
                    If they ever add the ability to combine attributes on shards, this will need to be updated to:
                    stackTip = it.keySet.maxOf { s -> it.getInteger(s) }.toString()
                    */
                    stackTip = it.getInteger(it.keySet.first()).toString()
                }
            } else if ((Skytils.config.showEnchantedBookTier || Skytils.config.showEnchantedBookAbbreviation) && itemId == "ENCHANTED_BOOK") {
                extraAttributes.getCompoundTag("enchantments").takeIf {
                    it.keySet.size == 1
                }?.let { enchantments ->
                    val name = enchantments.keySet.first()
                    if (Skytils.config.showEnchantedBookAbbreviation) {
                        val enchant = EnchantUtil.enchants.find { it.nbtName == name }
                        val prefix: String = if (enchant != null) {
                            val parts = enchant.loreName.split(" ")
                            val joined = if (parts.size > 1) parts.joinToString("") { it[0].uppercase() } else "${
                                parts[0].take(3).toTitleCase()
                            }."
                            if (enchant.nbtName.startsWith("ultimate")) {
                                "§d§l${joined}"
                            } else joined
                        } else {
                            val parts = name.split("_")
                            if (parts[0] == "ultimate") {
                                "§d§l" + parts.drop(1).joinToString("") { s -> s[0].uppercase() }
                            } else {
                                if (parts.size > 1) {
                                    parts.joinToString("") { s -> s[0].uppercase() }
                                } else {
                                    parts[0].take(3).toTitleCase() + "."
                                }
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
                        enchantments.getInteger(name).toString()
                }
            } else if (Skytils.config.showHeadFloorNumber && item.item === Items.skull && headPattern.matches(
                    itemId ?: ""
                )
            ) {
                stackTip = headPattern.matchEntire(itemId!!)?.groups?.indexOfLast { it != null }.toString()
            } else if (Skytils.config.showStarCount && ItemUtil.getStarCount(extraAttributes) > 0) {
                stackTip = ItemUtil.getStarCount(extraAttributes).toString()
            }
            if (extraAttributes.hasKey("pickonimbus_durability")) {
                RenderUtil.drawDurabilityBar(
                    event.x,
                    event.y,
                    1 - extraAttributes.getInteger("pickonimbus_durability") / 5000.0
                )
            }
            if (Skytils.config.showAttributeShardAbbreviation && itemId == "ATTRIBUTE_SHARD" && extraAttributes.getCompoundTag(
                    "attributes"
                ).keySet.size == 1
            ) {
                lore?.getOrNull(0)?.split(' ')?.dropLastWhile { it.romanToDecimal() == 0 }?.dropLast(1)
                    ?.joinToString(separator = "") {
                        if (it.startsWith('§'))
                            it.substring(0, 2) + it[2].uppercase()
                        else
                            it[0].uppercase()
                    }?.let { attribute ->
                        UGraphics.disableLighting()
                        UGraphics.disableDepth()
                        UGraphics.disableBlend()
                        matrixStack.push()
                        matrixStack.translate(event.x.toFloat(), event.y.toFloat(), 1f)
                        matrixStack.scale(0.8, 0.8, 1.0)
                        matrixStack.runWithGlobalState {
                            ScreenRenderer.fontRenderer.drawString(
                                attribute,
                                0f,
                                0f,
                                CommonColors.WHITE,
                                TextAlignment.LEFT_RIGHT,
                                TextShadow.NORMAL
                            )
                        }
                        matrixStack.pop()
                        UGraphics.enableLighting()
                        UGraphics.enableDepth()
                    }
            }
        }
        if (Skytils.config.showPetCandies && item.item === Items.skull) { // TODO: Use NBT
            lore?.forEach { line ->
                candyPattern.find(line)?.let {
                    stackTip = it.groups[1]!!.value
                    return@forEach
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
    fun onDrawContainerForeground(event: GuiContainerEvent.ForegroundDrawnEvent) {
        if (!Skytils.config.combineHelper || !Utils.inSkyblock) return
        if (event.container !is ContainerChest || !Utils.equalsOneOf(
                event.chestName,
                "Anvil",
                "Attribute Fusion"
            )
        ) return
        val item1 = event.container.getSlot(29).stack ?: return
        val item2 = event.container.getSlot(33).stack ?: return
        val nbt1 = getExtraAttributes(item1) ?: return
        val nbt2 = getExtraAttributes(item2) ?: return
        val tagName = when (getSkyBlockItemID(nbt1) to getSkyBlockItemID(nbt2)) {
            "ENCHANTED_BOOK" to "ENCHANTED_BOOK" -> "enchantments"
            "ATTRIBUTE_SHARD" to "ATTRIBUTE_SHARD" -> "attributes"
            else -> return
        }
        val typeList = listOf(nbt1, nbt2).map { nbt ->
            nbt.getCompoundTag(tagName)
        }
        val tierList = typeList.mapNotNull { nbt ->
            nbt.keySet.takeIf { it.size == 1 }?.first()
        }
        if (tierList.size != 2) return
        val errorString = if (tierList[0] != tierList[1]) {
            "Types don't match!"
        } else if (typeList[0].getInteger(tierList[0]) != typeList[1].getInteger(tierList[1])) {
            "Tiers don't match!"
        } else return
        val gui =
            event.gui as AccessorGuiContainer
        UGraphics.disableLighting()
        UGraphics.disableBlend()
        UGraphics.disableDepth()
        ScreenRenderer.fontRenderer.drawString(
            errorString,
            gui.xSize / 2f,
            22.5f,
            CommonColors.RED,
            TextAlignment.MIDDLE
        )
        UGraphics.enableDepth()
        UGraphics.enableBlend()
        UGraphics.enableLighting()
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
                RenderUtil.drawSelectionBox(
                    block,
                    state.block,
                    Skytils.config.showEtherwarpTeleportPosColor,
                    event.partialTicks
                )
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

    class SelectedArrowDisplay : GuiElement("Arrow Swapper Display", x = 0.65f, y = 0.85f) {
        override fun render() {
            if (toggled && Utils.inSkyblock) {
                val alignment =
                    if (scaleX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    selectedArrow,
                    if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val alignment =
                if (scaleX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "§aSelected: §rSkytils Arrow",
                if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
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

    class StackingEnchantDisplay : GuiElement("Stacking Enchant Display", x = 0.65f, y = 0.85f) {
        override fun render() {
            if (toggled && Utils.inSkyblock && stackingEnchantDisplayText.isNotBlank()) {
                val alignment =
                    if (scaleX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    stackingEnchantDisplayText,
                    if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val alignment =
                if (scaleX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Expertise 10: Maxed",
                if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
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

    class SoulflowGuiElement : GuiElement("Soulflow Display", x = 0.65f, y = 0.85f) {
        override fun render() {
            if (Utils.inSkyblock && toggled) {
                val alignment =
                    if (scaleX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    soulflowAmount,
                    if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val alignment =
                if (scaleX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "§3100⸎ Soulflow",
                if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
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


    class WitherShieldDisplay : GuiElement("Wither Shield Display", x = 0.65f, y = 0.85f) {
        override fun render() {
            if (toggled && Utils.inSkyblock) {
                val alignment =
                    if (scaleX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                if (lastShieldUse != -1L) {
                    val diff =
                        ((lastShieldUse + (if (Skytils.config.assumeWitherImpact) 5000 else 10000) - System.currentTimeMillis()) / 1000f)
                    ScreenRenderer.fontRenderer.drawString(
                        "Shield: §c${"%.2f".format(diff)}s",
                        if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                        0f,
                        CommonColors.ORANGE,
                        alignment,
                        TextShadow.NORMAL
                    )
                    if (diff < 0) lastShieldUse = -1
                } else {
                    ScreenRenderer.fontRenderer.drawString(
                        "Shield: §aREADY",
                        if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                        0f,
                        CommonColors.ORANGE,
                        alignment,
                        TextShadow.NORMAL
                    )
                }
            }
        }

        override fun demoRender() {
            val alignment =
                if (scaleX < UResolution.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "§6Shield: §aREADY",
                if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                0f,
                CommonColors.WHITE,
                alignment,
                TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§6Shield: §aREADY")
        override val toggled: Boolean
            get() = Skytils.config.witherShieldCooldown

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    @Serializable
    data class APISBItem(
        @SerialName("id")
        val id: String,
        @SerialName("material")
        val material: String,
        @SerialName("motes_sell_price")
        val motesSellPrice: Double? = null,
        @SerialName("name")
        val name: String,
        @SerialName("npc_sell_price")
        val npcSellPrice: Double? = null,
    )
}
