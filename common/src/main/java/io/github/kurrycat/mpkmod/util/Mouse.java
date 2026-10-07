package io.github.kurrycat.mpkmod.util;

public class Mouse {
    public enum Button {
        NONE(-1),
        LEFT(0),
        RIGHT(1),
        WHEEL(2),
        BUTTON_4(3),
        BUTTON_5(4);

        public final int value;

        Button(int v) {
            this.value = v;
        }

        public boolean equals(int v) {
            return v == this.value;
        }

        public static Button fromInt(int v) {
            for (Button b : values())
                if (b.value == v) return b;
            return NONE;
        }

        public static Button fromIntLatest(int v) {
            switch (v) {
                case 1:
                    return LEFT;
                case 2:
                    return WHEEL;
                case 3:
                    return RIGHT;
                case 4: // TODO Test 4 & 5
                    return BUTTON_4;
                case 5:
                    return BUTTON_5;
                default:
                    return NONE;
            }
        }
    }

    public enum State {
        DOWN,
        DRAG,
        UP,
        NONE;
    }
}