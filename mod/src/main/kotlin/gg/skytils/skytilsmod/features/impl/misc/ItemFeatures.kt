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

import gg.essential.elementa.unstable.layoutdsl.LayoutScope
import gg.essential.elementa.unstable.layoutdsl.Modifier
import gg.essential.elementa.unstable.layoutdsl.color
import gg.essential.elementa.unstable.state.v2.MutableState
import gg.essential.elementa.unstable.state.v2.State
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.BlockInteractEvent
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.impl.screen.GuiContainerForegroundDrawnEvent
import gg.skytils.event.impl.screen.GuiContainerPostDrawSlotEvent
import gg.skytils.event.impl.screen.GuiContainerPreDrawSlotEvent
import gg.skytils.event.impl.screen.GuiContainerSlotClickEvent
import gg.skytils.event.register
import gg.skytils.hypixel.types.skyblock.Pet
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.json
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.MainThreadPacketReceiveEvent
import gg.skytils.skytilsmod._event.PacketSendEvent
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures.dungeonFloorNumber
import gg.skytils.skytilsmod.features.impl.handlers.AuctionData
import gg.skytils.skytilsmod.features.impl.handlers.KuudraPriceData
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiContainer
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.ItemUtil.getDisplayName
import gg.skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import gg.skytils.skytilsmod.utils.ItemUtil.getItemLore
import gg.skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import gg.skytils.skytilsmod.utils.NumberUtil.romanToDecimal
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.RenderUtil.renderRarity
import gg.skytils.skytilsmod.utils.SkillUtils.level
import gg.skytils.skytilsmod.utils.Utils.equalsOneOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.block.DoorBlock
import net.minecraft.block.LadderBlock
import net.minecraft.block.FluidBlock
import net.minecraft.block.AbstractSignBlock
import net.minecraft.client.network.OtherClientPlayerEntity
import gg.essential.elementa.unstable.state.v2.mutableStateOf
import gg.essential.elementa.unstable.state.v2.stateUsingSystemTime
import gg.essential.universal.UDesktop
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMinecraft
import gg.skytils.skytilsmod.core.structure.v2.HudElement
import gg.skytils.skytilsmod.gui.layout.text
import gg.skytils.skytilsmod.utils.multiplatform.textComponent
import gg.skytils.skytilsmod.utils.rendering.DrawHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.block.Blocks
import net.minecraft.client.font.TextRenderer
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtString
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.util.math.Direction
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.BlockStateRaycastContext
import java.awt.Color
import java.time.Instant
import java.util.Optional
import java.util.WeakHashMap
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull
import kotlin.math.pow

object ItemFeatures : EventSubscriber {

    private val headPattern =
        Regex("(?:DIAMOND|GOLD)_(?:(BONZO)|(SCARF)|(PROFESSOR)|(THORN)|(LIVID)|(SADAN)|(NECRON))_HEAD")

    // TODO: it is possible for 2 items to have the same name but different material
    val itemIdToNameLookup = hashMapOf<String, String>()
    val sellPrices = HashMap<String, Double>()
    val bitCosts = HashMap<String, Int>()
    val copperCosts = HashMap<String, Int>()
    val hotbarRarityCache = hashMapOf<ItemStack, ItemRarity>()
    val soulflowState = mutableStateOf("")
    val stackingEnchantTextState = mutableStateOf("")
    var lowSoulFlowPinged = false
    val lastShieldUseState: MutableState<Instant> = mutableStateOf(Instant.MIN)
    var lastShieldClick = 0L

    init {
        Skytils.guiManager.registerElement(StackingEnchantHud())
        Skytils.guiManager.registerElement(SoulflowHud())
        Skytils.guiManager.registerElement(WitherShieldHud())
    }

    init {
        tickTimer(4, repeats = true) {
            val player = mc.player
            if (player != null && Utils.inSkyblock) {
                val held = player.inventory.selectedStack
                if (Skytils.config.showItemRarity) {
                    hotbarRarityCache.clear()

                    fun addToCache(stack: ItemStack?) {
                        if (stack?.isEmpty != false) return
                        hotbarRarityCache[stack] = ItemUtil.getRarity(stack)
                    }

                    (0..8).map { player.inventory.mainStacks[it] }.forEach(::addToCache)
                    //#if MC>=12000
                    addToCache(player.offHandStack)
                    //#endif
                }
                if (Skytils.config.stackingEnchantProgressDisplay.getUntracked()) {
                    apply {
                        also {
                            val extraAttr = getExtraAttributes(held) ?: return@also
                            val enchantments = extraAttr.getCompound("enchantments").getOrNull() ?: return@also
                            val stacking =
                                EnchantUtil.enchants.find { it is StackingEnchant && extraAttr.contains(it.nbtNum) } as? StackingEnchant
                                    ?: return@also

                            val stackingLevel = enchantments.getInt(stacking.nbtName).getOrDefault(0)
                            val stackingAmount = extraAttr.getLong(stacking.nbtNum).getOrDefault(0)

                            stackingEnchantTextState.set {
                                buildString {
                                    append("§b${stacking.loreName} §e$stackingLevel §f")
                                    val nextLevel = stacking.stackLevel.getOrNull(stackingLevel)
                                    if (stackingLevel == stacking.maxLevel || nextLevel == null) {
                                        append("(§e${stackingAmount}§f)")
                                    } else {
                                        append("(§c${stackingAmount} §f/ §a${NumberUtil.format(nextLevel)}§f)")
                                    }
                                }
                            }
                            return@apply
                        }
                        stackingEnchantTextState.set { "" }
                    }
                }
            }
        }
    }

    override fun setup() {
        register(::onDrawSlot)
        register(::onSlotClick)
        register(::ontooltip, EventPriority.Highest)
        register(::onReceivePacket)
        register(::onSendPacket)
        register(::onEntitySpawn)
        register(::onInteract)
        register(::onRenderItemOverlayPost)
        register(::onDrawContainerForeground)
        register(::onRenderWorld)
    }

    fun onDrawSlot(event: GuiContainerPreDrawSlotEvent) {
        if (Utils.inSkyblock && Skytils.config.showItemRarity && event.slot.hasStack()) {
            val x = (event.gui as AccessorGuiContainer).guiLeft
            val y = (event.gui as AccessorGuiContainer).guiTop
            renderRarity(event.slot.stack, x + event.slot.x, y + event.slot.y)
        }
        if (event.container is GenericContainerScreenHandler) {
            val chestName = event.chestName
            if (chestName.startsWithAny("Salvage", "Ender Chest") || equalsOneOf(
                    chestName,
                    "Ophelia",
                    "Trades"
                ) || (chestName.contains("Backpack") && !chestName.endsWith("Recipe"))
            ) {
                if (Skytils.config.highlightSalvageableItems) {
                    if (event.slot.hasStack()) {
                        val stack = event.slot.stack
                        if (ItemUtil.isSalvageable(stack)) {
                            val matrixStack = UMatrixStack.Compat.get()
                            matrixStack.translate(0f, 0f, 1f)
                            matrixStack.runWithGlobalState { event.slot highlight Color(15, 233, 233) }
                            matrixStack.translate(0f, 0f, -1f)
                        }
                    }
                }
            }
            if (chestName == "Ophelia" || chestName == "Trades" || chestName == "Booster Cookie") {
                if (Skytils.config.highlightDungeonSellableItems) {
                    if (event.slot.hasStack()) {
                        val stack = event.slot.stack
                        if (stack.name.string.containsAny(
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
            if (Skytils.config.combineHelper && equalsOneOf(
                    event.chestName,
                    "Anvil",
                    "Attribute Fusion"
                )
            ) {
                val item = event.container.getSlot(29).stack ?: return
                if (event.container.getSlot(33).hasStack()) return
                val candidate = event.slot.stack ?: return
                val nbt1 = getExtraAttributes(item) ?: return
                val nbt2 = getExtraAttributes(candidate) ?: return
                val tagName = when (getSkyBlockItemID(nbt1) to getSkyBlockItemID(nbt2)) {
                    "ENCHANTED_BOOK" to "ENCHANTED_BOOK" -> "enchantments"
                    "ATTRIBUTE_SHARD" to "ATTRIBUTE_SHARD" -> "attributes"
                    else -> return
                }
                val typeList = listOf(nbt1, nbt2).map { nbt ->
                    nbt.getCompound(tagName)
                }
                val tierList = typeList.mapNotNull { nbt ->
                    nbt.getOrNull()?.keys?.takeIf { it.size == 1 }?.first()
                }
                if (tierList.size != 2 || tierList[0] != tierList[1] || typeList[0].getOrNull()?.getInt(tierList[0])?.getOrNull() != typeList[1].getOrNull()?.getInt(
                        tierList[1]
                    )?.getOrNull()
                ) return

                event.slot highlight Color(17, 252, 243)
            }
        }
    }

    fun onSlotClick(event: GuiContainerSlotClickEvent) {
        if (!Utils.inSkyblock) return
        if (event.container is GenericContainerScreenHandler) {
            if (event.slot != null && event.slot!!.hasStack()) {
                val item = event.slot!!.stack ?: return
                val extraAttr = getExtraAttributes(item)
                if (Skytils.config.stopClickingNonSalvageable) {
                    if (event.chestName.startsWith("Salvage") && extraAttr != null) {
                        if (!extraAttr.contains("baseStatBoostPercentage") && !item.name.string.contains("Salvage") && !item.name.string.contains(
                                "Essence"
                            )
                        ) {
                            event.cancelled = true
                        }
                    }
                }
            }
        }
    }

    fun ontooltip(event: gg.skytils.event.impl.item.ItemTooltipEvent) {
        if (!Utils.inSkyblock) return
        val item = event.stack
        val extraAttr = getExtraAttributes(item)
        var itemId = getSkyBlockItemID(extraAttr)
        var isSuperpairsReward = false
        if (mc.player?.currentScreenHandler != null && SBInfo.lastOpenContainerName?.startsWith(
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
                        itemId = "ENCHANTED_BOOK-" + enchantName + "-" + item.count
                        isSuperpairsReward = true
                    }
                }
            }
        }
        if (itemId != null) {
            if (Skytils.config.showLowestBINPrice || Skytils.config.showCoinsPerBit || Skytils.config.showCoinsPerCopper || Skytils.config.showKuudraLowestBinPrice) {
                val auctionIdentifier = if (isSuperpairsReward) itemId else AuctionData.getIdentifier(item)
                if (auctionIdentifier != null) {
                    // this might actually have multiple items as the price
                    val valuePer = AuctionData.lowestBINs[auctionIdentifier]
                    if (valuePer != null) {
                        if (Skytils.config.showLowestBINPrice) {
                            val total =
                                if (isSuperpairsReward) NumberUtil.nf.format(valuePer) else NumberUtil.nf.format(
                                    valuePer * item.count
                                )
                            event.tooltip.add(
                                textComponent(
                                    "§6Lowest BIN Price: §b$total" + if (item.count > 1 && !isSuperpairsReward) " §7(" + NumberUtil.nf.format(
                                        valuePer
                                    ) + " each§7)" else ""
                                )
                            )
                        }
                        if (Skytils.config.showKuudraLowestBinPrice && item.count == 1) {
                            KuudraPriceData.getAttributePricedItemId(item)?.let {attrId ->
                                val kuudraPrice = KuudraPriceData.getOrRequestAttributePricedItem(attrId)
                                if (kuudraPrice != null) {
                                    if (kuudraPrice == KuudraPriceData.AttributePricedItem.EMPTY) {
                                        event.tooltip.add(textComponent("§6Kuudra BIN Price: §cNot Found"))
                                    } else {
                                        event.tooltip.add(
                                            textComponent("§6Kuudra BIN Price: §b${NumberUtil.nf.format(kuudraPrice.price)}")
                                        )
                                    }
                                } else {
                                    event.tooltip.add(textComponent("§6Kuudra BIN Price: §cLoading..."))
                                }
                            }
                        }
                        if (Skytils.config.showCoinsPerBit) {
                            var bitValue = bitCosts.getOrDefault(auctionIdentifier, -1)
                            if (bitValue == -1 && SBInfo.lastOpenContainerName == "Community Shop" || SBInfo.lastOpenContainerName?.startsWith(
                                    "Bits Shop - "
                                ) == true
                            ) {
                                val lore = getItemLore(item)
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
                            if (bitValue != -1) event.tooltip.add(textComponent("§6Coin/Bit: §b" + NumberUtil.nf.format(valuePer / bitValue)))
                        }
                        if (Skytils.config.showCoinsPerCopper) {
                            var copperValue = copperCosts.getOrDefault(auctionIdentifier, -1)
                            if (copperValue == -1 && SBInfo.lastOpenContainerName == "SkyMart") {
                                val lore = getItemLore(item)
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
                            if (copperValue != -1) event.tooltip.add(
                                textComponent("§6Coin/Copper: §c" + NumberUtil.nf.format(valuePer / copperValue))
                            )
                        }
                    }
                }
            }
            if (Skytils.config.showNPCSellPrice) {
                val valuePer = sellPrices[itemId]
                if (valuePer != null) event.tooltip.add(
                    textComponent("§6NPC Value: §b" + NumberUtil.nf.format(valuePer * item.count) + if (item.count > 1) " §7(" + NumberUtil.nf.format(
                        valuePer
                    ) + " each§7)" else "")
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
            for (i in event.tooltip.indices) {
                val line = event.tooltip[i]
                if (line.formattedText.contains("§7Crit Damage:")) {
                    event.tooltip.add(i + 1, textComponent("§8Radioactive Bonus: §c+${bonus}%"))
                    break
                }
            }
        }
        if (itemId == "PREHISTORIC_EGG" && extraAttr != null) {
            event.tooltip.add((event.tooltip.indexOfFirst { it.string.contains("Legendary Armadillo") } + 1),
                textComponent("§7Blocks Walked: §c${extraAttr.getInt("blocks_walked")}"))
        }
        if (Skytils.config.showGemstones && extraAttr?.contains("gems") == true) {
            val gems = extraAttr.getCompound("gems").getOrNull() ?: return
            event.tooltip.add(textComponent("§bGemstones: "))
            gems.keys.filterNot { it.endsWith("_gem") || it == "unlocked_slots" }.map {
                val quality = when (val tag: NbtElement? = gems.get(it)) {
                    is NbtCompound -> tag.getString("quality").getOrDefault("").toTitleCase().ifEmpty { "Report Unknown" }
                    is NbtString -> tag.asString().getOrNull()?.toTitleCase()
                    null -> "Report Issue"
                    else -> "Report Tag $tag"
                }
                "  §6- $quality ${
                    gems.getString("${it}_gem").getOrDefault("").ifEmpty { it.substringBeforeLast("_") }.toTitleCase()
                }"
            }.forEach {
                event.tooltip.add(textComponent(it))
            }
        }

        if (Skytils.config.showItemQuality && extraAttr != null) {
            val boost = extraAttr.getInt("baseStatBoostPercentage").getOrDefault(0)
            
            if (boost > 0) {
                val tier = extraAttr.getInt("item_tier").getOrDefault(-1)

                val req = extraAttr.getString("dungeon_skill_req").getOrDefault("")

                val floor: String = if (req.isEmpty() && tier == 0) "§aE" else if (req.isEmpty()) "§bF${tier}" else {
                    val (dungeon, level) = req.split(':', limit = 2)
                    val levelReq = level.toIntOrNull() ?: 0
                    if (dungeon == "CATACOMBS") {
                        if (levelReq - tier > 19) "§4M${tier-3}" else "§aF$tier"
                    } else {
                        "§b${dungeon} $tier"
                    }
                }

                val color = when {
                    boost <= 17 -> "§c"
                    boost <= 33 -> "§e"
                    boost <= 49 -> "§a"
                    else -> "§b"
                }

                event.tooltip.add(textComponent("§6Quality Bonus: $color+$boost% §7($floor§7)"))
            }
        }

        if (DevTools.getToggle("nbt") && UKeyboard.isKeyDown(UKeyboard.KEY_C) && UKeyboard.isCtrlKeyDown() && !UKeyboard.isShiftKeyDown() && !UKeyboard.isAltKeyDown()) {
            UDesktop.setClipboardString(ItemStack.CODEC.encodeStart(mc.player!!.registryManager.getOps(NbtOps.INSTANCE), event.stack).toString())
        }
    }

    fun onReceivePacket(event: MainThreadPacketReceiveEvent<*>) {
        if (!Utils.inSkyblock || mc.world == null) return
        event.packet.apply {
            if (this is ParticleS2CPacket) {
                if (type == ParticleTypes.EXPLOSION && Skytils.config.hideImplosionParticles) {
                    if (shouldForceSpawn() && count == 8 && speed == 8f && offsetX == 0f && offsetY == 0f && offsetZ == 0f) {
                        val dist = (if (DungeonFeatures.hasBossSpawned && dungeonFloorNumber == 7) 4f else 11f).pow(2f)

                        if (mc.world?.players?.any {
                                it.mainHandStack != null && it.uuid.version() == 4 && it.squaredDistanceTo(
                                    x,
                                    y,
                                    z
                                ) <= dist && getDisplayName(it.mainHandStack).stripControlCodes().containsAny(
                                    "Necron's Blade", "Scylla", "Astraea", "Hyperion", "Valkyrie"
                                )
                            } == true) {
                            event.cancelled = true
                        }
                    }
                }
            }
            if (this is ScreenHandlerSlotUpdateS2CPacket && syncId == 0) {
                val player = mc.player
                if (player == null || (!Utils.inSkyblock && player.age > 1)) return

                val item = stack ?: return
                val extraAttr = getExtraAttributes(item) ?: return
                val itemId = getSkyBlockItemID(extraAttr) ?: return

                if (equalsOneOf(itemId, "SOULFLOW_PILE", "SOULFLOW_BATTERY", "SOULFLOW_SUPERCELL")) {
                    getItemLore(item).find {
                        it.startsWith("§7Internalized: ")
                    }?.substringAfter("§7Internalized: ")?.let { s ->
                        soulflowState.set { s }
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
            if (this is EntityTrackerUpdateS2CPacket && lastShieldClick != -1L && id() == mc.player?.id && System.currentTimeMillis() - lastShieldClick <= 500 && trackedValues?.any { it.id() == 17 } == true) {
                lastShieldUseState.set { Instant.now() }
                lastShieldClick = -1
            }
        }
    }

    fun onSendPacket(event: PacketSendEvent<*>) {
        if (!Utils.inSkyblock || lastShieldUseState.getUntracked().isBefore(Instant.now()) || mc.player?.mainHandStack == null) return
        if (event.packet is PlayerInteractBlockC2SPacket &&
            mc.player?.mainHandStack?.item == Items.IRON_SWORD &&
            getExtraAttributes(mc.player?.mainHandStack)
                ?.getList("ability_scroll")
                ?.getOrNull() // String
                ?.asStringSet()
                //#if MC>12000
                ?.mapNotNull(Optional<String>::getOrNull)
                //#endif
                ?.contains("WITHER_SHIELD_SCROLL") == true
        ) {
            lastShieldClick = System.currentTimeMillis()
        }
    }

    fun onEntitySpawn(event: gg.skytils.event.impl.entity.EntityJoinWorldEvent) {
        if (!Utils.inSkyblock) return
        if (event.entity !is FishingBobberEntity || !Skytils.config.hideFishingHooks) return
        if ((event.entity as FishingBobberEntity).owner is OtherClientPlayerEntity) {
            (event.entity as FishingBobberEntity).remove(Entity.RemovalReason.DISCARDED)
            event.cancelled = true
        }
    }

    fun onInteract(event: BlockInteractEvent) {
        if (!Utils.inSkyblock) return
        val item = event.item
        val itemId = getSkyBlockItemID(item) ?: return
        if (Skytils.config.preventPlacingWeapons && (equalsOneOf(
                itemId,
                "FLOWER_OF_TRUTH",
                "BOUQUET_OF_LIES",
                "MOODY_GRAPPLESHOT",
                "BAT_WAND",
                "STARRED_BAT_WAND",
                "WEIRD_TUBA",
                "WEIRDER_TUBA",
                "PUMPKIN_LAUNCHER",
                "FIRE_FREEZE_STAFF",
                "GEMSTONE_GAUNTLET"
            ))
        ) {
            val block = mc.world?.getBlockState(event.pos) ?: return
            if (!isInteractable(block.block)) {
                event.cancelled = true
            }
        }
    }

    // In Yarn mappings, this is AbstractBlock#onUse
    private val onUseMethodName = FabricLoader.getInstance().mappingResolver.mapMethodName(
        "intermediary",
        "net.minecraft.class_4970",
        "method_55766",
        "(Lnet/minecraft/class_2680;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;Lnet/minecraft/class_1657;Lnet/minecraft/class_3965;)Lnet/minecraft/class_1269;"
    )

    private val interactableBlockCache = WeakHashMap<Class<out Block>, Boolean>()

    private fun isInteractable(block: Block): Boolean {
        if (Utils.inDungeons && (block === Blocks.COAL_BLOCK || block === Blocks.RED_TERRACOTTA)) {
            return true
        }

        val clazz = block.javaClass

        // If the block has its own onUse method that overrides the one in AbstractBlock, it is interactable
        return interactableBlockCache.getOrPut(clazz) {
            clazz.declaredMethods.any { it.name == onUseMethodName }
        }
    }

    fun onRenderItemOverlayPost(event: GuiContainerPostDrawSlotEvent) {
        val item = event.slot.stack ?: return
        if (!Utils.inSkyblock || item.count != 1 || item.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.contains("SkytilsNoItemOverlay") == true) return
        val matrixStack = UMatrixStack()
        DrawHelper.setupContainerScreenTransformations(matrixStack, aboveItems = true)
        var stackTip = ""
        val lore = getItemLore(item).takeIf { it.isNotEmpty() }
        getExtraAttributes(item)?.let { extraAttributes ->
            val itemId = getSkyBlockItemID(extraAttributes)
            if (Skytils.config.showPotionTier && extraAttributes.contains("potion_level")) {
                extraAttributes.getInt("potion_level").getOrNull()?.toString()?.let {
                    stackTip = it
                }
            } else if (Skytils.config.showAttributeShardLevel && itemId == "ATTRIBUTE_SHARD") {
                extraAttributes.getCompound("attributes").getOrNull()?.takeUnless {
                    it.isEmpty
                }?.let {
                    /*
                    If they ever add the ability to combine attributes on shards, this will need to be updated to:
                    stackTip = it.keySet.maxOf { s -> it.getInteger(s) }.toString()
                    */
                    it.getInt(it.keys.first()).getOrNull()?.toString()?.let {
                        stackTip = it
                    }
                }
            } else if ((Skytils.config.showEnchantedBookTier || Skytils.config.showEnchantedBookAbbreviation) && itemId == "ENCHANTED_BOOK") {
                extraAttributes.getCompound("enchantments").getOrNull()?.takeIf {
                    it.keys.size == 1
                }?.let { enchantments ->
                    val name = enchantments.keys.first()
                    if (Skytils.config.showEnchantedBookAbbreviation) {
                        val enchant = EnchantUtil.enchants.find { it.nbtName == name }
                        val prefix: String = if (enchant != null) {
                            val parts = enchant.loreName.split(" ")
                            val joined = if (parts.size > 1) parts.joinToString("") { it[0].uppercase() }
                                else if (parts.first().startsWith("Turbo-")) "${
                                    parts.first().split("-")[1].take(3).toTitleCase()
                                }."
                                else "${
                                    parts.first().take(3).toTitleCase()
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
                        matrixStack.push()
                        matrixStack.translate(event.slot.x.toFloat(), event.slot.y.toFloat(), 1f)
                        matrixStack.scale(0.8, 0.8, 1.0)
                        UGraphics.drawString(matrixStack, prefix, 0f, 0f, Color.WHITE.rgb, false)
                        matrixStack.pop()
                    }
                    if (Skytils.config.showEnchantedBookTier) {
                        enchantments.getInt(name).getOrNull()?.toString()?.let {
                            stackTip = it
                        }
                    }
                }
            } else if (Skytils.config.showHeadFloorNumber && item.item === Items.PLAYER_HEAD && headPattern.matches(
                    itemId ?: ""
                )
            ) {
                stackTip = headPattern.matchEntire(itemId!!)?.groups?.indexOfLast { it != null }.toString()
            } else if (Skytils.config.showStarCount && ItemUtil.getStarCount(extraAttributes) > 0) {
                stackTip = ItemUtil.getStarCount(extraAttributes).toString()
            }
            if (extraAttributes.contains("pickonimbus_durability")) {
                val durability = extraAttributes.getInt("pickonimbus_durability").getOrNull() ?: return
                /*
                Old Pickonimbuses had 5000 durability. If they were at full durability, they were nerfed to 2000.
                However, if they were not at full durability, they were left alone. Therefore, it's not really
                possible to check the true max durability.
                */
                if (durability < 2000) {
                    RenderUtil.drawDurabilityBar(
                        event.slot.x,
                        event.slot.y,
                        1 - durability / 2000.0
                    )
                }
            }
            if (Skytils.config.showAttributeShardAbbreviation && itemId == "ATTRIBUTE_SHARD" && extraAttributes.getCompound(
                    "attributes"
                ).getOrNull()?.keys?.size == 1
            ) {
                lore?.getOrNull(0)?.split(' ')?.dropLastWhile { it.romanToDecimal() == 0 }?.dropLast(1)
                    ?.joinToString(separator = "") {
                        if (it.startsWith('§'))
                            it.substring(0, 2) + it[2].uppercase()
                        else
                            it[0].uppercase()
                    }?.let { attribute ->
                        matrixStack.push()
                        matrixStack.translate(event.slot.x.toFloat(), event.slot.y.toFloat(), 1f)
                        matrixStack.scale(0.8, 0.8, 1.0)
                        UGraphics.drawString(matrixStack, attribute, 0f, 0f, Color.WHITE.rgb, false)
                        matrixStack.pop()
                    }
            }
            if (Skytils.config.showNYCakeYear && extraAttributes.contains("new_years_cake")) {
                stackTip = extraAttributes.getInt("new_years_cake").toString()
            }
        }
        if (Skytils.config.showPetCandies && item.item === Items.PLAYER_HEAD) {
            val petInfoString = getExtraAttributes(item)?.getString("petInfo")?.getOrNull()
            if (!petInfoString.isNullOrBlank()) {
                val petInfo = json.decodeFromString<Pet>(petInfoString)
                val level = petInfo.level
                val maxLevel = if (petInfo.type == "GOLDEN_DRAGON") 200 else 100

                if (petInfo.candyUsed > 0 && (SuperSecretSettings.alwaysShowPetCandy || level != maxLevel)) {
                    stackTip = petInfo.candyUsed.toString()
                }
            }
        }
        if (stackTip.isNotEmpty()) {
            matrixStack.push()
            matrixStack.translate(event.slot.x + 17f - UGraphics.getStringWidth(stackTip), event.slot.y + 9f, 0f)
            UGraphics.drawString(matrixStack, stackTip, 0f, 0f, Color.WHITE.rgb, true)
            matrixStack.pop()
        }
    }

    fun onDrawContainerForeground(event: GuiContainerForegroundDrawnEvent) {
        if (!Skytils.config.combineHelper || !Utils.inSkyblock) return
        if (event.container !is GenericContainerScreenHandler || !equalsOneOf(
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
        val typeList = listOf(nbt1, nbt2).mapNotNull { nbt ->
            nbt.getCompound(tagName).getOrNull()
        }
        val tierList = typeList.mapNotNull { nbt ->
            nbt.keys.takeIf { it.size == 1 }?.first()
        }
        if (tierList.size != 2) return
        val errorString = if (tierList[0] != tierList[1]) {
            "Types don't match!"
        } else if (typeList[0].getInt(tierList[0]) != typeList[1].getInt(tierList[1])) {
            "Tiers don't match!"
        } else return
        val gui =
            event.gui as AccessorGuiContainer
        UGraphics.disableLighting()
        UGraphics.disableBlend()
        UGraphics.disableDepth()
        val matrixStack = UMatrixStack.Compat.get()
        val vertexConsumer = UMinecraft.getMinecraft().bufferBuilders.entityVertexConsumers
        UMinecraft.getMinecraft().textRenderer.draw(
            errorString,
            gui.xSize / 2f, 22.5f,
            Color.RED.rgb,
            false,
            matrixStack.peek().model,
            vertexConsumer,
            TextRenderer.TextLayerType.NORMAL,
            0, 15728880
        )
        vertexConsumer.draw()
        UGraphics.enableDepth()
        UGraphics.enableBlend()
        UGraphics.enableLighting()
    }

    fun onRenderWorld(event: WorldDrawEvent) {
        if (!Utils.inSkyblock) return
        val player = mc.player ?: return
        if (Skytils.config.showEtherwarpTeleportPos && player.isSneaking == true) {
            val extraAttr = getExtraAttributes(player.mainHandStack) ?: return
            if (!extraAttr.getBoolean("ethermerge").getOrDefault(false)) return
            val dist = 57.0 + extraAttr.getInt("tuned_transmission").getOrDefault(0)
            val vec3 = player.getCameraPosVec(event.partialTicks)
            val vec31 = player.getRotationVec(event.partialTicks)
            val vec32 = vec3.add(
                vec31.x * dist,
                vec31.y * dist,
                vec31.z * dist
            )
            val obj = mc.world?.raycast(BlockStateRaycastContext(vec3, vec32, { !it.isAir })) ?: return
            val block = obj.blockPos ?: return
            val state = mc.world?.getBlockState(block) ?: return
            if (isValidEtherwarpPos(obj)) {
                RenderUtil.drawSelectionBox(
                    block,
                    Skytils.config.showEtherwarpTeleportPosColor,
                    event.partialTicks
                )
            }
        }
    }

    private fun isValidEtherwarpPos(obj: BlockHitResult): Boolean {
        val pos = obj.blockPos
        val sideHit = obj.side
        val world = mc.world ?: return false

        return world.getBlockState(pos)?.isSolid == true && (1..2).all {
            val newPos = pos.up(it)
            val newBlock = world.getBlockState(newPos)
            if (sideHit === Direction.UP && (equalsOneOf(
                    newBlock.block,
                    Blocks.FIRE,
                    Blocks.PLAYER_HEAD
                ) || newBlock.block is FluidBlock)
            ) return@all false
            if (sideHit !== Direction.UP && newBlock.block is AbstractSignBlock) return@all false
            if (newBlock.block is LadderBlock || newBlock.block is DoorBlock) return@all false
            return@all !newBlock.getCollisionShape(mc.world, newPos).isEmpty
        }
    }

    class StackingEnchantHud : HudElement("Stacking Enchant Display", 0.65, 0.85) {
        override val toggleState: State<Boolean>
            get() = Skytils.config.stackingEnchantProgressDisplay
        override fun LayoutScope.render() {
            text(stackingEnchantTextState)
        }

        override fun LayoutScope.demoRender() {
            text("Expertise 10: Maxed")
        }

    }

    class SoulflowHud : HudElement("Soulflow Display", 0.65, 0.85) {
        override val toggleState: State<Boolean>
            get() = Skytils.config.showSoulflowDisplay
        override fun LayoutScope.render() {
            text(soulflowState)
        }

        override fun LayoutScope.demoRender() {
            text("§3100⸎ Soulflow")
        }

    }

    class WitherShieldHud : HudElement("Wither Shield Display", 0.65, 0.85) {
        override val toggleState: State<Boolean>
            get() = Skytils.config.witherShieldCooldown
        override fun LayoutScope.render() {
            text(stateUsingSystemTime { time ->
                val lastShieldUse = lastShieldUseState()
                val cooldown = if (Skytils.config.assumeWitherImpactState()) 5L else 10L
                val cooldownExpiration = lastShieldUse.plusSeconds(cooldown)
                if (time.isBefore(cooldownExpiration)) {
                    val diff = time.until(cooldownExpiration).getValue().toMillis() / 1000f
                    "Shield: §c${"%.2f".format(diff)}s"
                } else {
                    "Shield: §aREADY"
                }
            }, Modifier.color(Color(0xff9000)))
        }

        override fun LayoutScope.demoRender() {
            text("§6Shield: §aREADY")
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
