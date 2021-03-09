package skytils.skytilsmod.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.features.impl.dungeons.solvers.ThreeWeirdosSolver;
import skytils.skytilsmod.features.impl.dungeons.solvers.TriviaSolver;
import skytils.skytilsmod.features.impl.mining.MiningFeatures;
import skytils.skytilsmod.features.impl.spidersden.RelicWaypoints;
import skytils.skytilsmod.utils.APIUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.Map;

public class DataFetcher {

    private static long lastReload = 0;

    private static void loadData() {
        String dataUrl = Skytils.config.dataURL;
        new Thread(() -> {
            JsonObject fetchurData = APIUtil.getJSONResponse(dataUrl + "solvers/fetchur.json");
            for (Map.Entry<String, JsonElement> solution : fetchurData.entrySet()) {
                MiningFeatures.fetchurItems.put(solution.getKey(), solution.getValue().getAsString());
            }

            JsonArray threeWeirdosSolutions = APIUtil.getArrayResponse(dataUrl + "solvers/threeweirdos.json");
            for (JsonElement solution : threeWeirdosSolutions) {
                ThreeWeirdosSolver.solutions.add(solution.getAsString());
            }

            JsonObject triviaData = APIUtil.getJSONResponse(dataUrl + "solvers/oruotrivia.json");
            for (Map.Entry<String, JsonElement> solution : triviaData.entrySet()) {
                TriviaSolver.triviaSolutions.put(solution.getKey(), getStringArrayFromJsonArray(solution.getValue().getAsJsonArray()));
            }

            JsonArray relicData = APIUtil.getArrayResponse(dataUrl + "constants/relics.json");
            for (int i = 0; i < relicData.size(); i++) {
                JsonArray coordsArr = relicData.get(i).getAsJsonArray();
                RelicWaypoints.relicLocations.add(new BlockPos(coordsArr.get(0).getAsInt(), coordsArr.get(1).getAsInt(), coordsArr.get(2).getAsInt()));
            }

        }).start();
    }

    private static void clearData() {
        MiningFeatures.fetchurItems.clear();
        RelicWaypoints.relicLocations.clear();
        ThreeWeirdosSolver.solutions.clear();
        TriviaSolver.triviaSolutions.clear();
    }

    public static void reloadData() {
        clearData();
        loadData();
    }

    private static String[] getStringArrayFromJsonArray(JsonArray jsonArray) {
        int arraySize = jsonArray.size();
        String[] stringArray = new String[arraySize];

        for (int i = 0; i < arraySize; i++) {
            stringArray[i] = jsonArray.get(i).getAsString();
        }

        return stringArray;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!Utils.inSkyblock) return;
        if (System.currentTimeMillis() - lastReload > 60 * 60 * 1000) {
            lastReload = System.currentTimeMillis();
            reloadData();
        }
    }

}
