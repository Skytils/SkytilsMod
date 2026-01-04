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

import com.mojang.blaze3d.opengl.GlStateManager
import gg.essential.elementa.unstable.layoutdsl.LayoutScope
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UChat
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.impl.entity.BossBarSetEvent
import gg.skytils.event.impl.entity.EntityJoinWorldEvent
import gg.skytils.event.impl.item.ItemTooltipEvent
import gg.skytils.event.impl.play.ChatMessageReceivedEvent
import gg.skytils.event.impl.play.ChatMessageSentEvent
import gg.skytils.event.impl.render.CheckRenderEntityEvent
import gg.skytils.event.impl.render.ItemOverlayPostRenderEvent
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.impl.screen.GuiContainerPreDrawSlotEvent
import gg.skytils.event.impl.screen.GuiContainerSlotClickEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.Skytils.prefix
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod._event.RenderHUDEvent
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.core.GuiManager.createTitle
import gg.skytils.skytilsmod.core.structure.v2.HudElement
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.gui.layout.text
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEntityArmorStand
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import gg.skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import gg.skytils.skytilsmod.utils.NumberUtil.romanToDecimal
import gg.skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.Utils.equalsOneOf
import gg.skytils.skytilsmod.utils.multiplatform.SlotActionType
import net.minecraft.block.EndPortalFrameBlock
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.unstable.layoutdsl.Modifier
import gg.essential.elementa.unstable.layoutdsl.color
import gg.essential.elementa.unstable.layoutdsl.fillHeight
import gg.essential.elementa.unstable.layoutdsl.height
import gg.essential.elementa.unstable.layoutdsl.row
import gg.essential.elementa.unstable.layoutdsl.widthAspect
import gg.essential.elementa.unstable.state.v2.*
import gg.essential.elementa.unstable.state.v2.combinators.and
import gg.essential.elementa.unstable.state.v2.combinators.map
import gg.essential.universal.UDesktop
import gg.essential.universal.UKeyboard
import gg.skytils.skytilsmod.gui.components.ItemComponent
import gg.skytils.skytilsmod.utils.multiplatform.EquipmentSlot
import net.minecraft.client.option.KeyBinding
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.LightningEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.block.Blocks
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.text.Text
import java.awt.Color
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

object MiscFeatures : EventSubscriber {
    private val golemSpawnTimeState: MutableState<Instant?> = mutableStateOf(null)
    val playerInRangeCountState = mutableStateOf(0)
    val placedEyesState = mutableStateOf(0)
    val dayState = mutableStateOf(-1)
    private var lastGLeaveCommand = 0L
    private var lastCoopAddCommand = 0L
    private val cheapCoins = setOf(
        "ewogICJ0aW1lc3RhbXAiIDogMTcxOTYwMDMwOTQ4MywKICAicHJvZmlsZUlkIiA6ICI1OTgyOWY1ZGY3MmM0ZmFlOTBmOGVhYmM0MjFjMzJkYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQZXBwZXJEcmlua2VyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE2YjkwZjRmYTNlYzEwNmJmZWYyMWYzYjc1ZjU0MWExOGU0NzU3Njc0ZjdkNTgyNTBmYTdlNzQ5NTJmMDg3ZGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        "eyJ0aW1lc3RhbXAiOjE1NjAwMzYyODI5MTcsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZGVlNjIxZWI4MmIwZGFiNDE2NjMzMGQxZGEwMjdiYTJhYzEzMjQ2YTRjMWU3ZDUxNzRmNjA1ZmRkZjEwYTEwIn19fQ==",
        "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg2ODk5MTUyNCwKICAicHJvZmlsZUlkIiA6ICIxMTM1Njg1ZTk3ZGE0ZjYyYTliNDQ3MzA0NGFiZjQ0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXJpb1dsZXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2NiY2NlMjJhZjU1OWVkNmJhNjAzODg0NWRiMzhjY2JjYTJlNjJiNzdiODdhMjZhMDY2NTcxMDljZTBlZmJhNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        "ewogICJ0aW1lc3RhbXAiIDogMTU5ODg0NzA4MjYxMywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQwZDZlMzYyYmM3ZWVlNGY5MTFkYmQwNDQ2MzA3ZTc0NThkMTA1MGQwOWFlZTUzOGViY2IwMjczY2Y3NTc0MiIKICAgIH0KICB9Cn0=",
    )
    private val hubSpawnPoint = BlockPos(-2, 70, -69)
    private val bestiaryTitleRegex = Regex("(?:\\(\\d+/\\d+\\) )?(?:Bestiary ➜ (?!Fishing)|Fishing ➜ )|Search Results")

    init {
        Skytils.guiManager.registerElement(GolemSpawnTimerHud())
        Skytils.guiManager.registerElement(PlayersInRangeHud())
        Skytils.guiManager.registerElement(PlacedSummoningEyeHud())
        Skytils.guiManager.registerElement(WorldAgeHud())
        Skytils.guiManager.registerElement(ItemNameHighlightDummy)
        Skytils.guiManager.registerElement(ActionBarDummy)
    }

    override fun setup() {
        register(::onSendChatMessage)
        register(::onBossBarSet)
        register(::onChat, EventPriority.Highest)
        register(::onCheckRender)
        register(::onDrawSlot)
        register(::onJoin)
        register(::onRenderHud, EventPriority.Highest)
        register(::onReceivePacket)
        register(::onSlotClick, EventPriority.Highest)
        register(::onTooltip)
        register(::onSlotClickLow, EventPriority.Lowest)
        register(::onTick)
        register(::onRenderItemOverlayPost)
    }

    fun onSendChatMessage(event: ChatMessageSentEvent) {
        if (!Utils.isOnHypixel) return
        if (Skytils.config.guildLeaveConfirmation && event.message.startsWith("/g leave") && System.currentTimeMillis() - lastGLeaveCommand >= 10_000) {
            event.cancelled = true
            lastGLeaveCommand = System.currentTimeMillis()
            UChat.chat("$failPrefix §cSkytils stopped you from using leaving your guild! §6Run the command again if you wish to leave!")
        }
        if (Skytils.config.coopAddConfirmation && event.message.startsWith("/coopadd ") && System.currentTimeMillis() - lastCoopAddCommand >= 10_000) {
            event.cancelled = true
            lastCoopAddCommand = System.currentTimeMillis()
            UChat.chat("$failPrefix §c§lBe careful! Skytils stopped you from giving a player full control of your island! §6Run the command again if you are sure!")
        }
    }

    fun onBossBarSet(event: BossBarSetEvent) {
        val displayData = event.data
        if (Utils.inSkyblock) {
            if (Skytils.config.bossBarFix && equalsOneOf(
                    displayData.name.string.stripControlCodes(),
                    "Wither",
                    "Dinnerbone",
                    "Grumm",
                    "Ender Dragon"
                )
            ) {
                event.cancelled = true
                return
            }
        }
    }

    fun onChat(event: ChatMessageReceivedEvent) {
        if (!Utils.inSkyblock) return
        val unformatted = event.message.string.stripControlCodes().trim()
        val formatted = event.message.formattedText
        if (formatted.startsWith("§r§cYou died") && Skytils.config.preventMovingOnDeath) {
            KeyBinding.unpressAll()
        }
        if (unformatted == "The ground begins to shake as an Endstone Protector rises from below!") {
            golemSpawnTimeState.set { Instant.now().plus(Duration.ofSeconds(20)) }
        }

        // TODO: Move to dungeons related class
        if (!Utils.inDungeons) {
            if (Skytils.config.copyDeathToClipboard) {
                if (formatted.startsWith("§r§c ☠ ")) {
                    event.message.style
                        .withHoverEvent(
                            HoverEvent.ShowText(Text.literal("§aClick to copy to clipboard."))
                        ).withClickEvent(ClickEvent.RunCommand("/skytilscopy $unformatted"))
                }
            }
        }
        if (Skytils.config.autoCopyRNGDrops) {
            if (formatted.startsWith("§r§d§lCRAZY RARE DROP! ") || formatted.startsWith("§r§c§lINSANE DROP! ") || formatted.startsWith(
                    "§r§6§lPET DROP! "
                ) || formatted.contains(" §r§ehas obtained §r§6§r§7[Lvl 1]")
            ) {
                UDesktop.setClipboardString(unformatted)
                UChat.chat("$prefix §aCopied RNG drop to clipboard.")
                event.message.style
                    .withHoverEvent(
                        HoverEvent.ShowText(Text.literal("§aClick to copy to clipboard."))
                    ).withClickEvent(ClickEvent.RunCommand("/skytilscopy $unformatted"))
            }
        }
        if (Skytils.config.autoCopyVeryRareDrops) {
            if (formatted.startsWith("§r§9§lVERY RARE DROP! ") || formatted.startsWith("§r§5§lVERY RARE DROP! ")) {
                UDesktop.setClipboardString(unformatted)
                UChat.chat("$prefix §aCopied very rare drop to clipboard.")
                event.message.style
                    .withHoverEvent(
                        HoverEvent.ShowText(Text.literal("§aClick to copy to clipboard."))
                    ).withClickEvent(ClickEvent.RunCommand("/skytilscopy $unformatted"))
            }
        }
    }

    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!Utils.inSkyblock) return
        // TODO: ensure this works properly
        if (Skytils.config.bossBarFix && event.entity is WitherEntity && event.entity.isInvisible && event.entity.hasCustomName()) {
            return
        } else if (Skytils.config.hideDyingMobs && event.entity is LivingEntity && ((event.entity as LivingEntity).health <= 0 || event.entity.isRemoved)) {
            event.cancelled = true
        } else if (event.entity is FallingBlockEntity) {
            val entity = event.entity as FallingBlockEntity
            if (Skytils.config.hideMidasStaffGoldBlocks && entity.blockState.block === Blocks.GOLD_BLOCK) {
                event.cancelled = true
            }
        } else if (event.entity is ItemEntity) {
            val entity = event.entity as ItemEntity
            val item = entity.stack
            if (Skytils.config.hideJerryRune) {
                if (item.item == Items.VILLAGER_SPAWN_EGG){
                    event.cancelled = true
                }
            }
            if (Skytils.config.hideCheapCoins && cheapCoins.contains(ItemUtil.getSkullTexture(item))) {
                event.cancelled = true
            }
        } else if (event.entity is LightningEntity) {
            if (Skytils.config.hideLightning) {
                event.cancelled = true
            }
        } else if (event.entity is OtherClientPlayerEntity) {
            if (Skytils.config.hidePlayersInSpawn && event.entity.blockPos == hubSpawnPoint && SBInfo.mode == SkyblockIsland.Hub.mode) {
                event.cancelled = true
            }
        } else if (Skytils.deobfEnvironment && DevTools.getToggle("invis")) {
            event.entity.isInvisible = false
            (event.entity as? AccessorEntityArmorStand)?.invokeSetShowArms(true)
        }
    }

    fun onDrawSlot(event: GuiContainerPreDrawSlotEvent) {
        if (!Utils.inSkyblock || event.container !is GenericContainerScreenHandler) return
        val item = event.slot.stack ?: return
        if (Skytils.config.highlightDisabledPotionEffects && event.chestName.startsWith("Toggle Potion Effects")) {
            if (item.item == Items.POTION) {
                if (ItemUtil.getItemLore(item).any {
                        it == "§7Currently: §cDISABLED"
                    }) {
                    event.slot highlight Color(255, 0, 0, 80)
                }
            }
        }
        // (Your|Co-op Bazaar Orders)
        if (Skytils.config.highlightFilledBazaarOrders && event.chestName.endsWith(" Bazaar Orders")) {
            val filled =
                ItemUtil.getItemLore(item).find { it.startsWith("§7Filled: §") }?.endsWith(" §a§l100%!") ?: false
            if (filled) event.slot highlight Color(0, 255, 0, 80)
        }
    }

    private val trickOrTreat = UUID.fromString("f955b4ac-0c41-3e45-8703-016c46a8028e")
    fun onJoin(event: EntityJoinWorldEvent) {
        val player = mc.player
        if (!Utils.inSkyblock || player == null || mc.world == null) return
        if (event.entity is ArmorStandEntity) {
            tickTimer(5) {
                val entity = event.entity as ArmorStandEntity
                val headSlot = entity.getEquippedStack(EquipmentSlot.HEAD)
                val headUUID = headSlot?.get(DataComponentTypes.PROFILE)?.gameProfile?.id ?: return@tickTimer
                if (Skytils.config.trickOrTreatChestAlert && mc.player != null && headSlot.item === Items.PLAYER_HEAD && entity.squaredDistanceTo(
                        mc.player
                    ) < 10 * 10
                ) {
                    if (headUUID == trickOrTreat) {
                        createTitle("§cTrick or Treat!", 60)
                    }
                }
            }
        }
    }

    fun onRenderHud(event: RenderHUDEvent) {
        val player = mc.player
        if (!Utils.inSkyblock || player == null || Skytils.config.lowHealthVignetteThreshold == 0.0f) return
        val healthPercentage = (player.health + player.absorptionAmount) / player.baseMaxHealth
        if (healthPercentage < Skytils.config.lowHealthVignetteThreshold) {
            val color =
                Skytils.config.lowHealthVignetteColor.withAlpha((Skytils.config.lowHealthVignetteColor.alpha * (1.0 - healthPercentage)).toInt())

            PatcherCompatability.disableHUDCaching = true
            RenderUtil.drawVignette(color)
        } else PatcherCompatability.disableHUDCaching = false
    }

    fun onReceivePacket(event: PacketReceiveEvent<*>) {
        if (!Utils.inSkyblock) return
        if (event.packet is PlaySoundS2CPacket) {
            val packet = event.packet
            if (Skytils.config.disableCooldownSounds && packet.sound.value() == SoundEvents.ENTITY_ENDERMAN_TELEPORT && packet.pitch == 0f && packet.volume == 8f) {
                event.cancelled = true
                return
            }
            if (Skytils.config.disableJerrygunSounds) {
                when (packet.sound.value()) {
                    SoundEvents.ENTITY_VILLAGER_YES -> if (packet.volume == 0.35f) {
                        event.cancelled = true
                        return
                    }

                    SoundEvents.ENTITY_VILLAGER_TRADE -> if (packet.volume == 0.5f) {
                        event.cancelled = true
                        return
                    }
                }
            }
            if (Skytils.config.disableTruthFlowerSounds && packet.sound.value() == SoundEvents.ENTITY_GENERIC_EAT.value() && packet.pitch == 0.6984127f && packet.volume == 1.0f) {
                event.cancelled = true
                return
            }
        }
    }

    fun onSlotClick(event: GuiContainerSlotClickEvent) {
        if (!Utils.inSkyblock) return
        if (event.container is GenericContainerScreenHandler) {
            val slot = event.slot ?: return
            val item = slot.stack ?: return
            if (Skytils.config.coopAddConfirmation && item.item == Items.DIAMOND && item.name.formattedText == "§aCo-op Request") {
                event.cancelled = true
                UChat.chat("$failPrefix §c§lBe careful! Skytils stopped you from giving a player full control of your island!")
            }
            val extraAttributes = getExtraAttributes(item)
            if (event.chestName == "Ophelia") {
                if (Skytils.config.dungeonPotLock > 0) {
                    if (slot.inventory === mc.player?.inventory || equalsOneOf(slot.id, 49, 53)) return
                    if (item.item !== Items.POTION || extraAttributes == null || !extraAttributes.contains("potion_level")) {
                        event.cancelled = true
                        return
                    }
                    if (extraAttributes.getInt("potion_level").getOrDefault(0) != Skytils.config.dungeonPotLock) {
                        event.cancelled = true
                        return
                    }
                }
            }
        }
    }

    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inSkyblock) return
        if (!Skytils.config.hideTooltipsOnStorage) return
        val currentScreen = mc.currentScreen
        if (currentScreen is GenericContainerScreen) {
            val chestName = currentScreen.title.string
            if (chestName.equals("Storage") && ItemUtil.getDisplayName(event.stack).containsAny(
                    "Backpack",
                    "Ender Chest",
                    "Locked Page"
                )) {
                event.tooltip.clear()
            }
        }
    }

    fun onSlotClickLow(event: GuiContainerSlotClickEvent) {
        if (!Utils.inSkyblock || !Skytils.config.middleClickGUIItems) return
        if (event.clickedButton != 0 || event.clickType != 0 || event.container !is GenericContainerScreenHandler || event.slot == null || !event.slot!!.hasStack()) return
        val chestName = event.gui.title.string
        val item = event.slot!!.stack

        if (equalsOneOf(
                chestName,
                "Anvil",
                "Chest",
                "Large Chest",
                "Storage",
                "Enchant Item",
                "Drill Anvil",
                "Runic Pedestal",
                "Reforge Anvil",
                "Rune Removal",
                "Reforge Item",
                "Exp Sharing",
                "Offer Pets",
                "Upgrade Item",
                "Convert to Dungeon Item",
            )
        ) return

        if (event.slot?.inventory === mc.player?.inventory || UKeyboard.isCtrlKeyDown()) return
        if (chestName.startsWithAny("Wardrobe")) return

        if (getSkyBlockItemID(item) == null) {
            if (chestName.contains("Minion") && equalsOneOf(
                    item.name,
                    "§aMinion Skin Slot",
                    "§aFuel",
                    "§aAutomated Shipping",
                    "§aUpgrade Slot"
                )
            ) return
            if (chestName == "Beacon"
                && item.isOf(Items.FURNACE) && item.name.formattedText == "§6Beacon Power") return
            if (chestName.startsWithAny(
                    "Salvage Item"
                ) && item.isOf(Items.BEACON) && item.name.formattedText == "§aSalvage Items"
            ) return
            if (ItemUtil.getItemLore(item).asReversed().any {
                    it.contains("-click", true)
                }) return
            event.cancelled = true
            mc.interactionManager?.clickSlot(event.container.syncId, event.slotId, 2, SlotActionType.CLONE, mc.player)
        }
    }

    fun onTick(event: TickEvent) {
        if (!Utils.inSkyblock || mc.player == null) return
        val world = mc.world ?: return

        if (Skytils.config.playersInRangeDisplay.getUntracked()) {
            playerInRangeCountState.set {
                (world.players.filterIsInstance<OtherClientPlayerEntity>().count {
                    (it.uuid.version() == 4 || it.uuid.version() == 1) && it.squaredDistanceTo(mc.player) <= 30 * 30 // Nicked players have uuid v1, Watchdog has uuid v4
                } - 1).coerceAtLeast(0) // The -1 is to remove watchdog from the list
            }
        }
        if (Skytils.config.summoningEyeDisplay.getUntracked() && SBInfo.mode == SkyblockIsland.TheEnd.mode) {
            placedEyesState.set {
                PlacedSummoningEyeHud.SUMMONING_EYE_FRAMES.count {
                    world.getBlockState(it).run {
                        block === Blocks.END_PORTAL_FRAME && this.get(EndPortalFrameBlock.EYE)
                    }
                }
            }
        }

        val day = mc.world?.timeOfDay?.div(24000)?.toInt() ?: -1
        dayState.set(day)
    }

    fun onRenderItemOverlayPost(event: ItemOverlayPostRenderEvent) {
        val item = event.stack
        if (!Utils.inSkyblock || item.count != 1) return
        val player = mc.player ?: return
        val itemNbt = event.stack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() ?: return
        if (itemNbt.contains("SkytilsNoItemOverlay")) return
        var stackTip = ""


        val screen = mc.currentScreen
        if (screen is GenericContainerScreen) {
            val name = screen.title.string
            if (Skytils.config.showBestiaryLevel && bestiaryTitleRegex in name) {
                val arrowSlot = screen.screenHandler.slots.getOrNull(48)?.stack
                if (arrowSlot != null && arrowSlot.item == Items.ARROW && ItemUtil.getItemLore(item)
                        .lastOrNull() == "§eClick to view!"
                ) {
                    var ending = ItemUtil.getDisplayName(item).substringAfterLast(" ", "")
                    if (ending.any { !it.isUpperCase() }) ending = ""
                    stackTip = ending.romanToDecimal().toString()
                }
            }
            if (Skytils.config.showDungeonFloorAsStackSize && name == "Catacombs Gate" && item.item === Items.PLAYER_HEAD) {
                stackTip =
                    getSkyBlockItemID(item)?.substringAfterLast("CATACOMBS_PASS_")?.toIntOrNull()?.minus(3)?.toString()
                        ?: ""
            }
        }

        if (stackTip.isNotBlank()) {
            // TODO: migrate this to using DrawContext
            // disable lighting
            GlStateManager._disableDepthTest()
            GlStateManager._disableBlend()
            UGraphics.drawString(
                UMatrixStack.Compat.get(),
                stackTip,
                (event.x + 17 - UGraphics.getStringWidth(stackTip)).toFloat(),
                (event.y + 9).toFloat(),
                16777215,
                true
            )
            // enable lighting
            GlStateManager._enableDepthTest()
        }
    }

    fun renderFishingHookAge(event: WorldDrawEvent) {
        if (Utils.inSkyblock && Config.fishingHookAge) {
            mc.world?.entities?.filterIsInstance<FishingBobberEntity>()?.filter { entity ->
                mc.player == entity.owner
            }?.forEach { entity ->
                RenderUtil.drawLabel(entity.pos.add(0.0, 0.5, 0.0), "%.2fs".format(entity.age / 20.0), Color.WHITE, event.partialTicks, UMatrixStack.Compat.get())
            }
        }
    }

    class GolemSpawnTimerHud : HudElement("Endstone Protector Spawn Timer", 150f, 20f) {
        override val toggleState: State<Boolean>
            get() = Skytils.config.golemSpawnTimer
        override fun LayoutScope.render() {
            if_(SBInfo.skyblockState) {
                ifNotNull(golemSpawnTimeState) { spawnTime ->
                    val diffText = State {
                        val diff = (withSystemTime { time -> time.until(spawnTime) }.toMillis().getValue() / 1000.0).roundToPrecision(1)
                        "§cGolem spawn in: §a${diff}s"
                    }
                    text(diffText)
                }
            }
        }

        override fun LayoutScope.demoRender() {
            text("§cGolem spawn in: §a20.0s")
        }

    }

    class PlayersInRangeHud : HudElement("Players In Range Display", 50f, 50f) {
        override val toggleState: State<Boolean>
            get() = Skytils.config.playersInRangeDisplay
        private val ENCHANTED_BOOK = ItemStack(Items.ENCHANTED_BOOK)
        private val stringState = playerInRangeCountState.map { it.toString() }

        override fun LayoutScope.render() {
            if_(SBInfo.skyblockState) {
                row(Modifier.height(16f)) {
                    ItemComponent(ENCHANTED_BOOK)(Modifier.fillHeight().widthAspect(1f))
                    text(stringState, Modifier.color(Color.ORANGE))
                }
            }
        }

        override fun LayoutScope.demoRender() {
            row(Modifier.height(16f)) {
                ItemComponent(ENCHANTED_BOOK)(Modifier.fillHeight().widthAspect(1f))
                text("69", Modifier.color(Color.ORANGE))
            }
        }
    }

    class PlacedSummoningEyeHud : HudElement("Placed Summoning Eye Display", 50f, 60f) {
        override val toggleState: State<Boolean>
            get() = Skytils.config.summoningEyeDisplay
        override fun LayoutScope.render() {
            if_(SBInfo.skyblockState and { SBInfo.modeState() == SkyblockIsland.TheEnd.mode }) {
                row(Modifier.height(16f)) {
                    UIImage.ofResource("/assets/skytils/icons/SUMMONING_EYE.png")(Modifier.fillHeight().widthAspect(1f))
                    text({"${placedEyesState()}/8"}, Modifier.color(Color.ORANGE))
                }
            }
        }

        override fun LayoutScope.demoRender() {
            row(Modifier.height(16f)) {
                UIImage.ofResource("/assets/skytils/icons/SUMMONING_EYE.png")(Modifier.fillHeight().widthAspect(1f))
                text("6/8", Modifier.color(Color.ORANGE))
            }
        }

        companion object {
            val SUMMONING_EYE_FRAMES = arrayOf(
                BlockPos(-669, 9, -275),
                BlockPos(-669, 9, -277),
                BlockPos(-670, 9, -278),
                BlockPos(-672, 9, -278),
                BlockPos(-673, 9, -277),
                BlockPos(-673, 9, -275),
                BlockPos(-672, 9, -274),
                BlockPos(-670, 9, -274)
            )
        }
    }

    class WorldAgeHud : HudElement("World Age Display", 50f, 60f) {
        override val toggleState: State<Boolean> = Skytils.config.showWorldAgeState

        val isUsingBaldTimeChanger = State {
            isTimechangerLoaded()
        }

        override fun LayoutScope.render() {
            if_(isUsingBaldTimeChanger) {
                text("Incompatible Time Changer detected.")
            } `else` {
                text({ "Day ${dayState()}" })
            }
        }

        override fun LayoutScope.demoRender() {
            if_(isUsingBaldTimeChanger) {
                text("Incompatible Time Changer detected.")
            } `else` {
                text("Day 0")
            }
        }

    }

    object ItemNameHighlightDummy : HudElement("Item Name Highlight", 50f, 60f) {
        override val toggleState: State<Boolean>
            get() = Skytils.config.moveableItemNameHighlight
        override fun LayoutScope.render() {
            /**
             * handled in [gg.skytils.skytilsmod.mixins.hooks.gui.modifyItemHighlightPosition]
             */
        }

        override fun LayoutScope.demoRender() {
            text("Item Name")
        }

    }

    object ActionBarDummy : HudElement("Action Bar", 50f, 70f) {
        override val toggleState: State<Boolean>
            get() = Skytils.config.moveableActionBar
        override fun LayoutScope.render() {
            /**
             * handled in [gg.skytils.skytilsmod.mixins.hooks.gui.modifyItemHighlightPosition]
             */
        }

        override fun LayoutScope.demoRender() {
            text("Action Bar")
        }

    }
}
