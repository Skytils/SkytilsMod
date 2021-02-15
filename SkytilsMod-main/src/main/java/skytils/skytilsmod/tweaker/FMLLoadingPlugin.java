package skytils.skytilsmod.tweaker;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import skytils.skytilsmod.ModCoreInstaller;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
public class FMLLoadingPlugin implements IFMLLoadingPlugin {

    public FMLLoadingPlugin() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.skytils.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        int initialize = ModCoreInstaller.initialize(Launch.minecraftHome, "1.8.9");

        if (ModCoreInstaller.isErrored() || initialize != 0 && initialize != -1) {
            System.out.println("Failed to load Sk1er Modcore - " + initialize + " - " + ModCoreInstaller.getError());
        }
        // If true the classes are loaded
        if (ModCoreInstaller.isIsRunningModCore()) {
            // register ModCore's class transformer
            return new String[]{"club.sk1er.mods.core.forge.ClassTransformer"};
        }

        return new String[]{};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}