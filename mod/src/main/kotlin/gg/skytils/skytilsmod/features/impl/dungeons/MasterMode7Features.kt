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

import com.mojang.blaze3d.opengl.GlStateManager
import gg.essential.universal.ChatColor
import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.entity.LivingEntityDeathEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.render.CheckRenderEntityEvent
import gg.skytils.event.impl.render.LivingEntityPreRenderEvent
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.impl.world.BlockStateUpdateEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.MainThreadPacketReceiveEvent
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorModelDragon
import gg.skytils.skytilsmod.utils.graphics.colors.ColorFactory
import gg.skytils.skytilsmod.utils.NumberUtil
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.middleVec
import gg.skytils.skytilsmod.utils.printDevMessage
import gg.skytils.skytilsmod.utils.toVec3
import gg.skytils.skytilsmod.utils.type
import net.minecraft.client.render.entity.EnderDragonEntityRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.block.Blocks
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.EntityType
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.awt.Color

object MasterMode7Features : EventSubscriber {

    private val spawningDragons = hashSetOf<WitherKingDragons>()
    private val killedDragons = hashSetOf<WitherKingDragons>()
    private val dragonMap = hashMapOf<Int, WitherKingDragons>()
    private val glowstones = hashSetOf<Box>()
    private val dragonSpawnTimes = hashMapOf<WitherKingDragons, Long>()

    override fun setup() {
        register(::onBlockChange)
        register(::onPacket)
        register(::onDeath)
        register(::onWorldLoad)
        register(::onRenderLivingPost)
        register(::onRenderWorld)
        register(::onCheckRender)
    }

    fun onBlockChange(event: BlockStateUpdateEvent) {
        if (DungeonTimer.phase4ClearTime == null) return
        if (Skytils.config.witherKingDragonSlashAlert) {
            if (event.old.block === Blocks.GLOWSTONE) {
                glowstones.clear()
                return
            }
            if (event.update.block === Blocks.GLOWSTONE && event.old.block != Blocks.PACKED_ICE) {
                glowstones.add(Box(event.pos.add(-5, -5, -5).toVec3(), event.pos.add(5, 5, 5).toVec3()))
            }
        }
        if ((event.pos.y == 18 || event.pos.y == 19) && event.update.block === Blocks.AIR && event.old.block === Blocks.SMOOTH_STONE_SLAB) {
            val dragon = WitherKingDragons.entries.find { it.bottomChin == event.pos } ?: return
            dragon.isDestroyed = true
        }
    }

    init {
        tickTimer(15, repeats = true) {
            val player = mc.player
            if (DungeonTimer.phase4ClearTime == null || DungeonTimer.scoreShownAt != null || player == null) return@tickTimer
            if (Skytils.config.witherKingDragonSlashAlert) {
                if (glowstones.any { it.contains(player.pos) }) {
                    GuiManager.createTitle("Dimensional Slash!", 10)
                }
            }
        }
    }

    fun onPacket(event: MainThreadPacketReceiveEvent<*>) {
        if (DungeonTimer.phase4ClearTime == null) return
        if (event.packet is EntitySpawnS2CPacket && event.packet.entityType == EntityType.ENDER_DRAGON) {
            val x = event.packet.x / 32.0
            val y = event.packet.y / 32.0
            val z = event.packet.z / 32.0
            if (x % 1 != 0.0 || y % 1 != 0.0 || z % 1 != 0.0) return
            val drag =
                WitherKingDragons.entries.find { it.blockPos.x == x.toInt() && it.blockPos.z == z.toInt() } ?: return
            if (spawningDragons.add(drag)) {
                printDevMessage({ "${drag.name} spawning $x $y $z" }, "witherkingdrags")
            }
        } else if (event.packet is ParticleS2CPacket) {
            event.packet.apply {
                if (count != 20 || y != WitherKingDragons.particleYConstant || this.type != ParticleTypes.FLAME || offsetX != 2f || offsetY != 3f || offsetZ != 2f || speed != 0f || !shouldForceSpawn() || x % 1 != 0.0 || z % 1 != 0.0) return
                val owner = WitherKingDragons.entries.find {
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
        if (DungeonTimer.phase4ClearTime != null && entity is EnderDragonEntity) {
            val type =
                dragonMap[entity.id] ?: WitherKingDragons.entries
                    .minByOrNull { entity.squaredDistanceTo(it.blockPos.toVec3()) } ?: return
            (entity as ExtensionEntityLivingBase).skytilsHook.colorMultiplier = type.color
            (entity as ExtensionEntityLivingBase).skytilsHook.masterDragonType = type
            printDevMessage({ "${type.name} spawned" }, "witherkingdrags")
            dragonMap[entity.id] = type
        }
    }

    fun onDeath(event: LivingEntityDeathEvent) {
        if (event.entity is EnderDragonEntity) {
            val item = (event.entity as ExtensionEntityLivingBase).skytilsHook.masterDragonType ?: return
            printDevMessage({ "${item.name} died" }, "witherkingdrags")
            spawningDragons.remove(item)
            killedDragons.add(item)
        }
    }

    fun onWorldLoad(event: WorldUnloadEvent) {
        spawningDragons.clear()
        killedDragons.clear()
        dragonMap.clear()
        glowstones.clear()
        WitherKingDragons.entries.forEach { it.isDestroyed = false }
    }

    fun onRenderLivingPost(event: LivingEntityPreRenderEvent<*, *, *>) {
        val entity = event.entity
        if (DungeonTimer.phase4ClearTime != null && entity is EnderDragonEntity && (Skytils.config.showWitherDragonsColorBlind || Skytils.config.showWitherKingDragonsHP || Skytils.config.showWitherKingStatueBox)) {
            val matrixStack = UMatrixStack()
            entity as ExtensionEntityLivingBase
            GlStateManager._disableCull()
            GlStateManager._disableDepthTest()
            val text = StringBuilder()
            val percentage = event.entity.health / event.entity.maxHealth
            val color = when {
                percentage >= 0.75 -> Color(0x9ACD32)
                percentage >= 0.5 -> Color.YELLOW
                percentage >= 0.25 -> Color(0xFF8C00)
                else -> ColorFactory.CRIMSON
            }
            if (Skytils.config.showWitherDragonsColorBlind) {
                text.append(entity.skytilsHook.masterDragonType?.textColor)
                text.append(' ')
            }
            if (Skytils.config.showWitherKingDragonsHP) {
                text.append(NumberUtil.format(event.entity.health))
            }
            if (Skytils.config.showWitherKingStatueBox && entity.skytilsHook.masterDragonType?.bb?.contains(entity.pos) == true) {
                text.append(" §fR")
            }

            RenderUtil.drawLabel(
                Vec3d(
                    RenderUtil.interpolate(entity.x, entity.lastRenderX, RenderUtil.getPartialTicks()),
                    RenderUtil.interpolate(entity.y, entity.lastRenderY, RenderUtil.getPartialTicks()) + 0.5f,
                    RenderUtil.interpolate(entity.z, entity.lastRenderZ, RenderUtil.getPartialTicks())
                ),
                text.toString(),
                color,
                RenderUtil.getPartialTicks(),
                matrixStack,
                true,
                6f
            )
            GlStateManager._enableDepthTest()
            GlStateManager._enableCull()
        }
    }

    fun onRenderWorld(event: WorldDrawEvent) {
        if (Skytils.config.showWitherKingStatueBox && DungeonTimer.phase4ClearTime != null) {
            for (drag in WitherKingDragons.entries) {
                if (drag.isDestroyed) continue
                RenderUtil.drawOutlinedBoundingBox(drag.bb, drag.color, 3.69f, event.partialTicks)
            }
        }
        if (Skytils.config.showWitherKingDragonsSpawnTimer) {
            val stack = UMatrixStack()
            GlStateManager._disableCull()
            GlStateManager._disableDepthTest()
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
            GlStateManager._enableDepthTest()
            GlStateManager._enableCull()
        }
    }

    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (event.entity is EnderDragonEntity && (event.entity as EnderDragonEntity).ticksSinceDeath > 1 && shouldHideDragonDeath()) {
            event.cancelled = true
        }
    }

    fun getHurtOpacity(
        renderDragon: EnderDragonEntityRenderer,
        lastDragon: EnderDragonEntity,
        value: Float
    ): Float {
        if (!Skytils.config.changeHurtColorOnWitherKingsDragons) return value
        lastDragon as ExtensionEntityLivingBase
        return if (lastDragon.skytilsHook.colorMultiplier != null) {
            // FIXME
//            val model =
//                renderDragon.method_4038() as AccessorModelDragon
//            model.body.noDraw = true
//            model.wing.noDraw = true
            0.03f
        } else value
    }

    fun getDragonCutoutLayer(entity: Entity?, renderLayer: RenderLayer): RenderLayer {
        if (entity !is EnderDragonEntity || !Skytils.config.retextureWitherKingsDragons) return renderLayer
        entity as ExtensionEntityLivingBase
        val type = entity.skytilsHook.masterDragonType ?: return renderLayer
        return type.cutoutLayer
    }


    fun getEntityTexture(entity: EnderDragonEntity, cir: CallbackInfoReturnable<Identifier>) {
        if (!Skytils.config.retextureWitherKingsDragons) return
        entity as ExtensionEntityLivingBase
        val type = entity.skytilsHook.masterDragonType ?: return
        cir.returnValue = type.texture
    }

    fun afterRenderHurtFrame(
        renderDragon: EnderDragonEntityRenderer,
        entitylivingbaseIn: EnderDragonEntity,
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        j: Float,
        scaleFactor: Float,
        ci: CallbackInfo
    ) {
        // FIXME
//        val model =
//            renderDragon.method_4038() as AccessorModelDragon
//        model.body.noDraw = false
//        model.leftWing.noDraw = false
//        model.rightWing.noDraw = false
    }

    fun shouldHideDragonDeath() =
        Utils.inDungeons && DungeonTimer.phase4ClearTime != null && Skytils.config.hideWitherKingDragonDeath
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
    val texture = Identifier.of("skytils", "textures/dungeons/m7/dragon_${name.lowercase()}.png")
    val bb = blockPos.run {
        Box(
            x - dragonOffset,
            y - 8.0,
            z - dragonOffset,
            x + dragonOffset,
            y + dragonOffset + 2,
            z + dragonOffset
        )
    }
    val particleLocation: BlockPos = blockPos.up(5)

    //#if MC>=12111
    //$$ val decalLayer = net.minecraft.client.render.RenderLayers.entityDecal(texture)
    //$$ val cutoutLayer = net.minecraft.client.render.RenderLayers.entityCutoutNoCull(texture)
    //#else
    val decalLayer = RenderLayer.getEntityDecal(texture)
    val cutoutLayer = RenderLayer.getEntityCutoutNoCull(texture)
    //#endif

    companion object {
        const val particleYConstant = 19.0
    }
}

private const val dragonOffset = 13.5