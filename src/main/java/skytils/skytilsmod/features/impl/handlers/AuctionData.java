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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.APIUtil;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AuctionData {

    public static final String dataURL = "https://sbe-stole-skytils.design/api/auctions/lowestbins";
    public static final HashMap<String, Double> lowestBINs = new HashMap<>();
    public static final StopWatch reloadTimer = new StopWatch();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String getIdentifier(@NotNull ItemStack item) {
        NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
        String id = ItemUtil.getSkyBlockItemID(extraAttr);
        switch (id) {
            case "PET":
                if (extraAttr.hasKey("petInfo")) {
                    JsonObject petInfo = gson.fromJson(extraAttr.getString("petInfo"), JsonObject.class);
                    if (petInfo.has("type") && petInfo.has("tier")) {
                        id = "PET-" + petInfo.get("type").getAsString() + "-" + petInfo.get("tier").getAsString();
                    }
                }
            break;
            case "ENCHANTED_BOOK":
                if (extraAttr.hasKey("enchantments")) {
                    NBTTagCompound enchants = extraAttr.getCompoundTag("enchantments");
                    if (!enchants.hasNoTags()) {
                        String enchant = enchants.getKeySet().iterator().next();
                        id = "ENCHANTED_BOOK-" + enchant + "-" + enchants.getInteger(enchant);
                    }
                }
            break;
            case "POTION":
                if (extraAttr.hasKey("potion") && extraAttr.hasKey("potion_level")) {
                    id = "POTION-" + extraAttr.getString("potion").toUpperCase(Locale.US) + "-" + extraAttr.getInteger("potion_level") + (extraAttr.hasKey("enhanced") ? "-ENHANCED" : "") + (extraAttr.hasKey("extended") ? "-EXTENDED" : "") + (extraAttr.hasKey("splash") ? "-SPLASH" : "");
                }
            break;
        }
        return id;
    }

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
