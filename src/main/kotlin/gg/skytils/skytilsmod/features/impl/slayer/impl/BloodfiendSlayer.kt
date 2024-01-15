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

package gg.skytils.skytilsmod.features.impl.slayer.impl

import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.features.impl.slayer.base.Slayer
import net.minecraft.client.entity.EntityOtherPlayerMP

class BloodfiendSlayer(entity: EntityOtherPlayerMP) :
    Slayer<EntityOtherPlayerMP>(entity, "Riftstalker Bloodfiend", "§c☠ §4Bloodfiend") {

    var lastHadTwinclaws = false
    var isStakeable = false
    private val stakeTitle = "§cSteak Stake!"

    fun nameEntityChanged(newName: String) {
        if (!isStakeable && newName.contains("҉")) {
            isStakeable = true
            if (Config.oneShotAlert) GuiManager.createTitle(stakeTitle, 10)
        } else {
            isStakeable = false
        }
    }

    fun timerEntityChanged(newName: String) {
        if (!lastHadTwinclaws && newName.contains("TWINCLAWS")) {
            if (Config.twinclawAlert && GuiManager.title != stakeTitle) GuiManager.createTitle("§6§lTWINCLAWS!", 10)
            lastHadTwinclaws = true
        } else {
            lastHadTwinclaws = false
        }
    }
}