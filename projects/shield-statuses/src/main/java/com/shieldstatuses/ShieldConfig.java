package com.shieldstatuses;

import net.minecraftforge.common.ForgeConfigSpec;

public class ShieldConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.ConfigValue<String> READY_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> DISABLED_COLOR;
    public static final ForgeConfigSpec.BooleanValue TINT_READY;
    public static final ForgeConfigSpec.BooleanValue INTERPOLATE;
    public static final ForgeConfigSpec.BooleanValue SHOW_OTHER_PLAYERS;
    public static final ForgeConfigSpec.BooleanValue SHOW_IN_GUI;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("shieldstatuses");

        ENABLED = b.comment("Turn the whole mod on/off")
                .define("enabled", true);
        READY_COLOR = b.comment("Colour of a shield that can block (hex, like #55FF55)")
                .define("readyColor", "#55FF55");
        DISABLED_COLOR = b.comment("Colour of a disabled shield (hex, like #FF4040)")
                .define("disabledColor", "#FF4040");
        TINT_READY = b.comment("Tint shields with the ready colour when they are NOT disabled. If false, ready shields look normal.")
                .define("tintReadyShields", true);
        INTERPOLATE = b.comment("true = fade from red to green while the shield recovers, false = solid red until it is ready")
                .define("interpolate", false);
        SHOW_OTHER_PLAYERS = b.comment("Show the status of other players' shields")
                .define("showOtherPlayers", true);
        SHOW_IN_GUI = b.comment("Also tint your shield in the hotbar / inventory")
                .define("showInGui", true);

        b.pop();
        SPEC = b.build();
    }

    /** "#RRGGBB" / "0xRRGGBB" / "RRGGBB" -> int, or fallback if the text is broken. */
    public static int parse(String text, int fallback) {
        try {
            String s = text.trim();
            if (s.startsWith("#")) {
                s = s.substring(1);
            }
            if (s.startsWith("0x") || s.startsWith("0X")) {
                s = s.substring(2);
            }
            return Integer.parseInt(s, 16) & 0xFFFFFF;
        } catch (Exception e) {
            return fallback;
        }
    }
}
