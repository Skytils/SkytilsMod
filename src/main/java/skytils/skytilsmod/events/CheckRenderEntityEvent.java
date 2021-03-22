package skytils.skytilsmod.events;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Cancelable
public class CheckRenderEntityEvent<T extends Entity> extends Event {

    public T entity;
    public ICamera camera;
    public double camX, camY, camZ;

    public CheckRenderEntityEvent(T entity, ICamera camera, double camX, double camY, double camZ) {
        this.entity = entity;
        this.camera = camera;
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
    }

}
