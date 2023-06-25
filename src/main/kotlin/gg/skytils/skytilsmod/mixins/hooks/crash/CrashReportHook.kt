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

package gg.skytils.skytilsmod.mixins.hooks.crash

import com.google.common.base.Objects
import gg.skytils.skytilsmod.utils.ReflectionHelper
import gg.skytils.skytilsmod.utils.ReflectionHelper.getFieldHelper
import gg.skytils.skytilsmod.utils.countMatches
import gg.skytils.skytilsmod.utils.startsWithAny
import net.minecraft.crash.CrashReport
import net.minecraft.crash.CrashReportCategory
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo
import org.spongepowered.asm.mixin.transformer.Config
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged

class CrashReportHook(private val crash: CrashReport) {

    private var isSkytilsCrash = false

    fun checkSkytilsCrash(cir: CallbackInfoReturnable<String>, stringbuilder: StringBuilder) {
        runCatching {
            if (!isSkytilsCrash && crash.causeStackTraceOrString.split("\n").any { line ->
                    (line.contains("gg.skytils.skytilsmod") || line.contains("gg/skytils/skytilsmod")) && !line.contains(
                        "SkytilsSecurityManager"
                    ) && !line.contains(
                        "SkytilsTweaker"
                    )
                }) {
                isSkytilsCrash = true
            }

            if (isSkytilsCrash) stringbuilder.append("Skytils may have caused this crash.\nJoin the Discord for support at discord.gg/skytils\n")

            val registry =
                InjectionInfo::class.java.getFieldHelper("registry")?.get(null) as? Map<*, *> ?: return
            val prefixField = ReflectionHelper.getFieldFor(
                "org.spongepowered.asm.mixin.injection.struct.InjectionInfo\$InjectorEntry",
                "prefix"
            ) ?: return
            val prefixes = registry.values.map {
                "${prefixField.get(it)}$"
            }
            crash.crashCause.stackTrace.filter {
                it.methodName.countMatches("$") >= 2 && it.methodName.startsWithAny(prefixes)
            }.mapNotNullTo(hashSetOf()) {
                val method = ReflectionHelper.getClassHelper(it.className)?.declaredMethods?.find { m ->
                    m.name == it.methodName
                } ?: return@mapNotNullTo null
                if (!method.isAnnotationPresent(MixinMerged::class.java)) return@mapNotNullTo null
                val annotation = method.getDeclaredAnnotation(MixinMerged::class.java) ?: return@mapNotNullTo null
                return@mapNotNullTo it to annotation.mixin
            }.also { l ->
                if (l.isNotEmpty()) {
                    stringbuilder.apply {
                        append("***Skytils detected Mixins in this crash***\n")
                        @Suppress("UNCHECKED_CAST")
                        val configs = Config::class.java.getFieldHelper("allConfigs")
                            ?.get(null) as Map<String, Config>
                        l.forEach { (e, c) ->
                            configs.values.find { c.startsWith(it.config.mixinPackage) }.also {
                                if (it != null) {
                                    append("Mixin registrant ${it.config.name}, class $c, transformed ${e.className}.${e.methodName}\n")
                                } else {
                                    append("Mixin Class $c, transformed ${e.className}.${e.methodName}\n")
                                }
                            }
                        }
                    }
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
            return@addCrashSectionCallable Objects
                .toStringHelper("")
                .add("HasBetterFPS", hasBetterFPS != "NONE")
                .add("BetterFPS Version", hasBetterFPS)
                .add(
                    "Disabled Start Checks", System.getProperty(
                        "skytils.skipStartChecks"
                    ) != null
                )
                .toString()
        }
    }
}