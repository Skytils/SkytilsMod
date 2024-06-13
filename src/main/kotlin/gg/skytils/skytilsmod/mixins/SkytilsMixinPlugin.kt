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

package gg.skytils.skytilsmod.mixins

import net.minecraft.launchwrapper.Launch
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

open class SkytilsMixinPlugin : IMixinConfigPlugin {
    open val mixinPackage = "gg.skytils.skytilsmod.mixins.transformers"
    val eventsPackage = "gg.skytils.events.mixins"
    var deobfEnvironment = false

    override fun onLoad(mixinPackage: String) {
        deobfEnvironment = Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false) as Boolean
        if (deobfEnvironment) {
            println("We are in a deobfuscated environment, loading compatibility mixins.")
        }
    }

    override fun getRefMapperConfig(): String? = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (!mixinClassName.startsWith(mixinPackage) && !mixinClassName.startsWith(eventsPackage)) {
            println("Woah, how did mixin $mixinClassName for $targetClassName get here?")
            return false
        }
        if (mixinClassName.startsWith("$mixinPackage.deobfenv") && !deobfEnvironment) {
            println("Mixin $mixinClassName is for a deobfuscated environment, disabling.")
            return false
        }
        return true
    }

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: Set<String>) {
    }

    override fun getMixins(): List<String>? = null

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo?
    ) {
    }
}