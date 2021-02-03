package skytils.skytilsmod.features.impl.dungeons;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

public class DungeonsFeatures {
    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre event) {
        if (Utils.inDungeons) {
            if (Skytils.config.showHiddenFels && event.entity.isInvisible() && event.entity instanceof EntityEnderman) {
                event.entity.setInvisible(false);
            }
        }
    }
}
