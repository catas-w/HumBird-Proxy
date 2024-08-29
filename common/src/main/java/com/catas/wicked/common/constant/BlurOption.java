package com.catas.wicked.common.constant;

import lombok.Getter;

@Getter
public enum BlurOption {

    AQUA("NSAppearanceNameAqua", "Aqua"),

    DARK_AQUA("NSAppearanceNameDarkAqua", "AquDark Aqua"),

    VIBRANT_LIGHT("NSAppearanceNameVibrantLight", "Vibrant Light"),

    VIBRANT_DARK("NSAppearanceNameVibrantDark", "Vibrant Dark"),

    HIGH_CONTRAST_AQUA("NSAppearanceNameAccessibilityHighContrastAqua",
            "Accessibility High Contrast Aqua"),

    HIGH_CONTRAST_DARK_AQUA("NSAppearanceNameAccessibilityHighContrastDarkAqua",
            "Accessibility High Contrast Dark Aqua"),

    HIGH_CONTRAST_VIBRANT_LIGHT("NSAppearanceNameAccessibilityHighContrastVibrantLight",
            "Accessibility High Contrast Vibrant Light"),

    HIGH_CONTRAST_VIBRANT_DARK("NSAppearanceNameAccessibilityHighContrastVibrantDark",
            "Accessibility High Contrast Vibrant Dark"),
    ;

    private final String nativeName;

    private final String desc;

    BlurOption(String nativeName, String desc) {
        this.nativeName = nativeName;
        this.desc = desc;
    }
}
