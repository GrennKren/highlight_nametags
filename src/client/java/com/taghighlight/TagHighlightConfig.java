package com.taghighlight;

public class TagHighlightConfig {
    public enum RenderStyle {
        OUTLINE_BOX,
        GLOWING
    }

    public RenderStyle renderStyle = RenderStyle.GLOWING;
}