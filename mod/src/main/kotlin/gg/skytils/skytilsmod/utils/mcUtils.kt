/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.utils

import gg.essential.elementa.unstable.state.v2.State
import gg.essential.universal.wrappers.UPlayer
import gg.skytils.skytilsmod.Skytils
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i

//#if MC>=12000
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.Optional
//#endif

//#if FORGE
//$$ import net.minecraft.launchwrapper.Launch
//$$ import net.minecraftforge.client.ClientCommandHandler
//$$ import net.minecraftforge.fml.common.Loader
//#endif

//#if FABRIC
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.OrderedText

//#endif

val isDeobfuscatedEnvironment = State {
    //#if FORGE
    //#if MC<11400
    //$$ Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false) as Boolean
    //#else
    //$$ (System.getenv("target") ?: "").lowercase() == "fmluserdevclient"
    //#endif
    //#else
    FabricLoader.getInstance().isDevelopmentEnvironment
    //#endif
}

fun isModLoaded(id: String) =
    //#if FORGE
    //#if MC<11400
    //$$ Loader.isModLoaded(id)
    //#else
    //$$ FMLLoader.getLoadingModList().getModFileById(id)
    //#endif
    //#else
    FabricLoader.getInstance().isModLoaded(id)
    //#endif

fun runClientCommand(command: String): Int
    //#if MC<11400
    //$$ = ClientCommandHandler.instance.method_0_6233(UPlayer.getPlayer(), command)
    //#else
    {
        return try {
            ClientCommandManager.getActiveDispatcher()?.execute(command.removePrefix("/"), UPlayer.getPlayer()?.networkHandler?.commandSource as? FabricClientCommandSource ?: error("No command source")) ?: error("Tried to run a client command when not in a world")
        } catch (_: com.mojang.brigadier.exceptions.CommandSyntaxException) {
            0
        } catch (throwable: Throwable) {
            throw throwable
        }
    }
    //#endif

fun isTimechangerLoaded() =
    //#if FORGE
    //$$ Loader.instance().activeModList.any { it.modId == "timechanger" && it.version == "1.0" }
    //#else
    false
    //#endif

operator fun ClientPlayerEntity.component1() = this.x
operator fun ClientPlayerEntity.component2() = this.y
operator fun ClientPlayerEntity.component3() = this.z

inline fun BlockPos(vec: Vec3d): BlockPos = BlockPos(vec.x, vec.y, vec.z)
inline fun BlockPos(x: Double, y: Double, z: Double): BlockPos = BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z))

inline fun Vec3d(pos: Vec3i): Vec3d = Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

fun GenericContainerScreen.getSlot(id: Int) =
    //#if MC<12000
    //$$ handler.getSlot(id)
    //#else
    screenHandler.getSlot(id)
    //#endif

val ItemStack.displayNameStr: String
    inline get() = this.name
        //#if MC>=11600
        .formattedText
        //#endif

//#if MC>=12000
val Text.formattedText: String
    get() = buildString {
        append(serializeFormattingToString(style))
        this@formattedText.content.visit<String> {
            append(it)
            Optional.empty()
        }
        append("§r")
        siblings.forEach { append(it.formattedText) }
    }

fun formattingFromHex(hex: String): Formatting? {
    val colorInt = hex.removePrefix("#").toIntOrNull(16) ?: return null
    return Formatting.entries.firstOrNull {
        it.colorValue == colorInt
    }
}

fun serializeFormattingToString(style: Style): String = buildString {
    style.color?.name
        ?.let { name ->
            Formatting.byName(name)?.toString() // Done first because most text will use this
                ?: when {
                    Skytils.usingAaronMod && name.equals("#AA5500", ignoreCase = true) ->
                        "§z"

                    name.startsWith('#') ->
                        formattingFromHex(name)?.toString() // TODO: Support all hex codes with a custom system like §#

                    else -> null
                }
        }
        ?.let(::append)
    if (style.isBold) append("§l")
    if (style.isItalic) append("§o")
    if (style.isUnderlined) append("§n")
    if (style.isObfuscated) append("§k")
    if (style.isStrikethrough) append("§m")
}

val OrderedText.string
    get() = buildString {
        this@string.accept { index: Int, style: Style, codePoint: Int ->
            this.appendCodePoint(codePoint)
            true
        }
    }

// Rough representation of `OrderedText` as `Text`.
fun OrderedText.asText(): Text {
    return Text.empty().also { component ->
        var prevStyle = Style.EMPTY
        val currString = StringBuilder()
        this.accept { index: Int, style: Style, codePoint: Int ->
            if (style != prevStyle) {
                component.append(Text.literal(currString.toString()).fillStyle(prevStyle))
                currString.clear()
            }
            prevStyle = style
            currString.append(component)
            true
        }
        if (currString.isNotEmpty()) {
            component.append(Text.literal(currString.toString()).fillStyle(prevStyle))
        }
    }
}
//#endif