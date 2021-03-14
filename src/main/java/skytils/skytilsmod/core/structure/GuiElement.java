package skytils.skytilsmod.core.structure;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;

public abstract class GuiElement {
    private static final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
    String name;
    float scale;
    FloatPair pos;

    public GuiElement(String name) {
        this(name, new FloatPair(0, 0));
    }

    public GuiElement (String name, FloatPair fp) {
        this(name, 1.0F, fp);
    }

    public GuiElement(String name, float scale, FloatPair fp) {
        this.name = name;
        this.scale = scale;
        if (GuiManager.GUIPOSITIONS.containsKey(name)) {
            this.pos = GuiManager.GUIPOSITIONS.get(name);
        } else {
            this.pos = fp;
        }
    }

    public abstract void render();

    public abstract void demoRender();

    public float getScale() {
        return this.scale;
    }

    public void setScale(float newScale) {
        this.scale = newScale;
    }

    public abstract boolean getToggled();

    public void setPos(FloatPair newPos) {
        this.pos = newPos;
    }

    public FloatPair getPos() {
        return this.pos;
    }

    public void setPos(int x, int y) {
        float fX = x / (float) sr.getScaledWidth();
        float fY = y / (float) sr.getScaledHeight();
        setPos(fX, fY);
    }

    public void setPos(float x, float y) {
        this.pos = new FloatPair(x, y);
    }

    public String getName() {
        return this.name;
    }

    public float getActualX() {
        int maxX = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
        return maxX * getPos().getX();
    }

    public float getActualY() {
        int maxY = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
        return maxY * getPos().getY();
    }

    public abstract int getHeight();

    public abstract int getWidth();
}
