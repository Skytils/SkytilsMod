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
package skytils.skytilsmod.tweaker

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins

@MCVersion("1.8.9")
class FMLLoadingPlugin : IFMLLoadingPlugin {
    override fun getASMTransformerClass() = arrayOf<String>()

    override fun getModContainerClass() = null

    override fun getSetupClass() = null

    override fun injectData(data: Map<String, Any>) {}

    override fun getAccessTransformerClass() = null

    init {
        MixinBootstrap.init()
        Mixins.addConfiguration("mixins.skytils.json")
        MixinEnvironment.getCurrentEnvironment().obfuscationContext = "searge"
        MixinEnvironment.getCurrentEnvironment().side = MixinEnvironment.Side.CLIENT
    }
}