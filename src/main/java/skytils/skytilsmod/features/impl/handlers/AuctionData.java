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

package skytils.skytilsmod.features.impl.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.time.StopWatch;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.APIUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class AuctionData {

    public static final String dataURL = "https://sbe-stole-skytils.design/api/auctions/lowestbins";
    public static final HashMap<String, Double> lowestBINs = new HashMap<>();
    public static final StopWatch reloadTimer = new StopWatch();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !Utils.inSkyblock) return;
        if (!reloadTimer.isStarted()) reloadTimer.start();
        if (reloadTimer.getTime() >= 60000) {
            reloadTimer.reset();
            if (Skytils.config.showLowestBINPrice) {
                new Thread(() -> {
                    JsonObject data = APIUtil.getJSONResponse(dataURL);
                    for (Map.Entry<String, JsonElement> items : data.entrySet()) {
                        lowestBINs.put(items.getKey(), items.getValue().getAsDouble());
                    }
                }, "Skytils-FetchAuctionData").start();
            }
        }
    }

}
