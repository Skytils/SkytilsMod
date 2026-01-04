/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.commands

import gg.skytils.skytilsmod.commands.impl.*
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.MinecraftClient
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.fabric.FabricClientCommandManager
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport

//#if MC==10809 && FORGE
//$$ import gg.skytils.skytilsmod.commands.utils.legacy.LegacyForgeRegistrationHandler
//$$ import org.incendo.cloud.CommandManager
//#endif


//#if MC==10809 && FABRIC
//$$ object SkytilsCommands : CommandManager<SkytilsCommandSender>(ExecutionCoordinator.simpleCoordinator(),
//$$    LegacyForgeRegistrationHandler
//$$ ) {
//#endif
object SkytilsCommands {
    val commandManager = FabricClientCommandManager.createNative(ExecutionCoordinator.simpleCoordinator())
    val annotationParser = AnnotationParser(
        //#if MC==10809 && FORGE
        //$$ this,
        //#else
        commandManager,
        //#endif
        SkytilsCommandSender::class.java)

    init {
        runCatching {
            annotationParser.installCoroutineSupport(context = MinecraftClient.getInstance().asCoroutineDispatcher())

            val parsedCommands = annotationParser.parse(
                ArmorColorCommand,
                CalcXPCommand,
                CataCommand,
                GlintCustomizeCommand,
                HollowWaypointCommand,
                ItemCycleCommand,
                OrderedWaypointCommand,
                ProtectItemCommand,
                RepartyCommand,
                ScamCheckCommand,
                SkytilsCommand,
                SlayerCommand,
                TrackCooldownCommand,
                TrophyFishCommand
            )
            println("Parsed ${parsedCommands.size} commands.")
        }.onFailure {
            it.printStackTrace()
        }
    }

    //#if MC==10809 && FORGE
    //$$ override fun hasPermission(
    //$$    sender: SkytilsCommandSender,
    //$$    permission: String
    //$$ ): Boolean = true
    //#endif
}