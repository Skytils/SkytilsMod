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

import net.minecraft.block.BlockEndPortalFrame
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.*
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager.Companion.createTitle
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.*
import skytils.skytilsmod.events.GuiContainerEvent.SlotClickEvent
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import skytils.skytilsmod.utils.NumberUtil.round
import skytils.skytilsmod.utils.RenderUtil.renderItem
import skytils.skytilsmod.utils.RenderUtil.renderTexture
import skytils.skytilsmod.utils.StringUtils.startsWithAny
import skytils.skytilsmod.utils.StringUtils.stripControlCodes
import skytils.skytilsmod.utils.Utils.equalsOneOf
import skytils.skytilsmod.utils.Utils.isInTablist
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow
import skytils.skytilsmod.utils.graphics.colors.CommonColors

class MiscFeatures {
    @SubscribeEvent
    fun onBossBarSet(event: BossBarEvent.Set) {
        val displayData = event.displayData
        if (Utils.inSkyblock) {
            if (Skytils.config.bossBarFix && stripControlCodes(displayData.displayName.unformattedText) == "Wither") {
                event.isCanceled = true
                return
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock) return
        val unformatted = stripControlCodes(event.message.unformattedText).trim { it <= ' ' }
        if (unformatted == "The ground begins to shake as an Endstone Protector rises from below!") {
            golemSpawnTime = System.currentTimeMillis() + 20000
        }
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityCreeper) {
            val entity = event.entity as EntityCreeper
            if (!Utils.inDungeons && Skytils.config.hideCreeperVeilNearNPCs && entity.maxHealth == 20f && entity.health == 20f && entity.powered) {
                if (mc.theWorld.playerEntities.stream().anyMatch { p: EntityPlayer ->
                        p is EntityOtherPlayerMP && p.getUniqueID()
                            .version() == 2 && p.getHealth() == 20f && !p.isPlayerSleeping() && p.getDistanceSqToEntity(
                            event.entity
                        ) <= 49
                    }) {
                    event.isCanceled = true
                }
            }
        }
        if (event.entity is EntityFallingBlock) {
            val entity = event.entity as EntityFallingBlock
            if (Skytils.config.hideMidasStaffGoldBlocks && entity.block.block === Blocks.gold_block) {
                event.isCanceled = true
            }
        }
        if (event.entity is EntityItem) {
            val entity = event.entity as EntityItem
            if (Skytils.config.hideJerryRune) {
                val item = entity.entityItem
                if (item.item === Items.spawn_egg && ItemMonsterPlacer.getEntityName(item) == "Villager" && item.displayName == "Spawn Villager" && entity.lifespan == 6000) {
                    event.isCanceled = true
                }
            }
        }
        if (event.entity is EntityLightningBolt) {
            if (Skytils.config.hideLightning) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onJoin(event: EntityJoinWorldEvent) {
        if (!Utils.inSkyblock || mc.thePlayer == null || mc.theWorld == null) return
        if (event.entity is EntityArmorStand) {
            val entity = event.entity as EntityArmorStand
            val headSlot = entity.getCurrentArmor(3)
            if (Skytils.config.trickOrTreatChestAlert && headSlot != null && headSlot.item === Items.skull && headSlot.hasTagCompound() && entity.getDistanceSqToEntity(
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

    @SubscribeEvent
    fun onRenderOverlayPre(event: RenderGameOverlayEvent.Pre) {
        if (!Utils.inSkyblock) return
        if (event.type == RenderGameOverlayEvent.ElementType.AIR && Skytils.config.hideAirDisplay && !Utils.inDungeons) {
            event.isCanceled = true
        }
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
            val packet = event.packet as S29PacketSoundEffect?
            if (Skytils.config.disableCooldownSounds && packet!!.soundName == "mob.endermen.portal" && packet.pitch == 0f && packet.volume == 8f) {
                event.isCanceled = true
            }
            if (Skytils.config.disableJerrygunSounds) {
                when (packet!!.soundName) {
                    "mob.villager.yes" -> if (packet.volume == 0.35f) {
                        event.isCanceled = true
                    }
                    "mob.villager.haggle" -> if (packet.volume == 0.5f) {
                        event.isCanceled = true
                    }
                }
            }
            if (Skytils.config.disableTruthFlowerSounds && packet!!.soundName == "random.eat" && packet.pitch == 0.6984127f && packet.volume == 1.0f) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!Utils.inSkyblock) return
        if (event.container is ContainerChest) {
            val chest = event.container as ContainerChest
            val inventory = chest.lowerChestInventory
            val slot = event.slot ?: return
            val item = slot.stack
            val inventoryName = inventory.displayName.unformattedText
            if (item == null) return
            val extraAttributes = getExtraAttributes(item)
            if (inventoryName == "Ophelia") {
                if (Skytils.config.dungeonPotLock > 0) {
                    if (slot.inventory === mc.thePlayer.inventory || slot.slotNumber == 49) return
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onSlotClickLow(event: SlotClickEvent) {
        if (!Utils.inSkyblock || !Skytils.config.middleClickGUIItems) return
        if (event.clickedButton != 0 || event.clickType != 0 || event.container !is ContainerChest || event.slot == null || !event.slot!!.hasStack) return
        val chest = event.container as ContainerChest
        if (equalsOneOf(chest.lowerChestInventory.name, "Chest", "Large Chest")) return
        if (startsWithAny(SBInfo.instance.lastOpenContainerName, "Wardrobe", "Drill Anvil", "Anvil")) return
        if (event.slot!!.inventory === mc.thePlayer.inventory || GuiScreen.isCtrlKeyDown()) return
        val item = event.slot!!.stack
        if (getSkyBlockItemID(item) == null) {
            if (startsWithAny(
                    SBInfo.instance.lastOpenContainerName,
                    "Auctions"
                ) && item.item === Items.arrow
            ) return
            if (startsWithAny(
                    SBInfo.instance.lastOpenContainerName,
                    "Reforge Item"
                ) && item.item === Item.getItemFromBlock(Blocks.anvil) && item.displayName == "§aReforge Item"
            ) return
            event.isCanceled = true
            mc.playerController.windowClick(chest.windowId, event.slotId, 2, 0, mc.thePlayer)
        }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private var golemSpawnTime: Long = 0

        init {
            GolemSpawnTimerElement()
            LegionPlayerDisplay()
            PlacedSummoningEyeDisplay()
        }
    }

    class GolemSpawnTimerElement : GuiElement("Endstone Protector Spawn Timer", FloatPair(150, 20)) {
        override fun render() {
            val player = mc.thePlayer
            if (toggled && Utils.inSkyblock && player != null && golemSpawnTime - System.currentTimeMillis() > 0) {
                val sr = ScaledResolution(mc)
                val leftAlign = actualX < sr.scaledWidth / 2f
                val text =
                    "§cGolem spawn in: §a" + round((golemSpawnTime - System.currentTimeMillis()) / 1000.0, 1) + "s"
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    text,
                    if (leftAlign) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    TextShadow.NORMAL
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
                TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§cGolem spawn in: §a20.0s")

        override val toggled: Boolean
            get() = Skytils.config.golemSpawnTimer

        init {
            Skytils.GUIMANAGER.registerElement(this)
        }
    }

    class LegionPlayerDisplay : GuiElement("Legion Player Display", FloatPair(50, 50)) {
        override fun render() {
            val player = mc.thePlayer
            if (toggled && Utils.inSkyblock && player != null && mc.theWorld != null) {
                var hasLegion = false
                for (armor in player.inventory.armorInventory) {
                    val extraAttr = getExtraAttributes(armor)
                    if (extraAttr != null && extraAttr.hasKey("enchantments") && extraAttr.getCompoundTag("enchantments")
                            .hasKey("ultimate_legion")
                    ) {
                        hasLegion = true
                        break
                    }
                }
                if (!hasLegion) return
                renderItem(ItemStack(Items.enchanted_book), 0, 0)
                val players = mc.theWorld.getPlayers<EntityPlayer>(
                    EntityOtherPlayerMP::class.java
                ) { p: EntityPlayer? ->
                    p!!.getDistanceToEntity(player) <= 30 && p.uniqueID.version() != 2 && p !== player && isInTablist(
                        p
                    )
                }
                ScreenRenderer.fontRenderer.drawString(
                    if (Skytils.config.legionCap && players.size > 20) "20" else players.size.toString(),
                    20f,
                    5f,
                    CommonColors.ORANGE,
                    TextAlignment.LEFT_RIGHT,
                    TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val x = 0f
            val y = 0f
            renderItem(ItemStack(Items.enchanted_book), x.toInt(), y.toInt())
            ScreenRenderer.fontRenderer.drawString(
                "30",
                x + 20,
                y + 5,
                CommonColors.ORANGE,
                TextAlignment.LEFT_RIGHT,
                TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = 16
        override val width: Int
            get() = 20 + ScreenRenderer.fontRenderer.getStringWidth("30")

        override val toggled: Boolean
            get() = Skytils.config.legionPlayerDisplay

        init {
            Skytils.GUIMANAGER.registerElement(this)
        }
    }

    class PlacedSummoningEyeDisplay : GuiElement("Placed Summoning Eye Display", FloatPair(50, 60)) {
        override fun render() {
            val player = mc.thePlayer
            if (toggled && Utils.inSkyblock && player != null && mc.theWorld != null) {
                if (SBInfo.instance.mode != "combat_3") return
                var invalid = false
                var placedEyes = 0
                for (pos in SUMMONING_EYE_FRAMES) {
                    val block = mc.theWorld.getBlockState(pos)
                    if (block.block !== Blocks.end_portal_frame) {
                        invalid = true
                        break
                    } else if (block.getValue(BlockEndPortalFrame.EYE)) {
                        placedEyes++
                    }
                }
                if (invalid) return
                renderTexture(ICON, 0, 0)
                ScreenRenderer.fontRenderer.drawString(
                    "$placedEyes/8",
                    20f,
                    5f,
                    CommonColors.ORANGE,
                    TextAlignment.LEFT_RIGHT,
                    TextShadow.NORMAL
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
                TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = 16
        override val width: Int
            get() = 20 + ScreenRenderer.fontRenderer.getStringWidth("6/8")

        override val toggled: Boolean
            get() = Skytils.config.summoningEyeDisplay

        companion object {
            private val SUMMONING_EYE_FRAMES = arrayOf(
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
            Skytils.GUIMANAGER.registerElement(this)
        }
    }
}