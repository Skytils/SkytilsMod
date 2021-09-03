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

package skytils.skytilsmod.mixins.hooks.crash

import net.minecraft.crash.CrashReport
import net.minecraft.crash.CrashReportCategory
import org.spongepowered.asm.mixin.Mixins
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged
import skytils.skytilsmod.utils.countMatches
import skytils.skytilsmod.utils.startsWithAny

class CrashReportHook(private val crash: CrashReport) {

    private var isSkytilsCrash = false

    fun checkSkytilsCrash(cir: CallbackInfoReturnable<String>, stringbuilder: StringBuilder) {
        runCatching {
            if (!isSkytilsCrash && crash.causeStackTraceOrString.split("\n").any { line ->
                    (line.contains("skytils.skytilsmod") || line.contains("skytils/skytilsmod")) && !line.contains("SkytilsSecurityManager") && !line.contains(
                        "SkytilsTweaker"
                    )
                }) {
                isSkytilsCrash = true
            }

            if (isSkytilsCrash) stringbuilder.append("Skytils may have caused this crash.\nJoin the Discord for support at discord.gg/skytils\n")

            crash.crashCause.stackTrace.filter {
                it.methodName.countMatches("$") == 2 && it.methodName.startsWithAny(
                    "modify$",
                    "args$",
                    "redirect$",
                    "localvar$",
                    "constant$",
                    "handler$"
                )
            }.mapNotNull {
                val clazz = Class.forName(it.className)
                val method = clazz.declaredMethods.find { m ->
                    it.methodName == m.name
                } ?: return@mapNotNull null
                method.isAccessible = true
                if (!method.isAnnotationPresent(MixinMerged::class.java)) return@mapNotNull null
                val annotation = method.getDeclaredAnnotation(MixinMerged::class.java)
                return@mapNotNull it to annotation.mixin
            }.distinct().also { l ->
                if (l.isNotEmpty()) {
                    stringbuilder.append(buildString {
                        append("***Skytils detected Mixins in this crash***\n")
                        l.forEach { (e, c) ->
                            Mixins.getConfigs().find { c.startsWith(it.config.mixinPackage) }.also {
                                if (it != null) {
                                    append("Mixin registrant ${it.config.name}, class $c, transformed ${e.className}.${e.methodName}\n")
                                } else {
                                    append("Mixin Class $c, transformed ${e.className}.${e.methodName}\n")
                                }
                            }
                        }
                    })
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun generateCauseForLauncher(theCauseStackTraceOrString: String): String {
        return if (isSkytilsCrash) {
            "Skytils may have caused this crash. Please send the full report below by clicking \"View crash report\" to discord.gg/skytils in the #support channel. Taking a screenshot of this screen provides no information to any mod developers.\n\n$theCauseStackTraceOrString"
        } else theCauseStackTraceOrString
    }

    fun generateWittyComment(comment: String): String {
        return if (isSkytilsCrash) "Did Sychic do that?" else comment
    }

    fun addDataToCrashReport(crashReportCategory: CrashReportCategory) {
        crashReportCategory.addCrashSectionCallable("Skytils Debug Info") {
            val hasBetterFPS = runCatching {
                Class.forName("me.guichaguri.betterfps.BetterFpsHelper").getDeclaredField("VERSION")
                    .also { it.isAccessible = true }
                    .get(null) as String
            }.getOrDefault("NONE")

            return@addCrashSectionCallable """
                            # BetterFPS: ${hasBetterFPS != "NONE"} version: $hasBetterFPS; Disabled Startup Check: ${
                System.getProperty(
                    "skytils.skipStartChecks"
                ) != null
            }
                        """.trimMargin("#")
        }
    }
}