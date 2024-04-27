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

import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.core.GuiManager.createTitle
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.events.impl.*
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent.SlotClickEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEntityArmorstand
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorWorldInfo
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import gg.skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import gg.skytils.skytilsmod.utils.NumberUtil.romanToDecimal
import gg.skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.RenderUtil.renderItem
import gg.skytils.skytilsmod.utils.RenderUtil.renderTexture
import gg.skytils.skytilsmod.utils.Utils.equalsOneOf
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.block.BlockEndPortalFrame
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.projectile.EntityFishHook
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.item.ItemMonsterPlacer
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import kotlin.time.Duration.Companion.milliseconds

object MiscFeatures {
    private var golemSpawnTime: Long = 0
    var inRangePlayerCount = 0
    var placedEyes = 0
    private var lastGLeaveCommand = 0L
    private var lastCoopAddCommand = 0L
    private val cheapCoins = setOf(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0=",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZhMDg3ZWI3NmU3Njg3YTgxZTRlZjgxYTdlNjc3MjY0OTk5MGY2MTY3Y2ViMGY3NTBhNGM1ZGViNmM0ZmJhZCJ9fX0=",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjM2UzMWNmYzY2NzMzMjc1YzQyZmNmYjVkOWE0NDM0MmQ2NDNiNTVjZDE0YzljNzdkMjczYTIzNTIifX19",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RlZTYyMWViODJiMGRhYjQxNjYzMzBkMWRhMDI3YmEyYWMxMzI0NmE0YzFlN2Q1MTc0ZjYwNWZkZGYxMGExMCJ9fX0=",
        "ewogICJ0aW1lc3RhbXAiIDogMTU5ODg0NzA4MjYxMywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQwZDZlMzYyYmM3ZWVlNGY5MTFkYmQwNDQ2MzA3ZTc0NThkMTA1MGQwOWFlZTUzOGViY2IwMjczY2Y3NTc0MiIKICAgIH0KICB9Cn0=",
    )
    private val hubSpawnPoint = BlockPos(-2, 70, -69)
    private val bestiaryTitleRegex = Regex("(?:\\(\\d+/\\d+\\) )?(?:Bestiary ➜ (?!Fishing)|Fishing ➜ )|Search Results")

    init {
        GolemSpawnTimerElement()
        PlayersInRangeDisplay()
        PlacedSummoningEyeDisplay()
        WorldAgeDisplay()
    }

    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        if (!Utils.isOnHypixel) return
        if (Skytils.config.guildLeaveConfirmation && event.message.startsWith("/g leave") && System.currentTimeMillis() - lastGLeaveCommand >= 10_000) {
            event.isCanceled = true
            lastGLeaveCommand = System.currentTimeMillis()
            UChat.chat("$failPrefix §cSkytils stopped you from using leaving your guild! §6Run the command again if you wish to leave!")
        }
        if (Skytils.config.coopAddConfirmation && event.message.startsWith("/coopadd ") && System.currentTimeMillis() - lastCoopAddCommand >= 10_000) {
            event.isCanceled = true
            lastCoopAddCommand = System.currentTimeMillis()
            UChat.chat("$failPrefix §c§lBe careful! Skytils stopped you from giving a player full control of your island! §6Run the command again if you are sure!")
        }
    }

    @SubscribeEvent
    fun onBossBarSet(event: BossBarEvent.Set) {
        val displayData = event.displayData
        if (Utils.inSkyblock) {
            if (Skytils.config.bossBarFix && equalsOneOf(
                    displayData.displayName.unformattedText.stripControlCodes(),
                    "Wither",
                    "Dinnerbone",
                    "Grumm",
                    "Ender Dragon"
                )
            ) {
                event.isCanceled = true
                return
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return
        val unformatted = event.message.unformattedText.stripControlCodes().trim()
        val formatted = event.message.formattedText
        if (formatted.startsWith("§r§cYou died") && Skytils.config.preventMovingOnDeath) {
            KeyBinding.unPressAllKeys()
        }
        if (unformatted == "The ground begins to shake as an Endstone Protector rises from below!") {
            golemSpawnTime = System.currentTimeMillis() + 20000
        }

        if (!Utils.inDungeons) {
            if (Skytils.config.copyDeathToClipboard) {
                if (formatted.startsWith("§r§c ☠ ")) {
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
        }
        if (Skytils.config.autoCopyRNGDrops) {
            if (formatted.startsWith("§r§d§lCRAZY RARE DROP! ") || formatted.startsWith("§r§c§lINSANE DROP! ") || formatted.startsWith(
                    "§r§6§lPET DROP! "
                ) || formatted.contains(" §r§ehas obtained §r§6§r§7[Lvl 1]")
            ) {
                GuiScreen.setClipboardString(unformatted)
                UChat.chat("$prefix §aCopied RNG drop to clipboard.")
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
        if (Skytils.config.autoCopyVeryRareDrops) {
            if (formatted.startsWith("§r§9§lVERY RARE DROP! ") || formatted.startsWith("§r§5§lVERY RARE DROP! ")) {
                GuiScreen.setClipboardString(unformatted)
                UChat.chat("$prefix §aCopied very rare drop to clipboard.")
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
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.bossBarFix && event.entity is IBossDisplayData && event.entity.isInvisible && event.entity.hasCustomName()) {
            event.result = Event.Result.ALLOW
        } else if (Skytils.config.hideDyingMobs && event.entity is EntityLivingBase && (event.entity.health <= 0 || event.entity.isDead)) {
            event.isCanceled = true
        } else if (event.entity is EntityFallingBlock) {
            val entity = event.entity
            if (Skytils.config.hideMidasStaffGoldBlocks && entity.block.block === Blocks.gold_block) {
                event.isCanceled = true
            }
        } else if (event.entity is EntityItem) {
            val entity = event.entity
            val item = entity.entityItem
            if (Skytils.config.hideJerryRune) {
                if (item.item === Items.spawn_egg && ItemMonsterPlacer.getEntityName(item) == "Villager" && item.displayName == "Spawn Villager" && entity.lifespan == 6000) {
                    event.isCanceled = true
                }
            }
            if (Skytils.config.hideCheapCoins && cheapCoins.contains(ItemUtil.getSkullTexture(item))) {
                event.isCanceled = true
            }
        } else if (event.entity is EntityLightningBolt) {
            if (Skytils.config.hideLightning) {
                event.isCanceled = true
            }
        } else if (event.entity is EntityOtherPlayerMP) {
            if (Skytils.config.hidePlayersInSpawn && event.entity.position == hubSpawnPoint && SBInfo.mode == SkyblockIsland.Hub.mode) {
                event.isCanceled = true
            }
        } else if (Skytils.deobfEnvironment && DevTools.getToggle("invis")) {
            event.entity.isInvisible = false
            (event.entity as? AccessorEntityArmorstand)?.invokeSetShowArms(true)
        }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inSkyblock || event.container !is ContainerChest) return
        val item = event.slot.stack ?: return
        if (Skytils.config.highlightDisabledPotionEffects && event.chestName.startsWith("Toggle Potion Effects")) {
            if (item.item == Items.potionitem) {
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

    @SubscribeEvent
    fun onJoin(event: EntityJoinWorldEvent) {
        if (!Utils.inSkyblock || mc.thePlayer == null || mc.theWorld == null) return
        if (event.entity is EntityArmorStand) {
            tickTimer(5) {
                val entity = event.entity as EntityArmorStand
                val headSlot = entity.getCurrentArmor(3)
                if (Skytils.config.trickOrTreatChestAlert && mc.thePlayer != null && headSlot != null && headSlot.item === Items.skull && headSlot.hasTagCompound() && entity.getDistanceSqToEntity(
                        mc.thePlayer
                    ) < 10 * 10
                ) {
                    if (headSlot.tagCompound.getCompoundTag("SkullOwner")
                            .getString("Id") == "f955b4ac-0c41-3e45-8703-016c46a8028e"
                    ) {
                        createTitle("§cTrick or Treat!", 60)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlayPre(event: RenderGameOverlayEvent.Pre) {
        if (!Utils.inSkyblock) return
        if (event.type == RenderGameOverlayEvent.ElementType.AIR && Skytils.config.hideAirDisplay && !Utils.inDungeons) {
            event.isCanceled = true
        } else if (event.type == RenderGameOverlayEvent.ElementType.ARMOR && Skytils.config.hideArmorDisplay) {
            event.isCanceled = true
        } else if (event.type == RenderGameOverlayEvent.ElementType.FOOD && Skytils.config.hideHungerDisplay) {
            event.isCanceled = true
        } else if (event.type == RenderGameOverlayEvent.ElementType.HEALTH && Skytils.config.hideHealthDisplay) {
            event.isCanceled = true
        } else if (event.type == RenderGameOverlayEvent.ElementType.HEALTHMOUNT && Skytils.config.hidePetHealth) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderHud(event: RenderHUDEvent) {
        if (!Utils.inSkyblock || mc.thePlayer == null || Skytils.config.lowHealthVignetteThreshold == 0.0f) return
        val healthPercentage = (mc.thePlayer.health + mc.thePlayer.absorptionAmount) / mc.thePlayer.baseMaxHealth
        if (healthPercentage < Skytils.config.lowHealthVignetteThreshold) {
            val color =
                Skytils.config.lowHealthVignetteColor.withAlpha((Skytils.config.lowHealthVignetteColor.alpha * (1.0 - healthPercentage)).toInt())

            PatcherCompatability.disableHUDCaching = true
            RenderUtil.drawVignette(color)
        } else PatcherCompatability.disableHUDCaching = false
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        if (Utils.inSkyblock && Skytils.config.noFire && event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock) return
        if (event.packet is S29PacketSoundEffect) {
            val packet = event.packet
            if (Skytils.config.disableCooldownSounds && packet.soundName == "mob.endermen.portal" && packet.pitch == 0f && packet.volume == 8f) {
                event.isCanceled = true
                return
            }
            if (Skytils.config.disableJerrygunSounds) {
                when (packet.soundName) {
                    "mob.villager.yes" -> if (packet.volume == 0.35f) {
                        event.isCanceled = true
                        return
                    }

                    "mob.villager.haggle" -> if (packet.volume == 0.5f) {
                        event.isCanceled = true
                        return
                    }
                }
            }
            if (Skytils.config.disableTruthFlowerSounds && packet.soundName == "random.eat" && packet.pitch == 0.6984127f && packet.volume == 1.0f) {
                event.isCanceled = true
                return
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onSlotClick(event: SlotClickEvent) {
        if (!Utils.inSkyblock) return
        if (event.container is ContainerChest) {
            val slot = event.slot ?: return
            val item = slot.stack ?: return
            if (Skytils.config.coopAddConfirmation && item.item == Items.diamond && item.displayName == "§aCo-op Request") {
                event.isCanceled = true
                UChat.chat("$failPrefix §c§lBe careful! Skytils stopped you from giving a player full control of your island!")
            }
            val extraAttributes = getExtraAttributes(item)
            if (event.chestName == "Ophelia") {
                if (Skytils.config.dungeonPotLock > 0) {
                    if (slot.inventory === mc.thePlayer.inventory || equalsOneOf(slot.slotNumber, 49, 53)) return
                    if (item.item !== Items.potionitem || extraAttributes == null || !extraAttributes.hasKey("potion_level")) {
                        event.isCanceled = true
                        return
                    }
                    if (extraAttributes.getInteger("potion_level") != Skytils.config.dungeonPotLock) {
                        event.isCanceled = true
                        return
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inSkyblock) return
        if (!Skytils.config.hideTooltipsOnStorage) return
        if (event.toolTip == null) return
        if (mc.currentScreen is GuiChest) {
            val player = Minecraft.getMinecraft().thePlayer
            val chest = player.openContainer as ContainerChest
            val inventory = chest.lowerChestInventory
            val chestName = inventory.displayName.unformattedText
            if (chestName.equals("Storage")) {
                if (ItemUtil.getDisplayName(event.itemStack).containsAny(
                        "Backpack",
                        "Ender Chest",
                        "Locked Page"
                    )
                )
                    event.toolTip.clear()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onSlotClickLow(event: SlotClickEvent) {
        if (!Utils.inSkyblock || !Skytils.config.middleClickGUIItems) return
        if (event.clickedButton != 0 || event.clickType != 0 || event.container !is ContainerChest || event.slot == null || !event.slot.hasStack) return
        val chest = event.container
        if (equalsOneOf(chest.lowerChestInventory.name, "Chest", "Large Chest")) return
        if (SBInfo.lastOpenContainerName.startsWithAny("Wardrobe", "Drill Anvil", "Anvil", "Storage")) return
        if (event.slot.inventory === mc.thePlayer.inventory || GuiScreen.isCtrlKeyDown()) return
        val item = event.slot.stack
        if (getSkyBlockItemID(item) == null) {
            if (SBInfo.lastOpenContainerName.startsWithAny(
                    "Reforge Item"
                ) && item.item === Item.getItemFromBlock(Blocks.anvil) && item.displayName == "§aReforge Item"
            ) return
            if (SBInfo.lastOpenContainerName.startsWithAny(
                    "Salvage Item"
                ) && item.item === Item.getItemFromBlock(Blocks.beacon) && item.displayName == "§aSalvage Item"
            ) return
            if (ItemUtil.getItemLore(item).asReversed().any {
                    it.contains("-click", true)
                }) return
            event.isCanceled = true
            mc.playerController.windowClick(chest.windowId, event.slotId, 2, 3, mc.thePlayer)
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!Utils.inSkyblock || event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return

        if (Skytils.config.playersInRangeDisplay) {
            inRangePlayerCount = (mc.theWorld.playerEntities.filterIsInstance<EntityOtherPlayerMP>().count {
                (it.uniqueID.version() == 4 || it.uniqueID.version() == 1) && it.getDistanceSqToEntity(mc.thePlayer) <= 30 * 30 // Nicked players have uuid v1, Watchdog has uuid v4
            } - 1).coerceAtLeast(0) // The -1 is to remove watchdog from the list
        }
        if (Skytils.config.summoningEyeDisplay && SBInfo.mode == SkyblockIsland.TheEnd.mode) {
            placedEyes = PlacedSummoningEyeDisplay.SUMMONING_EYE_FRAMES.count {
                mc.theWorld.getBlockState(it).run {
                    block === Blocks.end_portal_frame && this.getValue(BlockEndPortalFrame.EYE)
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.Post) {
        val item = event.stack ?: return
        if (!Utils.inSkyblock || mc.thePlayer == null || item.stackSize != 1 || item.tagCompound?.hasKey("SkytilsNoItemOverlay") == true) return
        var stackTip = ""

        val c = mc.thePlayer.openContainer
        if (c is ContainerChest) {
            val name = c.lowerChestInventory.name
            if (Skytils.config.showBestiaryLevel && bestiaryTitleRegex in name) {
                val arrowSlot = c.inventorySlots.getOrNull(48)?.stack
                if (arrowSlot != null && arrowSlot.item == Items.arrow && ItemUtil.getItemLore(item)
                        .lastOrNull() == "§eClick to view!"
                ) {
                    var ending = ItemUtil.getDisplayName(item).substringAfterLast(" ", "")
                    if (ending.any { !it.isUpperCase() }) ending = ""
                    stackTip = ending.romanToDecimal().toString()
                }
            }
            if (Skytils.config.showDungeonFloorAsStackSize && name == "Catacombs Gate" && item.item === Items.skull) {
                stackTip =
                    getSkyBlockItemID(item)?.substringAfterLast("CATACOMBS_PASS_")?.toIntOrNull()?.minus(3)?.toString()
                        ?: ""
            }
        }

        if (stackTip.isNotBlank()) {
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

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onEnderTeleport(event: EnderTeleportEvent) {
        if (Utils.inSkyblock && Skytils.config.disableEndermanTeleport) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun renderFishingHookAge(event: RenderWorldLastEvent) {
        if (Utils.inSkyblock && Config.fishingHookAge) {
            mc.theWorld?.getEntities(EntityFishHook::class.java) { entity ->
                mc.thePlayer == entity?.angler
            }?.filterNotNull()?.forEach { entity ->
                RenderUtil.drawLabel(entity.positionVector.addVector(0.0, 0.5, 0.0), "%.2fs".format(entity.ticksExisted / 20.0), Color.WHITE, event.partialTicks, UMatrixStack.Compat.get())
            }
        }
    }

    class GolemSpawnTimerElement : GuiElement("Endstone Protector Spawn Timer", x = 150, y = 20) {
        override fun render() {
            val player = mc.thePlayer
            if (toggled && Utils.inSkyblock && player != null && golemSpawnTime - System.currentTimeMillis() > 0) {

                val leftAlign = scaleX < sr.scaledWidth / 2f
                val text =
                    "§cGolem spawn in: §a" + ((golemSpawnTime - System.currentTimeMillis()) / 1000.0).roundToPrecision(1) + "s"
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    text,
                    if (leftAlign) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    textShadow
                )
            }
        }

        override fun demoRender() {
            ScreenRenderer.fontRenderer.drawString(
                "§cGolem spawn in: §a20.0s",
                0f,
                0f,
                CommonColors.WHITE,
                TextAlignment.LEFT_RIGHT,
                textShadow
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§cGolem spawn in: §a20.0s")

        override val toggled: Boolean
            get() = Skytils.config.golemSpawnTimer

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class PlayersInRangeDisplay : GuiElement("Players In Range Display", x = 50, y = 50) {
        override fun render() {
            if (toggled && Utils.inSkyblock && mc.thePlayer != null && mc.theWorld != null) {
                renderItem(ItemStack(Items.enchanted_book), 0, 0)
                ScreenRenderer.fontRenderer.drawString(
                    inRangePlayerCount.toString(),
                    20f,
                    5f,
                    CommonColors.ORANGE,
                    TextAlignment.LEFT_RIGHT,
                    textShadow
                )
            }
        }

        override fun demoRender() {
            renderItem(ItemStack(Items.enchanted_book), 0, 0)
            ScreenRenderer.fontRenderer.drawString(
                "69",
                20f,
                5f,
                CommonColors.ORANGE,
                TextAlignment.LEFT_RIGHT,
                textShadow
            )
        }

        override val height: Int
            get() = 16
        override val width: Int
            get() = 20 + ScreenRenderer.fontRenderer.getStringWidth("69")

        override val toggled: Boolean
            get() = Skytils.config.playersInRangeDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class PlacedSummoningEyeDisplay : GuiElement("Placed Summoning Eye Display", x = 50, y = 60) {
        override fun render() {
            val player = mc.thePlayer
            if (toggled && Utils.inSkyblock && player != null && mc.theWorld != null) {
                if (SBInfo.mode != SkyblockIsland.TheEnd.mode) return
                renderTexture(ICON, 0, 0)
                ScreenRenderer.fontRenderer.drawString(
                    "$placedEyes/8",
                    20f,
                    5f,
                    CommonColors.ORANGE,
                    TextAlignment.LEFT_RIGHT,
                    textShadow
                )
            }
        }

        override fun demoRender() {
            renderTexture(ICON, 0, 0)
            ScreenRenderer.fontRenderer.drawString(
                "6/8",
                20f,
                5f,
                CommonColors.ORANGE,
                TextAlignment.LEFT_RIGHT,
                textShadow
            )
        }

        override val height: Int
            get() = 16
        override val width: Int
            get() = 20 + ScreenRenderer.fontRenderer.getStringWidth("6/8")

        override val toggled: Boolean
            get() = Skytils.config.summoningEyeDisplay

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
            private val ICON = ResourceLocation("skytils", "icons/SUMMONING_EYE.png")
        }

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class WorldAgeDisplay : GuiElement("World Age Display", x = 50, y = 60) {

        var usesBaldTimeChanger = false

        override fun render() {
            if (toggled && Utils.inSkyblock && mc.theWorld != null) {
                if (usesBaldTimeChanger) {
                    ScreenRenderer.fontRenderer.drawString(
                        "Incompatible Time Changer detected.",
                        0f,
                        0f,
                        CommonColors.RED,
                        TextAlignment.LEFT_RIGHT,
                        textShadow
                    )
                    return
                }
                val day =
                    (mc.theWorld.worldInfo as AccessorWorldInfo).realWorldTime / 24000
                ScreenRenderer.fontRenderer.drawString(
                    "Day ${NumberUtil.nf.format(day)}",
                    0f,
                    0f,
                    CommonColors.ORANGE,
                    TextAlignment.LEFT_RIGHT,
                    textShadow
                )
            }
        }

        override fun demoRender() {
            usesBaldTimeChanger =
                Loader.instance().activeModList.any { it.modId == "timechanger" && it.version == "1.0" }
            if (usesBaldTimeChanger) {
                ScreenRenderer.fontRenderer.drawString(
                    "Incompatible Time Changer detected.",
                    0f,
                    0f,
                    CommonColors.RED,
                    TextAlignment.LEFT_RIGHT,
                    textShadow
                )
                return
            }
            ScreenRenderer.fontRenderer.drawString(
                "Day 0",
                0f,
                0f,
                CommonColors.ORANGE,
                TextAlignment.LEFT_RIGHT,
                textShadow
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Day 0")

        override val toggled: Boolean
            get() = Skytils.config.showWorldAge

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    object ItemNameHighlightDummy : GuiElement("Item Name Highlight", x = 50, y = 60) {
        override fun render() {
            //This is a placeholder
        }

        override fun demoRender() {
            ScreenRenderer.fontRenderer.drawString(
                "Item Name",
                0f,
                0f,
                CommonColors.WHITE,
                TextAlignment.MIDDLE,
                textShadow
            )
        }

        override val height: Int
            get() = fr.FONT_HEIGHT
        override val width: Int
            get() = fr.getStringWidth("Item Name")

        override val toggled: Boolean
            get() = Skytils.config.moveableItemNameHighlight

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    object ActionBarDummy : GuiElement("Action Bar", x = 50, y = 70) {
        override fun render() {
            //This is a placeholder
        }

        override fun demoRender() {
            ScreenRenderer.fontRenderer.drawString(
                "Action Bar",
                0f,
                0f,
                CommonColors.WHITE,
                TextAlignment.MIDDLE,
                textShadow
            )
        }

        override val height: Int
            get() = fr.FONT_HEIGHT
        override val width: Int
            get() = fr.getStringWidth("Action Bar")

        override val toggled: Boolean
            get() = Skytils.config.moveableActionBar

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}
