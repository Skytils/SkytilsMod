/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.ChatColor
import gg.essential.universal.UChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderDragon
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.events.impl.BlockChangeEvent
import skytils.skytilsmod.events.impl.MainReceivePacketEvent
import skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase
import skytils.skytilsmod.mixins.transformers.accessors.AccessorModelDragon
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.colors.ColorFactory
import java.awt.Color

object MasterMode7Features {

    private val spawningDragons = hashSetOf<WitherKingDragons>()
    private val spawnedDragons = hashSetOf<WitherKingDragons>()
    private val killedDragons = hashSetOf<WitherKingDragons>()
    private val dragonMap = hashMapOf<Int, WitherKingDragons>()
    private val glowstones = hashSetOf<AxisAlignedBB>()
    private var ticks = 0

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (DungeonTimer.phase4ClearTime == -1L) return
        if (Skytils.config.witherKingDragonSlashAlert) {
            if (event.old.block === Blocks.glowstone) {
                glowstones.clear()
                return
            }
            if (event.update.block === Blocks.glowstone) {
                glowstones.add(AxisAlignedBB(event.pos.add(-5, -5, -5), event.pos.add(5, 5, 5)))
            }
        }
        if ((event.pos.y == 18 || event.pos.y == 19) && event.update.block === Blocks.air && event.old.block === Blocks.stone_slab) {
            val dragon = WitherKingDragons.values().find { it.bottomChin == event.pos } ?: return
            dragon.isDestroyed = true
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (DungeonTimer.phase4ClearTime == -1L || event.phase != TickEvent.Phase.START || mc.thePlayer == null) return
        if (ticks % 15 == 0) {
            if (Skytils.config.witherKingDragonSlashAlert) {
                if (glowstones.any { it.isVecInside(mc.thePlayer.positionVector) }) {
                    GuiManager.createTitle("Dimensional Slash!", 10)
                }
            }
            ticks = 0
        }
        ticks++
    }

    @SubscribeEvent
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (DungeonTimer.phase4ClearTime == -1L) return
        if (event.packet is S2CPacketSpawnGlobalEntity && event.packet.func_149053_g() == 1) {
            val x = event.packet.func_149051_d() / 32.0
            val y = event.packet.func_149050_e() / 32.0
            val z = event.packet.func_149049_f() / 32.0
            if (x % 1 != 0.0 || y % 1 != 0.0 || z % 1 != 0.0) return
            val drag =
                WitherKingDragons.values().find { it.blockPos.x == x.toInt() && it.blockPos.z == z.toInt() } ?: return
            if (spawningDragons.add(drag)) {
                printDevMessage("${drag.name} spawning", "witherkingdrags")
                if (Skytils.config.witherKingDragonSpawnAlert) UChat.chat("§c§lThe ${drag.chatColor}§l${drag.name} §c§lis spawning! §f(${x}, ${y}, ${z})")
            }
        }
    }

    fun onMobSpawned(entity: Entity) {
        if (DungeonTimer.phase4ClearTime != -1L && entity is EntityDragon) {
            val type =
                dragonMap[entity.entityId] ?: WitherKingDragons.values().filterNot { spawnedDragons.contains(it) }
                    .minByOrNull { entity.getXZDistSq(it.blockPos) } ?: return
            (entity as ExtensionEntityLivingBase).skytilsHook.colorMultiplier = type.color
            (entity as ExtensionEntityLivingBase).skytilsHook.masterDragonType = type
            printDevMessage("${type.name} spawned", "witherkingdrags")
            spawnedDragons.add(type)
            dragonMap[entity.entityId] = type
        }
    }

    @SubscribeEvent
    fun onDeath(event: LivingDeathEvent) {
        if (event.entity is EntityDragon) {
            val item = (event.entity as ExtensionEntityLivingBase).skytilsHook.masterDragonType ?: return
            printDevMessage("${item.name} died", "witherkingdrags")
            spawningDragons.remove(item)
            spawnedDragons.remove(item)
            killedDragons.add(item)
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        spawningDragons.clear()
        spawnedDragons.clear()
        killedDragons.clear()
        dragonMap.clear()
        glowstones.clear()
        WitherKingDragons.values().forEach { it.isDestroyed = false }
    }

    @SubscribeEvent
    fun onRenderLivingPost(event: RenderLivingEvent.Post<*>) {
        val entity = event.entity
        if (Skytils.config.showWitherKingDragonsHP && DungeonTimer.phase4ClearTime != -1L && entity is EntityDragon) {
            entity as ExtensionEntityLivingBase
            GlStateManager.disableCull()
            GlStateManager.disableDepth()
            val percentage = event.entity.health / event.entity.baseMaxHealth
            val color = when {
                percentage >= 0.75 -> ColorFactory.YELLOWGREEN
                percentage >= 0.5 -> ColorFactory.YELLOW
                percentage >= 0.25 -> ColorFactory.DARKORANGE
                else -> ColorFactory.CRIMSON
            }
            RenderUtil.drawLabel(
                Vec3(
                    RenderUtil.interpolate(entity.posX, entity.lastTickPosX, RenderUtil.getPartialTicks()),
                    RenderUtil.interpolate(entity.posY, entity.lastTickPosY, RenderUtil.getPartialTicks()) + 0.5f,
                    RenderUtil.interpolate(entity.posZ, entity.lastTickPosZ, RenderUtil.getPartialTicks())
                ),
                "${NumberUtil.format(event.entity.health)} ${
                    "§fR".toStringIfTrue(
                        entity.skytilsHook.masterDragonType?.bb?.isVecInside(entity.positionVector)
                    )
                }",
                color,
                RenderUtil.getPartialTicks(),
                true,
                6f
            )
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (Skytils.config.showWitherKingStatueBox && DungeonTimer.phase4ClearTime != -1L) {
            for (drag in WitherKingDragons.values()) {
                if (drag.isDestroyed) continue
                RenderUtil.drawOutlinedBoundingBox(drag.bb, drag.color, 5f, event.partialTicks)
            }
        }
    }

    fun onRenderMainModel(
        entity: EntityDragon,
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        j: Float,
        scaleFactor: Float,
        ci: CallbackInfo
    ) {
        if (!Skytils.config.recolorWitherKingsDragons) return
        entity as ExtensionEntityLivingBase
        entity.skytilsHook.colorMultiplier?.bindColor()
    }

    fun getHurtOpacity(
        renderDragon: RenderDragon,
        lastDragon: EntityDragon,
        value: Float
    ): Float {
        if (!Skytils.config.recolorWitherKingsDragons) return value
        lastDragon as ExtensionEntityLivingBase
        return if (lastDragon.skytilsHook.colorMultiplier != null) {
            val model = renderDragon.mainModel as AccessorModelDragon
            model.body.isHidden = true
            model.wing.isHidden = true
            0.03f
        } else value
    }

    fun getEntityTexture(entity: EntityDragon, cir: CallbackInfoReturnable<ResourceLocation>) {
        if (!Skytils.config.retextureWitherKingsDragons) return
        entity as ExtensionEntityLivingBase
        val type = entity.skytilsHook.masterDragonType ?: return
        cir.returnValue = type.texture
    }

    fun afterRenderHurtFrame(
        renderDragon: RenderDragon,
        entitylivingbaseIn: EntityDragon,
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        j: Float,
        scaleFactor: Float,
        ci: CallbackInfo
    ) {
        val model = renderDragon.mainModel as AccessorModelDragon
        model.body.isHidden = false
        model.wing.isHidden = false
    }

    fun shouldHideDragonDeath() =
        Utils.inDungeons && DungeonTimer.phase4ClearTime != -1L && Skytils.config.hideWitherKingDragonDeath
}

enum class WitherKingDragons(
    val textColor: String,
    val blockPos: BlockPos,
    val color: Color,
    val chatColor: ChatColor,
    val bottomChin: BlockPos,
    var isDestroyed: Boolean = false
) {
    POWER("Red", BlockPos(27, 14, 59), ColorFactory.RED, ChatColor.RED, BlockPos(32, 18, 59)),
    APEX("Green", BlockPos(27, 14, 94), ColorFactory.LIME, ChatColor.GREEN, BlockPos(32, 19, 94)),
    SOUL("Purple", BlockPos(56, 14, 125), ColorFactory.PURPLE, ChatColor.DARK_PURPLE, BlockPos(56, 18, 128)),
    ICE("Blue", BlockPos(84, 14, 94), ColorFactory.CYAN, ChatColor.AQUA, BlockPos(79, 19, 94)),
    FLAME("Orange", BlockPos(85, 14, 56), ColorFactory.CORAL, ChatColor.GOLD, BlockPos(80, 19, 56));

    val itemName = "§cCorrupted $textColor Relic"
    val itemId = "${textColor.uppercase()}_KING_RELIC"
    val texture = ResourceLocation("skytils", "textures/dungeons/m7/dragon_${this.name.lowercase()}.png")
    private val a = 13.5
    val bb = blockPos.run {
        AxisAlignedBB(x - a, y - 8.0, z - a, x + a, y + a + 2, z + a)
    }
}