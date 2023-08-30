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

package gg.skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.ChatColor
import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.events.impl.CheckRenderEntityEvent
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorModelDragon
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.colors.ColorFactory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderDragon
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.awt.Color

object MasterMode7Features {

    private val spawningDragons = hashSetOf<WitherKingDragons>()
    private val killedDragons = hashSetOf<WitherKingDragons>()
    private val dragonMap = hashMapOf<Int, WitherKingDragons>()
    private val glowstones = hashSetOf<AxisAlignedBB>()
    private val dragonSpawnTimes = hashMapOf<WitherKingDragons, Long>()

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (DungeonTimer.phase4ClearTime == -1L) return
        if (Skytils.config.witherKingDragonSlashAlert) {
            if (event.old.block === Blocks.glowstone) {
                glowstones.clear()
                return
            }
            if (event.update.block === Blocks.glowstone && event.old.block != Blocks.packed_ice) {
                glowstones.add(AxisAlignedBB(event.pos.add(-5, -5, -5), event.pos.add(5, 5, 5)))
            }
        }
        if ((event.pos.y == 18 || event.pos.y == 19) && event.update.block === Blocks.air && event.old.block === Blocks.stone_slab) {
            val dragon = WitherKingDragons.values().find { it.bottomChin == event.pos } ?: return
            dragon.isDestroyed = true
        }
    }

    init {
        TickTask(15, repeats = true) {
            if (DungeonTimer.phase4ClearTime == -1L || DungeonTimer.scoreShownAt != -1L || mc.thePlayer == null) return@TickTask
            if (Skytils.config.witherKingDragonSlashAlert) {
                if (glowstones.any { it.isVecInside(mc.thePlayer.positionVector) }) {
                    GuiManager.createTitle("Dimensional Slash!", 10)
                }
            }
        }
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
                printDevMessage("${drag.name} spawning $x $y $z", "witherkingdrags")
            }
        } else if (event.packet is S2APacketParticles) {
            event.packet.apply {
                if (count != 20 || y != WitherKingDragons.particleYConstant || type != EnumParticleTypes.FLAME || xOffset != 2f || yOffset != 3f || zOffset != 2f || speed != 0f || !isLongDistance || x % 1 != 0.0 || z % 1 != 0.0) return
                val owner = WitherKingDragons.values().find {
                    it.particleLocation.x == x.toInt() && it.particleLocation.z == z.toInt()
                } ?: return
                if (owner !in dragonSpawnTimes) {
                    dragonSpawnTimes[owner] = System.currentTimeMillis() + 5000
                    if (Skytils.config.witherKingDragonSpawnAlert) {
                        UChat.chat("§c§lThe ${owner.chatColor}§l${owner.name} §c§ldragon is spawning!")
                    }
                }
            }
        }
    }

    fun onMobSpawned(entity: Entity) {
        if (DungeonTimer.phase4ClearTime != -1L && entity is EntityDragon) {
            val type =
                dragonMap[entity.entityId] ?: WitherKingDragons.values()
                    .minByOrNull { entity.getXZDistSq(it.blockPos) } ?: return
            (entity as ExtensionEntityLivingBase).skytilsHook.colorMultiplier = type.color
            (entity as ExtensionEntityLivingBase).skytilsHook.masterDragonType = type
            printDevMessage("${type.name} spawned", "witherkingdrags")
            dragonMap[entity.entityId] = type
        }
    }

    @SubscribeEvent
    fun onDeath(event: LivingDeathEvent) {
        if (event.entity is EntityDragon) {
            val item = (event.entity as ExtensionEntityLivingBase).skytilsHook.masterDragonType ?: return
            printDevMessage("${item.name} died", "witherkingdrags")
            spawningDragons.remove(item)
            killedDragons.add(item)
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        spawningDragons.clear()
        killedDragons.clear()
        dragonMap.clear()
        glowstones.clear()
        WitherKingDragons.values().forEach { it.isDestroyed = false }
    }

    @SubscribeEvent
    fun onRenderLivingPost(event: RenderLivingEvent.Post<*>) {
        val entity = event.entity
        if (DungeonTimer.phase4ClearTime != -1L && entity is EntityDragon && (Skytils.config.showWitherDragonsColorBlind || Skytils.config.showWitherKingDragonsHP || Skytils.config.showWitherKingStatueBox)) {
            val matrixStack = UMatrixStack()
            entity as ExtensionEntityLivingBase
            GlStateManager.disableCull()
            GlStateManager.disableDepth()
            val text = StringBuilder()
            val percentage = event.entity.health / event.entity.baseMaxHealth
            val color = when {
                percentage >= 0.75 -> ColorFactory.YELLOWGREEN
                percentage >= 0.5 -> ColorFactory.YELLOW
                percentage >= 0.25 -> ColorFactory.DARKORANGE
                else -> ColorFactory.CRIMSON
            }
            if (Skytils.config.showWitherDragonsColorBlind) {
                text.append(entity.skytilsHook.masterDragonType?.textColor)
                text.append(' ')
            }
            if (Skytils.config.showWitherKingDragonsHP) {
                text.append(NumberUtil.format(event.entity.health))
            }
            if (Skytils.config.showWitherKingStatueBox && entity.skytilsHook.masterDragonType?.bb?.isVecInside(entity.positionVector) == true) {
                text.append(" §fR")
            }

            RenderUtil.drawLabel(
                Vec3(
                    RenderUtil.interpolate(entity.posX, entity.lastTickPosX, RenderUtil.getPartialTicks()),
                    RenderUtil.interpolate(entity.posY, entity.lastTickPosY, RenderUtil.getPartialTicks()) + 0.5f,
                    RenderUtil.interpolate(entity.posZ, entity.lastTickPosZ, RenderUtil.getPartialTicks())
                ),
                text.toString(),
                color,
                RenderUtil.getPartialTicks(),
                matrixStack,
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
                RenderUtil.drawOutlinedBoundingBox(drag.bb, drag.color, 3.69f, event.partialTicks)
            }
        }
        if (Skytils.config.showWitherKingDragonsSpawnTimer) {
            val stack = UMatrixStack()
            GlStateManager.disableCull()
            GlStateManager.disableDepth()
            dragonSpawnTimes.entries.removeAll { (drag, time) ->
                val diff = time - System.currentTimeMillis()
                val color = when {
                    diff <= 1000 -> 'c'
                    diff <= 3000 -> 'e'
                    else -> 'a'
                }
                RenderUtil.drawLabel(
                    drag.bottomChin.middleVec(),
                    "${drag.textColor}: §${color}$diff ms",
                    drag.color,
                    event.partialTicks,
                    stack,
                    scale = 6f
                )
                return@removeAll diff < 0
            }
            GlStateManager.enableCull()
            GlStateManager.enableDepth()
        }
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (event.entity is EntityDragon && event.entity.deathTicks > 1 && shouldHideDragonDeath()) {
            event.isCanceled = true
        }
    }

    fun getHurtOpacity(
        renderDragon: RenderDragon,
        lastDragon: EntityDragon,
        value: Float
    ): Float {
        if (!Skytils.config.changeHurtColorOnWitherKingsDragons) return value
        lastDragon as ExtensionEntityLivingBase
        return if (lastDragon.skytilsHook.colorMultiplier != null) {
            val model =
                renderDragon.mainModel as AccessorModelDragon
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
        val model =
            renderDragon.mainModel as AccessorModelDragon
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
    val texture = ResourceLocation("skytils", "textures/dungeons/m7/dragon_${name.lowercase()}.png")
    val bb = blockPos.run {
        AxisAlignedBB(x - a, y - 8.0, z - a, x + a, y + a + 2, z + a)
    }
    val particleLocation: BlockPos = blockPos.up(5)

    companion object {
        private const val a = 13.5
        const val particleYConstant = 19.0
    }
}