package com.taghighlight.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.taghighlight.TagHighlightClient;
import com.taghighlight.TagHighlightConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class TagHighlightModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TagHighlightConfigScreen::new;
    }

    public static class TagHighlightConfigScreen extends Screen {
        private final Screen parent;
        private SliderWidget redSlider;
        private SliderWidget greenSlider;
        private SliderWidget blueSlider;
        private SliderWidget alphaSlider;

        // Constants for color preview dimensions
        private static final int PREVIEW_SIZE = 30;
        private static final int PREVIEW_X_OFFSET = 120;
        private static final int PREVIEW_Y_OFFSET = 65;

        public TagHighlightConfigScreen(Screen parent) {
            super(Text.translatable("text.tag-highlight.config"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            // Slider untuk Outline Red
            redSlider = new SliderWidget(this.width / 2 - 100, this.height / 6 + 24, 200, 20,
                    Text.translatable("options.tag-highlight.outline.red", (int)(TagHighlightClient.CONFIG.outlineRed * 255)),
                    TagHighlightClient.CONFIG.outlineRed) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.outlineRed = (float)this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.outline.red", (int)(this.value * 255)));
                }
            };
            this.addDrawableChild(redSlider);

            // Slider untuk Outline Green
            greenSlider = new SliderWidget(this.width / 2 - 100, this.height / 6 + 50, 200, 20,
                    Text.translatable("options.tag-highlight.outline.green", (int)(TagHighlightClient.CONFIG.outlineGreen * 255)),
                    TagHighlightClient.CONFIG.outlineGreen) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.outlineGreen = (float)this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.outline.green", (int)(this.value * 255)));
                }
            };
            this.addDrawableChild(greenSlider);

            // Slider untuk Outline Blue
            blueSlider = new SliderWidget(this.width / 2 - 100, this.height / 6 + 76, 200, 20,
                    Text.translatable("options.tag-highlight.outline.blue", (int)(TagHighlightClient.CONFIG.outlineBlue * 255)),
                    TagHighlightClient.CONFIG.outlineBlue) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.outlineBlue = (float)this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.outline.blue", (int)(this.value * 255)));
                }
            };
            this.addDrawableChild(blueSlider);

            // Slider untuk Outline Alpha
            alphaSlider = new SliderWidget(this.width / 2 - 100, this.height / 6 + 102, 200, 20,
                    Text.translatable("options.tag-highlight.outline.alpha", (int)(TagHighlightClient.CONFIG.outlineAlpha * 100)),
                    TagHighlightClient.CONFIG.outlineAlpha) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.outlineAlpha = (float)this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.outline.alpha", (int)(this.value * 100)));
                }
            };
            this.addDrawableChild(alphaSlider);

            // Tombol toggle untuk mengaktifkan/mematikan outline
            ButtonWidget toggleButton = ButtonWidget.builder(
                    Text.translatable("options.tag-highlight.outline.enabled", TagHighlightClient.CONFIG.outlineEnabled),
                    button -> {
                        TagHighlightClient.CONFIG.outlineEnabled = !TagHighlightClient.CONFIG.outlineEnabled;
                        button.setMessage(Text.translatable("options.tag-highlight.outline.enabled", TagHighlightClient.CONFIG.outlineEnabled));
                    }).dimensions(this.width / 2 - 100, this.height / 6 + 130, 200, 20).build();
            this.addDrawableChild(toggleButton);

            // Tombol Done
            ButtonWidget doneButton = ButtonWidget.builder(ScreenTexts.DONE, button -> {
                // Save config dan kembali ke layar sebelumnya
                TagHighlightClient.saveConfig();
                this.client.setScreen(this.parent);
            }).dimensions(this.width / 2 - 100, this.height / 6 + 168, 200, 20).build();
            this.addDrawableChild(doneButton);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

            // Render color preview box
            int previewX = this.width / 2 + PREVIEW_X_OFFSET;
            int previewY = this.height / 6 + PREVIEW_Y_OFFSET;

            // Calculate color from current slider values
            int red = (int)(TagHighlightClient.CONFIG.outlineRed * 255);
            int green = (int)(TagHighlightClient.CONFIG.outlineGreen * 255);
            int blue = (int)(TagHighlightClient.CONFIG.outlineBlue * 255);
            int alpha = (int)(TagHighlightClient.CONFIG.outlineAlpha * 255);
            int color = (alpha << 24) | (red << 16) | (green << 8) | blue;

            // Draw color preview label
            context.drawTextWithShadow(this.textRenderer, Text.translatable("options.tag-highlight.preview"),
                    previewX - 5, previewY - 15, 0xFFFFFF);

            // Draw outer border (black)
            context.fill(previewX - 2, previewY - 2, previewX + PREVIEW_SIZE + 2, previewY + PREVIEW_SIZE + 2, 0xFF000000);

            // Draw inner border (white)
            context.fill(previewX - 1, previewY - 1, previewX + PREVIEW_SIZE + 1, previewY + PREVIEW_SIZE + 1, 0xFFFFFFFF);

            // Draw color preview with exact pixel values
            context.fill(previewX, previewY, previewX + PREVIEW_SIZE, previewY + PREVIEW_SIZE, color);

            // Draw a grid pattern on transparent backgrounds to better show transparency
            if (TagHighlightClient.CONFIG.outlineAlpha < 1.0f) {
                int gridSize = 5;
                for (int x = 0; x < PREVIEW_SIZE; x += gridSize) {
                    for (int y = 0; y < PREVIEW_SIZE; y += gridSize) {
                        boolean isCheckerboard = (x / gridSize + y / gridSize) % 2 == 0;
                        if (isCheckerboard) {
                            context.fill(
                                    previewX + x,
                                    previewY + y,
                                    Math.min(previewX + x + gridSize, previewX + PREVIEW_SIZE),
                                    Math.min(previewY + y + gridSize, previewY + PREVIEW_SIZE),
                                    0x22000000
                            );
                        }
                    }
                }
            }

            super.render(context, mouseX, mouseY, delta);
        }
    }
}