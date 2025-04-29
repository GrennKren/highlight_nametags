// Update src/client/java/com/taghighlight/TagHighlightConfig.java
package com.taghighlight;

public class TagHighlightConfig {
    // Warna outline (nilai dari 0.0f sampai 1.0f)
    public float outlineRed = 1.0f;
    public float outlineGreen = 0.0f;
    public float outlineBlue = 0.0f;
    public float outlineAlpha = 1.0f;

    // Toggle apakah outline ditampilkan
    public boolean outlineEnabled = true;

    // Outline style (0 = box outline, 1 = entity outline)
    public int outlineStyle = 0;

    public boolean preventDuplicateNamesEnabled = true;

    // Toggle stats mode
    public boolean statsMode = false;

    // Stats mode colors
    public float statsModeRed = 0.0f;
    public float statsModeGreen = 1.0f;
    public float statsModeBlue = 1.0f;
    public float statsModeAlpha = 1.0f;
}