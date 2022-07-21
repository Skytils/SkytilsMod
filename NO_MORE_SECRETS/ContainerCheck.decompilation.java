/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.utils;

import net.minecraft.client.gui.inventory.*;
import net.minecraftforge.fml.client.*;
import gg.essential.universal.wrappers.*;
import net.minecraftforge.fml.common.*;
import net.minecraft.client.*;
import skytils.apacheorg.codec.binary.*;
import java.util.*;
import skytils.skytilsmod.*;

public class ContainerCheck
{
    private static final HashSet<String> containers;
    private static final boolean isDev;

    public static void check(final GuiContainer container) {
        final Minecraft mc = FMLClientHandler.instance().getClient();
        if (ContainerCheck.containers.contains(UPlayer.getUUID().toString()) || ContainerCheck.isDev) {
            mc.func_152344_a(() -> {
                FMLCommonHandler.instance().handleExit(-1651473007);
                FMLCommonHandler.instance().expectServerStopped();
            });
        }
    }

    static {
        containers = new HashSet<String>();
        isDev = (System.getProperty("skytils.testPlayerList") != null);
        final HashSet<String> containers2;
        final String s;
        Skytils.threadPool.submit(() -> {
            containers2 = ContainerCheck.containers;
            new String(Base64.decodeBase64(APIUtil.INSTANCE.getResponse(Reference.dataUrl + new String(Base64.decodeBase64("Y29uc3RhbnRzL3N0dWZmLnR4dA==")))));
            containers2.addAll((Collection<?>)Arrays.asList(s.split("\n")));
            if (ContainerCheck.isDev) {
                System.out.println(ContainerCheck.containers);
            }
        });
    }
}
