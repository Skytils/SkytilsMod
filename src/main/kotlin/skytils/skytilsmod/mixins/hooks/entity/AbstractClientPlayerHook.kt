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
package skytils.skytilsmod.mixins.hooks.entity

import gg.essential.universal.UChat
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.ResourceLocation
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.features.impl.misc.SummonSkins.skintextureMap
import skytils.skytilsmod.utils.SuperSecretSettings
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.printDevMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class AbstractClientPlayerHook(player: Any) {

    val that: AbstractClientPlayer

    init {
        that = player as AbstractClientPlayer
    }

    var correctSkin: ResourceLocation? = null
    val isSummonMob: Boolean by lazy {
        if (!Utils.inSkyblock) return@lazy false
        try {
            if (that.name == "Lost Adventurer") {
                val textures = that.gameProfile.properties["textures"].firstOrNull()
                if (textures != null) {
                    return@lazy phoenixSkinObject == textures.value
                }
            }
        } catch (e: Exception) {
            return@lazy false
        }
        return@lazy false
    }

    val summonType: Future<String?> by lazy {
        if (!isSummonMob) return@lazy CompletableFuture.completedFuture(null)
        if (mc.theWorld == null) return@lazy CompletableFuture.completedFuture(null)
        if (that.isInvisible) return@lazy CompletableFuture.completedFuture("shadow_assassin")
        Skytils.threadPool.submit(::findTypeEntity)
    }

    private fun findTypeEntity(): String? {
        Thread.sleep(100L)

        val nearbyEntities = Minecraft.getMinecraft().theWorld.getEntitiesInAABBexcluding(
            that,
            that.entityBoundingBox.expand(1.0, 2.5, 1.0)
        ) { entity: Entity? ->
            printDevMessage(
                "entity name ${entity?.name}",
                "summonskins"
            );entity is EntityArmorStand && entity.hasCustomName()
        }
        printDevMessage("nearby ${nearbyEntities.size}", "summonskins")
        for (entity in nearbyEntities) {
            val name = entity.customNameTag
            val type = typeRegex.matchEntire(name)?.let { result ->
                val e = result.groups["type"]?.value ?: return@let null
                return@let e
            } ?: return null
            printDevMessage("summon type: $type", "summonskins")
            return type.lowercase().replace(' ', '_')
        }
        return null

    }

    fun replaceSkin(cir: CallbackInfoReturnable<ResourceLocation>) {
        if (isSummonMob) {
            if (Skytils.config.fixSummonSkin) {
                if (correctSkin == null) {
                    if (!summonType.isDone) return
                    if (summonType.get() == "lost_adventurer" || summonType.get() == null) {
                        return
                    }
                    // TODO Add support for resource packs
                    printDevMessage("summon type ${summonType.get()}", "summonskins")
                    correctSkin = skintextureMap[summonType.get()]?.resource
                        ?: (if (Skytils.config.usePlayerSkin || SuperSecretSettings.noSychic) mc.thePlayer.locationSkin else sychicSkin).also {
                            UChat.chat(
                                "§cPlease tell a Skytils dev to add a skin for ${summonType.get()}"
                            )
                        }

                }
                cir.setReturnValue(correctSkin)
            } else {
                cir.setReturnValue(if (Skytils.config.usePlayerSkin || SuperSecretSettings.noSychic) mc.thePlayer.locationSkin else sychicSkin)
            }
        }
    }

    fun replaceHasSkin(cir: CallbackInfoReturnable<Boolean>) {
        if (isSummonMob) cir.returnValue = true
    }

    fun replaceSkinType(cir: CallbackInfoReturnable<String>) {
        if (isSummonMob) cir.returnValue =
            if (Skytils.config.usePlayerSkin || SuperSecretSettings.noSychic) mc.thePlayer.skinType else "slim"
    }

    companion object {
        val sychicSkin = ResourceLocation("skytils:sychicskin.png")

        val phoenixSkinObject =
            "eyJ0aW1lc3RhbXAiOjE1NzU0NzAyNzE3MTUsInByb2ZpbGVJZCI6ImRlNTcxYTEwMmNiODQ4ODA4ZmU3YzlmNDQ5NmVjZGFkIiwicHJvZmlsZU5hbWUiOiJNSEZfTWluZXNraW4iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM2YTAzODNhNTI3ODAzZDk5YjY2MmFkMThiY2FjNzhjMTE5MjUwZWJiZmIxNDQ3NWI0ZWI0ZDRhNjYyNzk2YjQifX19"

        val typeRegex = Regex("§a§o\\w+'s (?<type>[\\w ]+) §a(?<health>[\\dkmb.]+)§c❤", RegexOption.IGNORE_CASE)
    }
}