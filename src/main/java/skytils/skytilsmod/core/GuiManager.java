package skytils.skytilsmod.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.GuiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiManager {
    private final Map<Integer, GuiElement> elements = new HashMap<>();
    private int counter = 0;
    private final Map<String, GuiElement> names = new HashMap<>();

    public boolean registerElement(GuiElement e) {
        try {
            counter++;
            elements.put(counter, e);
            names.put(e.getName(), e);
            return true;
        } catch(Exception err) {
            err.printStackTrace();
            return false;
        }
    }

    public GuiElement getByID(Integer ID) {
        return elements.get(ID);
    }

    public GuiElement getByName(String name) {
        return names.get(name);
    }

    public List<GuiElement> searchElements(String query) {
        List<GuiElement> results = new ArrayList<>();
        for(Map.Entry<String, GuiElement> e : names.entrySet()) {
            if(e.getKey().equals(query)) results.add(e.getValue());
        }
        return results;
    }

    public Map<Integer,GuiElement> getElements() {
        return this.elements;
    }

    @SubscribeEvent
    public void renderPlayerInfo(final RenderGameOverlayEvent.Post event) {
        if (Skytils.usingLabymod && !(Minecraft.getMinecraft().ingameGUI instanceof GuiIngameForge)) return;
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE && event.type != RenderGameOverlayEvent.ElementType.JUMPBAR)
            return;
        for(Map.Entry<Integer, GuiElement> e : elements.entrySet()) {
            try {
                e.getValue().render();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // LabyMod Support
    @SubscribeEvent
    public void renderPlayerInfoLabyMod(final RenderGameOverlayEvent event) {
        if (!Skytils.usingLabymod) return;
        if (event.type != null) return;
        for(Map.Entry<Integer, GuiElement> e : elements.entrySet()) {
            try {
                e.getValue().render();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
