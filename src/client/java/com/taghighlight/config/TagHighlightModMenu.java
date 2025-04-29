package com.taghighlight.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.taghighlight.TagHighlightClient;
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

        // Constants for color preview dimensions
        private static final int PREVIEW_SIZE = 30;
        private static final int PREVIEW_X_OFFSET = 120;
        private static final int PREVIEW_Y_OFFSET = 65;
        private static final int STATS_PREVIEW_Y_OFFSET = 215;

        // Add scrolling variables
        private int scrollOffset = 0;
        private boolean isScrolling = false;
        private final int contentHeight = 350; // Approximate height of all content

        public TagHighlightConfigScreen(Screen parent) {
            super(Text.translatable("text.tag-highlight.config"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            // Clear any existing elements before recreating them
            this.clearChildren();

            int yBase = this.height / 6;

            // Regular outline color controls
            // Slider untuk Outline Red
            SliderWidget redSlider = new SliderWidget(this.width / 2 - 100, yBase + 24 + scrollOffset, 200, 20,
                    Text.translatable("options.tag-highlight.outline.red", (int) (TagHighlightClient.CONFIG.outlineRed * 255)),
                    TagHighlightClient.CONFIG.outlineRed) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.outlineRed = (float) this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.outline.red", (int) (this.value * 255)));
                }
            };
            this.addDrawableChild(redSlider);

            // Slider untuk Outline Green
            SliderWidget greenSlider = new SliderWidget(this.width / 2 - 100, yBase + 50 + scrollOffset, 200, 20,
                    Text.translatable("options.tag-highlight.outline.green", (int) (TagHighlightClient.CONFIG.outlineGreen * 255)),
                    TagHighlightClient.CONFIG.outlineGreen) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.outlineGreen = (float) this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.outline.green", (int) (this.value * 255)));
                }
            };
            this.addDrawableChild(greenSlider);

            // Slider untuk Outline Blue
            SliderWidget blueSlider = new SliderWidget(this.width / 2 - 100, yBase + 76 + scrollOffset, 200, 20,
                    Text.translatable("options.tag-highlight.outline.blue", (int) (TagHighlightClient.CONFIG.outlineBlue * 255)),
                    TagHighlightClient.CONFIG.outlineBlue) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.outlineBlue = (float) this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.outline.blue", (int) (this.value * 255)));
                }
            };
            this.addDrawableChild(blueSlider);

            // Slider untuk Outline Alpha
            SliderWidget alphaSlider = new SliderWidget(this.width / 2 - 100, yBase + 102 + scrollOffset, 200, 20,
                    Text.translatable("options.tag-highlight.outline.alpha", (int) (TagHighlightClient.CONFIG.outlineAlpha * 100)),
                    TagHighlightClient.CONFIG.outlineAlpha) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.outlineAlpha = (float) this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.outline.alpha", (int) (this.value * 100)));
                }
            };
            this.addDrawableChild(alphaSlider);

            // Tombol toggle untuk mengaktifkan/mematikan outline
            ButtonWidget toggleButton = ButtonWidget.builder(
                    Text.translatable("options.tag-highlight.outline.enabled", TagHighlightClient.CONFIG.outlineEnabled),
                    button -> {
                        TagHighlightClient.CONFIG.outlineEnabled = !TagHighlightClient.CONFIG.outlineEnabled;
                        button.setMessage(Text.translatable("options.tag-highlight.outline.enabled", TagHighlightClient.CONFIG.outlineEnabled));
                    }).dimensions(this.width / 2 - 100, yBase + 128 + scrollOffset, 200, 20).build();
            this.addDrawableChild(toggleButton);

            // Outline style button (box or entity outline)
            ButtonWidget outlineStyleButton = ButtonWidget.builder(
                    Text.translatable("options.tag-highlight.outline.style",
                            TagHighlightClient.CONFIG.outlineStyle == 0 ? "Box" : "Entity"),
                    button -> {
                        // Toggle between 0 (box) and 1 (entity)
                        TagHighlightClient.CONFIG.outlineStyle =
                                (TagHighlightClient.CONFIG.outlineStyle + 1) % 2;
                        button.setMessage(Text.translatable("options.tag-highlight.outline.style",
                                TagHighlightClient.CONFIG.outlineStyle == 0 ? "Box" : "Entity"));
                    }).dimensions(this.width / 2 - 100, yBase + 128 + 26 + scrollOffset, 200, 20).build();
            this.addDrawableChild(outlineStyleButton);

            // Tombol toggle untuk mengaktifkan/mematikan pencegahan nama duplikat
            ButtonWidget preventDuplicateNamesButton = ButtonWidget.builder(
                    Text.translatable("options.tag-highlight.prevent_duplicate.enabled", TagHighlightClient.CONFIG.preventDuplicateNamesEnabled),
                    button -> {
                        TagHighlightClient.CONFIG.preventDuplicateNamesEnabled = !TagHighlightClient.CONFIG.preventDuplicateNamesEnabled;
                        button.setMessage(Text.translatable("options.tag-highlight.prevent_duplicate.enabled",
                                TagHighlightClient.CONFIG.preventDuplicateNamesEnabled));
                    }).dimensions(this.width / 2 - 100, yBase + 154 + 26 + scrollOffset, 200, 20).build();
            this.addDrawableChild(preventDuplicateNamesButton);

            // Stats Mode Controls (Stats mode section)
            this.addDrawableChild(ButtonWidget.builder(
                    Text.translatable("options.tag-highlight.stats_mode.enabled", TagHighlightClient.CONFIG.statsMode),
                    button -> {
                        TagHighlightClient.CONFIG.statsMode = !TagHighlightClient.CONFIG.statsMode;
                        button.setMessage(Text.translatable("options.tag-highlight.stats_mode.enabled",
                                TagHighlightClient.CONFIG.statsMode));
                    }).dimensions(this.width / 2 - 100, yBase + 180 + 26 + scrollOffset, 200, 20).build());

            // Stats mode color controls
            SliderWidget statsRedSlider = new SliderWidget(this.width / 2 - 100, yBase + 206 + 26 + scrollOffset, 200, 20,
                    Text.translatable("options.tag-highlight.stats_mode.red", (int) (TagHighlightClient.CONFIG.statsModeRed * 255)),
                    TagHighlightClient.CONFIG.statsModeRed) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.statsModeRed = (float) this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.stats_mode.red", (int) (this.value * 255)));
                }
            };
            this.addDrawableChild(statsRedSlider);

            SliderWidget statsGreenSlider = new SliderWidget(this.width / 2 - 100, yBase + 232 + 26 + scrollOffset, 200, 20,
                    Text.translatable("options.tag-highlight.stats_mode.green", (int) (TagHighlightClient.CONFIG.statsModeGreen * 255)),
                    TagHighlightClient.CONFIG.statsModeGreen) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.statsModeGreen = (float) this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.stats_mode.green", (int) (this.value * 255)));
                }
            };
            this.addDrawableChild(statsGreenSlider);

            SliderWidget statsBlueSlider = new SliderWidget(this.width / 2 - 100, yBase + 258 + 26 + scrollOffset, 200, 20,
                    Text.translatable("options.tag-highlight.stats_mode.blue", (int) (TagHighlightClient.CONFIG.statsModeBlue * 255)),
                    TagHighlightClient.CONFIG.statsModeBlue) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.statsModeBlue = (float) this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.stats_mode.blue", (int) (this.value * 255)));
                }
            };
            this.addDrawableChild(statsBlueSlider);

            SliderWidget statsAlphaSlider = new SliderWidget(this.width / 2 - 100, yBase + 284 + 26 + scrollOffset, 200, 20,
                    Text.translatable("options.tag-highlight.stats_mode.alpha", (int) (TagHighlightClient.CONFIG.statsModeAlpha * 100)),
                    TagHighlightClient.CONFIG.statsModeAlpha) {
                @Override
                protected void updateMessage() {

                }

                @Override
                protected void applyValue() {
                    TagHighlightClient.CONFIG.statsModeAlpha = (float) this.value;
                    this.setMessage(Text.translatable("options.tag-highlight.stats_mode.alpha", (int) (this.value * 100)));
                }
            };
            this.addDrawableChild(statsAlphaSlider);

            // Tombol Done
            ButtonWidget doneButton = ButtonWidget.builder(ScreenTexts.DONE, button -> {
                // Save config dan kembali ke layar sebelumnya
                TagHighlightClient.saveConfig();
                assert this.client != null;
                this.client.setScreen(this.parent);
            }).dimensions(this.width / 2 - 100, yBase + 310 + 26 + scrollOffset, 200, 20).build();
            this.addDrawableChild(doneButton);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            // Handle scrolling - adjust scrollOffset based on scroll direction
            int newScrollOffset = scrollOffset + (int)(verticalAmount * 20); // Adjust 20 for scroll speed

            // Calculate the total height of the content
            int visibleHeight = this.height - 50; // Approximate visible area height

            // Limit scrolling so elements don't go too far up or down
            if (newScrollOffset <= 0 && newScrollOffset >= -(contentHeight - visibleHeight)) {
                scrollOffset = newScrollOffset;
                // Reinitialize all elements with the new scroll position
                this.init();
                return true;
            }

            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            // Handle mouse dragging for scrolling
            if (isScrolling) {
                int newScrollOffset = scrollOffset - (int)deltaY;

                // Calculate the total height of the content
                int visibleHeight = this.height - 50; // Approximate visible area height

                // Limit scrolling so elements don't go too far up or down
                if (newScrollOffset <= 0 && newScrollOffset >= -(contentHeight - visibleHeight)) {
                    scrollOffset = newScrollOffset;
                    // Reinitialize all elements with the new scroll position
                    this.init();
                    return true;
                }
            }

            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Start scrolling when mouse is clicked in empty area
            if (button == 0 && mouseX > this.width - 15) { // Left mouse button near scrollbar
                isScrolling = true;
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            // Stop scrolling when mouse is released
            if (button == 0) { // Left mouse button
                isScrolling = false;
            }

            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            // Draw the background
            this.renderBackground(context, mouseX, mouseY, delta);

            // Draw title at a fixed position (not affected by scroll)
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 16777215);

            // Render all widgets (they already have the scroll offset applied)
            super.render(context, mouseX, mouseY, delta);

            // Then render the color previews *after* all widgets
            renderColorPreviews(context);

            // Draw scrollbar if needed
            int visibleHeight = this.height - 50;
            if (contentHeight > visibleHeight) {
                drawScrollbar(context);
            }
        }

        // Method to draw a scrollbar
        private void drawScrollbar(DrawContext context) {
            // Calculate scrollbar position and size
            int visibleHeight = this.height - 50;
            float contentRatio = (float)visibleHeight / contentHeight;
            int scrollbarHeight = Math.max((int)(visibleHeight * contentRatio), 32); // Minimum scrollbar height

            // Calculate scrollbar position
            float scrollRatio = (float)Math.abs(scrollOffset) / Math.max(1, contentHeight - visibleHeight);
            int scrollbarY = 25 + (int)((visibleHeight - scrollbarHeight) * scrollRatio);

            // Draw scrollbar track
            context.fill(this.width - 10, 25, this.width - 6, this.height - 25, 0x40000000);

            // Draw scrollbar thumb
            context.fill(this.width - 10, scrollbarY, this.width - 6, scrollbarY + scrollbarHeight, 0x80FFFFFF);
        }

        // Separate method to render color previews - this helps ensure they always appear on top
        private void renderColorPreviews(DrawContext context) {
            // Render normal mode color preview box
            int previewX = this.width / 2 + PREVIEW_X_OFFSET;
            int previewY = this.height / 6 + PREVIEW_Y_OFFSET + scrollOffset;

            // Calculate color from current slider values
            int red = (int)(TagHighlightClient.CONFIG.outlineRed * 255);
            int green = (int)(TagHighlightClient.CONFIG.outlineGreen * 255);
            int blue = (int)(TagHighlightClient.CONFIG.outlineBlue * 255);
            int alpha = (int)(TagHighlightClient.CONFIG.outlineAlpha * 255);
            int color = (alpha << 24) | (red << 16) | (green << 8) | blue;

            // Only draw preview if it's visible
            if (previewY + PREVIEW_SIZE > 25 && previewY < this.height - 25) {
                // Draw preview with borders
                drawColorPreview(context, previewX, previewY, color, TagHighlightClient.CONFIG.outlineAlpha < 1.0f);
            }

            // Render stats mode color preview box
            previewY = this.height / 6 + STATS_PREVIEW_Y_OFFSET + 26 + scrollOffset;

            // Calculate stats mode color
            red = (int)(TagHighlightClient.CONFIG.statsModeRed * 255);
            green = (int)(TagHighlightClient.CONFIG.statsModeGreen * 255);
            blue = (int)(TagHighlightClient.CONFIG.statsModeBlue * 255);
            alpha = (int)(TagHighlightClient.CONFIG.statsModeAlpha * 255);
            color = (alpha << 24) | (red << 16) | (green << 8) | blue;

            // Only draw preview if it's visible
            if (previewY + PREVIEW_SIZE > 25 && previewY < this.height - 25) {
                // Draw preview with borders
                drawColorPreview(context, previewX, previewY, color, TagHighlightClient.CONFIG.statsModeAlpha < 1.0f);
            }
        }

        // Helper method to draw a color preview with borders and optional transparency grid
        private void drawColorPreview(DrawContext context, int x, int y, int color, boolean showTransparencyGrid) {
            // Draw outer border (black)
            context.fill(x - 2, y - 2, x + TagHighlightConfigScreen.PREVIEW_SIZE + 2, y + TagHighlightConfigScreen.PREVIEW_SIZE + 2, 0xFF000000);

            // Draw inner border (white)
            context.fill(x - 1, y - 1, x + TagHighlightConfigScreen.PREVIEW_SIZE + 1, y + TagHighlightConfigScreen.PREVIEW_SIZE + 1, 0xFFFFFFFF);

            // Draw color preview with exact pixel values
            context.fill(x, y, x + TagHighlightConfigScreen.PREVIEW_SIZE, y + TagHighlightConfigScreen.PREVIEW_SIZE, color);

            // Draw a grid pattern on transparent backgrounds to better show transparency
            if (showTransparencyGrid) {
                int gridSize = 5;
                for (int gx = 0; gx < TagHighlightConfigScreen.PREVIEW_SIZE; gx += gridSize) {
                    for (int gy = 0; gy < TagHighlightConfigScreen.PREVIEW_SIZE; gy += gridSize) {
                        boolean isCheckerboard = (gx / gridSize + gy / gridSize) % 2 == 0;
                        if (isCheckerboard) {
                            context.fill(
                                    x + gx,
                                    y + gy,
                                    Math.min(x + gx + gridSize, x + TagHighlightConfigScreen.PREVIEW_SIZE),
                                    Math.min(y + gy + gridSize, y + TagHighlightConfigScreen.PREVIEW_SIZE),
                                    0x22000000
                            );
                        }
                    }
                }
            }
        }
    }
}