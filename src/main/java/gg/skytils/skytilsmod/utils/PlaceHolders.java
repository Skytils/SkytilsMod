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

package gg.skytils.skytilsmod.utils;

import gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation;
import net.minecraft.client.Minecraft;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PlaceHolders {
    public static String replacePlaceHolder(String s) {
        s = s.replaceAll("%date%", getYYYYMMDD()).
                replaceAll("%player_name%", Minecraft.getMinecraft().thePlayer.getName())
                .replaceAll("%seconds_elapsed%", String.valueOf(ScoreCalculation.INSTANCE.getSecondsElapsed().get()))
                .replaceAll("%deaths%", String.valueOf(ScoreCalculation.INSTANCE.getDeaths().get()));
        return s;
    }

    public static String getYYYYMMDD() {
        Date curdate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(curdate);
    }
}
