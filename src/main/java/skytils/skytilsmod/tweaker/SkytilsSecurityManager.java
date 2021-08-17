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

package skytils.skytilsmod.tweaker;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.FMLSecurityManager;

import java.security.Permission;

public class SkytilsSecurityManager extends FMLSecurityManager {
    @Override
    public void checkPermission(Permission perm) {
        String permName = perm.getName() != null ? perm.getName() : "missing";
        if (permName.startsWith("exitVM") && Minecraft.getMinecraft() == null ) {
            Class<?>[] classContexts = getClassContext();
            String callingClass = classContexts.length > 3 ? classContexts[4].getName() : "none";
            // Skytils: allow the LaunchWrapper to close the game
            if (callingClass.equals("net.minecraft.launchwrapper.Launch")) {
                return;
            }
        } else if ("setSecurityManager".equals(permName)) {
            throw new SecurityException("Cannot replace the FML (Skytils) security manager");
        }
        super.checkPermission(perm);
    }
}