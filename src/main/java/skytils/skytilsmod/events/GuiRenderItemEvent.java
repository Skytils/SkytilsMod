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

package skytils.skytilsmod.events;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GuiRenderItemEvent extends Event {

    public static class RenderOverlayEvent extends GuiRenderItemEvent {

        public FontRenderer fr;
        public ItemStack stack;
        public int x, y;
        public String text;

        public RenderOverlayEvent(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text) {
            this.fr = fr;
            this.stack = stack;
            this.x = xPosition;
            this.y = yPosition;
            this.text = text;
        }

        public static class Post extends RenderOverlayEvent {
            public Post(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text) {
                super(fr, stack, xPosition, yPosition, text);
            }
        }
    }

}
