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

package gg.skytils.skytilsmod.asm

import dev.falsehonesty.asmhelper.BaseClassTransformer
import gg.skytils.skytilsmod.asm.transformers.*
import net.minecraft.launchwrapper.LaunchClassLoader
import java.util.*

class SkytilsTransformer : BaseClassTransformer() {

    companion object {
        /*
         * Key is srg name, value is deobf name
        */
        val methodMaps: WeakHashMap<String, String> = WeakHashMap()
        var madeTransformers = false
    }

    override fun setup(classLoader: LaunchClassLoader) {
        methodMaps + mapOf(
            "func_150254_d" to "getFormattedText",
            "func_145748_c_" to "getDisplayName",
            "func_177067_a" to "renderName"
        )
    }

    override fun makeTransformers() {
        if (!madeTransformers) {
            madeTransformers = true
            try {
                addColoredNamesCheck()
                injectSplashProgressTransformer()
                changeRenderedName()
                insertReceivePacketEvent()
                injectNullCheck()
                commitArson()
                injectScoreboardScoreRemover()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}