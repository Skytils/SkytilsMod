package skytils.skytilsmod.utils;

public class EnumUtil {
    public enum AnchorPoint {

        TOP_LEFT(0),
        TOP_RIGHT(1),
        BOTTOM_LEFT(2),
        BOTTOM_RIGHT(3),
        BOTTOM_MIDDLE(4);

        private int id;

        AnchorPoint(int id) {
            this.id = id;
        }

        @SuppressWarnings("unused") // Accessed by reflection...
        public static AnchorPoint fromId(int id) {
            for (AnchorPoint feature : values()) {
                if (feature.getId() == id) {
                    return feature;
                }
            }
            return null;
        }

        public int getId() {
            return this.id;
        }

        public int getX(int maxX) {
            int x = 0;
            switch (this) {
                case TOP_RIGHT: case BOTTOM_RIGHT:
                    x = maxX;
                    break;
                case BOTTOM_MIDDLE:
                    x = maxX / 2;
                    break;

            }
            return x;
        }

        public int getY(int maxY) {
            int y = 0;
            switch (this) {
                case BOTTOM_LEFT: case BOTTOM_RIGHT: case BOTTOM_MIDDLE:
                    y = maxY;
                    break;

            }
            return y;
        }
    }
}
