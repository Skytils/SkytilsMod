/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.features.impl.misc.damagesplash

import gg.essential.universal.UChat
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.entity.item.EntityArmorStand
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.utils.NumberUtil
import sharttils.sharttilsmod.utils.NumberUtil.format
import sharttils.sharttilsmod.utils.Utils.random
import sharttils.sharttilsmod.utils.graphics.ScreenRenderer
import sharttils.sharttilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import sharttils.sharttilsmod.utils.graphics.SmartFontRenderer.TextShadow
import sharttils.sharttilsmod.utils.graphics.colors.CustomColor
import java.util.*
import java.util.regex.Pattern

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
class DamageSplashEntity(
    private val boundEntity: EntityArmorStand,
    private var damage: String,
    currentLocation: Location
) : FakeEntity(currentLocation) {
    private var displayText: String
    private var scale = 1f
    private var color: CustomColor? = null
    private var love = false
    private var extremeFocus = false
    private var octodexterity = false
    override val name: String
        get() = "EntityDamageSplash"

    override fun tick() {
        if (color == null) {
            println("Failed to parse color for DamageSplashEntity, bound entity '${boundEntity.customNameTag}' | parse string '$damage'")
            UChat.chat("§cSharttils ran into a problem while rendering damage, please report this on our Discord with your log file attached.")
            remove()
            return
        }
        val maxLiving = 150
        if (livingTicks > maxLiving) {
            remove()
            return
        }
        val initialScale = 2.5f

        // makes the text goes down and resize
        currentLocation.subtract(0.0, 2 / maxLiving.toDouble(), 0.0)
        scale = initialScale - livingTicks * initialScale / maxLiving
    }

    override fun render(partialTicks: Float, context: RenderGlobal?, render: RenderManager) {
        if (color == null) return
        val thirdPerson = render.options.thirdPersonView == 2
        ScreenRenderer.isRendering = true
        run {
            run { // setting up
                GlStateManager.rotate(-render.playerViewY, 0f, 1f, 0f) // rotates yaw
                GlStateManager.rotate(
                    (if (thirdPerson) -1 else 1).toFloat() * render.playerViewX,
                    1.0f,
                    0.0f,
                    0.0f
                ) // rotates pitch
                GlStateManager.scale(-0.025f, -0.025f, 0.025f) // size the text to the same size as a nametag
                GlStateManager.scale(scale, scale, scale)
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            }
            renderer.drawString(
                displayText, 0f, 0f, color!! /*CommonColors.RAINBOW*/,
                TextAlignment.MIDDLE, TextShadow.NONE
            )
        }
        ScreenRenderer.isRendering = false
    }

    companion object {
        private val renderer = ScreenRenderer()
        private val added = WeakHashMap<String, UUID>()
        private val SYMBOL_PATTERN = Pattern.compile("(\\d+)(.*)")
    }

    init {
        val symbolMatcher = SYMBOL_PATTERN.matcher(damage)
        if (symbolMatcher.matches()) {
            var symbol = symbolMatcher.group(2)
            damage = symbolMatcher.group(1)
            if (symbol.contains("❤")) {
                love = true
                symbol = symbol.replace("❤", "")
            }
            if (symbol.contains("⚔")) {
                extremeFocus = true
                symbol = symbol.replace("⚔", "")
            }
            if (symbol.contains("+")) {
                octodexterity = true
                symbol = symbol.replace("+", "")
            }
            color = Damage.fromSymbol(symbol)?.color
        }
        displayText =
            "${if (Sharttils.config.commaDamage) NumberUtil.nf.format(damage.toLong()) else format(damage.toLong())}${
                exportStringIfTrue(
                    love,
                    "❤"
                )
            }${
                exportStringIfTrue(
                    extremeFocus,
                    "⚔"
                )
            }${exportStringIfTrue(octodexterity, "+")}"
        val uuid = UUID(random.nextLong(), random.nextLong())
        if (added.containsValue(uuid)) remove() else added[displayText] = uuid
    }
}

private fun exportStringIfTrue(bool: Boolean, str: String): String {
    return if (bool) str else ""
}