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

package gg.skytils.skytilsmod.earlytweaker;

import gg.skytils.earlytweaker.IEarlyTweaker;
import gg.skytils.earlytweaker.IEarlyTweakerRegistrant;

import java.util.Collection;
import java.util.Collections;

public class SkytilsEarlyTweakerRegistrant implements IEarlyTweakerRegistrant {
    @Override
    public Collection<Class<? extends IEarlyTweaker>> getEarlyTweakers() {
        return Collections.singletonList(SkytilsEarlyTweaker.class);
    }
}
