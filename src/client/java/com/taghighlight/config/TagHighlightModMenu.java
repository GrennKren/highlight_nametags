package com.taghighlight.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.taghighlight.TagHighlightClient;
import com.taghighlight.TagHighlightConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class TagHighlightModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TagHighlightConfigScreen::new;
    }

    // Config screen implementation
    public static class TagHighlightConfigScreen extends Screen {
        private final Screen parent;

        public TagHighlightConfigScreen(Screen parent) {
            super(Text.translatable("text.tag-highlight.config"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            // Render style cycling button
            CyclingButtonWidget<TagHighlightConfig.RenderStyle> renderStyleButton = CyclingButtonWidget.<TagHighlightConfig.RenderStyle>builder(style ->
                            Text.translatable("options.tag-highlight.render_style." + style.name().toLowerCase()))
                    .values(TagHighlightConfig.RenderStyle.values())
                    .initially(TagHighlightClient.CONFIG.renderStyle)
                    .build(this.width / 2 - 100, this.height / 6 + 24, 200, 20, Text.translatable("options.tag-highlight.render_style"), (button, value) -> {
                        TagHighlightClient.CONFIG.renderStyle = value;
                    });

            // Done button
            ButtonWidget doneButton = ButtonWidget.builder(ScreenTexts.DONE, button -> {
                // Save config and close
                TagHighlightClient.saveConfig();
                this.client.setScreen(this.parent);
            }).dimensions(this.width / 2 - 100, this.height / 6 + 168, 200, 20).build();

            this.addDrawableChild(renderStyleButton);
            this.addDrawableChild(doneButton);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
            super.render(context, mouseX, mouseY, delta);
        }
    }
}