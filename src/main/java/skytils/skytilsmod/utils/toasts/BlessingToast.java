package skytils.skytilsmod.utils.toasts;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;

import java.nio.FloatBuffer;
import java.util.List;

public class BlessingToast implements IToast<BlessingToast> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("skytils:gui/toast.png");
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
    private final long maxDrawTime;
    private final Blessing blessing;
    private final List<BlessingBuff> buffs;

    public BlessingToast(String blessing, List<BlessingBuff> buffs) {
        this.maxDrawTime = Skytils.config.toastTime;
        this.blessing = Blessing.fromName(blessing);
        this.buffs = buffs;
    }

    @Override
    public IToast.Visibility draw(GuiToast toastGui, long delta) {

        toastGui.mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);

        RenderHelper.disableStandardItemLighting();

        toastGui.mc.fontRendererObj.drawStringWithShadow(this.blessing.getFormattedName(), 30, 7, 16777215);

        StringBuilder buffStats = new StringBuilder();
        for (BlessingBuff buff : this.buffs) {
            String color;
            switch (buff.symbol) {
                case "❤":
                case "❁":
                    color = EnumChatFormatting.RED.toString();
                    break;
                case "✎":
                    color = EnumChatFormatting.AQUA.toString();
                    break;
                case "❈":
                case "HP":
                    color = EnumChatFormatting.GREEN.toString();
                    break;
                case "✦":
                    color = EnumChatFormatting.WHITE.toString();
                    break;
                case "☠":
                    color = EnumChatFormatting.BLUE.toString();
                    break;
                default:
                    color = EnumChatFormatting.GRAY.toString();
            }
            buffStats.append(color).append(buff.amount).append(buff.symbol).append(" ");
        }
        GuiToast.drawSubline(toastGui, delta, 0L, this.maxDrawTime, this.buffer, buffStats.toString(), false);
        RenderHelper.enableGUIStandardItemLighting();

        RenderUtil.renderTexture(this.blessing.texture, 8, 8);

        GlStateManager.disableLighting();
        return delta >= this.maxDrawTime ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }

    private enum Blessing {
        LIFE("life", "skytils:toasts/blessings/life.png", "§c§lLIFE BLESSING!"),
        POWER("power", "skytils:toasts/blessings/power.png", "§5§lPOWER BLESSING!"),
        STONE("stone", "skytils:toasts/blessings/stone.png", "§a§lSTONE BLESSING!"),
        WISDOM("wisdom", "skytils:toasts/blessings/wisdom.png", "§b§lWISDOM BLESSING!"),
        TIME("time", "skytils:toasts/blessings/time.png", "§6§lTIME BLESSING!");

        private final ResourceLocation texture;
        private final String name;
        private final String formattedName;

        Blessing(String name, String location, String formattedName) {
            this.name = name;
            this.texture = new ResourceLocation(location);
            this.formattedName = formattedName;
        }

        public static Blessing fromName(String name) {
            for (Blessing type : values()) {
                if (type.name.equals(name))
                    return type;
            }
            return null;
        }

        public String getFormattedName() {
            return this.formattedName;
        }
    }

    public static class BlessingBuff {

        public String amount;
        public String symbol;

        public BlessingBuff(String amount, String symbol) {
            this.amount = amount;
            this.symbol = symbol;
        }
    }
}
