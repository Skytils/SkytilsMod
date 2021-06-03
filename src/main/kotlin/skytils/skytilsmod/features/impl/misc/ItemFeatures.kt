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

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.projectile.EntityFishHook
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.events.GuiContainerEvent.SlotClickEvent
import skytils.skytilsmod.events.GuiRenderItemEvent
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.features.impl.handlers.BlockAbility
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

class ItemFeatures {
    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (Utils.inSkyblock && Skytils.config.showItemRarity && event.slot.hasStack) {
            renderRarity(event.slot.stack, event.slot.xDisplayPosition, event.slot.yDisplayPosition)
        }
        if (event.gui is GuiChest) {
            val gui = event.gui
            val chest = gui.inventorySlots as ContainerChest
            val inv = chest.lowerChestInventory
            val chestName = inv.displayName.unformattedText.trim { it <= ' ' }
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
                        if (extraAttr != null && extraAttr.hasKey("baseStatBoostPercentage") && !extraAttr.hasKey("dungeon_item_level")) {
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
                                "Stone", "Tripwire Hook", "Journal Entry",
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
            val chestName = inv.displayName.unformattedText.trim { it <= ' ' }
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
        if (Skytils.config.showSoulEaterBonus) {
            if (extraAttr != null) {
                if (extraAttr.hasKey("ultimateSoulEaterData")) {
                    val bonus = extraAttr.getInteger("ultimateSoulEaterData")
                    var foundStrength = false
                    for (i in event.toolTip.indices) {
                        val line = event.toolTip[i]
                        if (line.contains("§7Strength:")) {
                            event.toolTip.add(i + 1, "§4 Soul Eater Bonus: §a$bonus")
                            foundStrength = true
                            break
                        }
                    }
                    if (!foundStrength) {
                        val index = if (event.showAdvancedItemTooltips) 4 else 2
                        event.toolTip.add(event.toolTip.size - index, "")
                        event.toolTip.add(event.toolTip.size - index, "§4 Soul Eater Bonus: §a$bonus")
                    }
                }
            }
        }
        if (Skytils.config.showRadioactiveBonus && itemId == "TARANTULA_HELMET") {
            val bonus = try {
                (TabListUtils.tabEntries[68].getText().substringAfter("❁").removeSuffix("§r").toInt()
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
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock || mc.theWorld == null) return
        try {
            if (event.packet is S2APacketParticles) {
                val packet = event.packet
                val type = packet.particleType
                val longDistance = packet.isLongDistance
                val count = packet.particleCount
                val speed = packet.particleSpeed
                val xOffset = packet.xOffset
                val yOffset = packet.yOffset
                val zOffset = packet.zOffset
                val x = packet.xCoordinate
                val y = packet.yCoordinate
                val z = packet.zCoordinate
                val pos = Vec3(x, y, z)
                if (type == EnumParticleTypes.EXPLOSION_LARGE && Skytils.config.hideImplosionParticles) {
                    if (longDistance && count == 8 && speed == 8f && xOffset == 0f && yOffset == 0f && zOffset == 0f) {
                        for (player in mc.theWorld.playerEntities) {
                            if (pos.squareDistanceTo(Vec3(player.posX, player.posY, player.posZ)) <= 11 * 11) {
                                val item = player.heldItem
                                if (item != null) {
                                    val itemName = getDisplayName(item).stripControlCodes()
                                    if (itemName.contains("Necron's Blade") || itemName.contains("Scylla") || itemName.contains(
                                            "Astraea"
                                        ) || itemName.contains("Hyperion") || itemName.contains("Valkyrie")
                                    ) {
                                        event.isCanceled = true
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: ConcurrentModificationException) {
            e.printStackTrace()
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
            if (!BlockAbility.interactables.contains(block.block) || Utils.inDungeons && (block.block === Blocks.coal_block || block.block === Blocks.stained_hardened_clay)) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.Post) {
        val item = event.stack
        if (!Utils.inSkyblock || item == null || item.stackSize != 1) return
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
                                parts[0].substring(0, parts[0].length.coerceAtMost(3))
                                    .replaceFirstChar { it.titlecase() } + "."
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

    companion object {
        private val mc = Minecraft.getMinecraft()
        private val candyPattern = Pattern.compile("§a\\((\\d+)/10\\) Pet Candy Used")
        val sellPrices = HashMap<String, Double>()
        val bitCosts = HashMap<String, Int>()

        init {
            SoulStrengthGuiElement()
            SoulflowGuiElement()
        }
    }

    class SoulStrengthGuiElement : GuiElement("Soul Eater Strength", FloatPair(200, 10)) {
        override fun render() {
            val player = mc.thePlayer
            if (toggled && Utils.inSkyblock && player != null) {
                val item = mc.thePlayer.heldItem
                if (item != null) {
                    val extraAttr = getExtraAttributes(item)
                    if (extraAttr != null) {
                        if (extraAttr.hasKey("ultimateSoulEaterData")) {
                            val bonus = extraAttr.getInteger("ultimateSoulEaterData")
                            mc.fontRendererObj.drawString("§cSoul Strength: §a$bonus", 0f, 0f, 0xFFFFFF, true)
                        }
                    }
                }
            }
        }

        override fun demoRender() {
            ScreenRenderer.fontRenderer.drawString(
                "§cSoul Strength: §a1000",
                0f,
                0f,
                CommonColors.WHITE,
                TextAlignment.LEFT_RIGHT,
                TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§cSoul Strength: §a1000")
        override val toggled: Boolean
            get() = Skytils.config.showSoulEaterBonus

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class SoulflowGuiElement : GuiElement("Soulflow Display", FloatPair(0.65f, 0.85f)) {
        override fun render() {
            if (Utils.inSkyblock) {
                for (i in Skytils.mc.thePlayer.inventory.mainInventory) {
                    if (i == null) continue
                    if (!i.displayName.containsAny("Soulflow Pile", "Soulflow Battery", "Soulflow Supercell")) continue
                    for (str in getItemLore(i)) {
                        if (!str.startsWith("§7Internalized: ")) continue
                        if (Skytils.config.lowSoulflowPing > 0) {
                            val soulflow = (str.substring(18).filter { it.isDigit() }).toInt()
                            if (soulflow <= Skytils.config.lowSoulflowPing && !pinged) {
                                GuiManager.createTitle("§cLow Soulflow", 20)
                                pinged = true
                            } else if (soulflow > Skytils.config.lowSoulflowPing) {
                                pinged = false
                            }
                        }
                        if (toggled) {
                            val alignment =
                                if (actualX < ScaledResolution(mc).scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                            ScreenRenderer.fontRenderer.drawString(
                                str.substring(16),
                                if (actualX < ScaledResolution(mc).scaledWidth / 2f) 0f else width.toFloat(),
                                0f,
                                CommonColors.WHITE,
                                alignment,
                                TextShadow.NORMAL
                            )
                        }
                    }
                }
            }
        }

        override fun demoRender() {
            val alignment =
                if (actualX < ScaledResolution(mc).scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "§3100⸎ Soulflow",
                if (actualX < ScaledResolution(mc).scaledWidth / 2f) 0f else width.toFloat(),
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
        private var pinged = false

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}
