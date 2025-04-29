package com.taghighlight.mixin.client;

import com.taghighlight.TagHighlightClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityColorMixin {
    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void overrideGlowColor(CallbackInfoReturnable<Integer> cir) {
            Entity entity = (Entity) (Object) this;

        // Check if this entity should have custom outline color
        if (TagHighlightClient.CONFIG != null && TagHighlightClient.CONFIG.outlineEnabled &&
                TagHighlightClient.CONFIG.outlineStyle == 1) {

            // Different color for different modes
            if ((!TagHighlightClient.CONFIG.statsMode && TagHighlightClient.getEntitiesToHighlight().contains(entity)) ||
                    (TagHighlightClient.CONFIG.statsMode && TagHighlightClient.getStatsMatchingEntities().contains(entity))) {

                // Convert RGB values (0-1) to RGB int
                int red, green, blue;

                if (TagHighlightClient.CONFIG.statsMode) {
                    red = (int)(TagHighlightClient.CONFIG.statsModeRed * 255);
                    green = (int)(TagHighlightClient.CONFIG.statsModeGreen * 255);
                    blue = (int)(TagHighlightClient.CONFIG.statsModeBlue * 255);
                } else {
                    red = (int)(TagHighlightClient.CONFIG.outlineRed * 255);
                    green = (int)(TagHighlightClient.CONFIG.outlineGreen * 255);
                    blue = (int)(TagHighlightClient.CONFIG.outlineBlue * 255);
                }

                // Create RGB color value and set as return value
                int color = (red << 16) | (green << 8) | blue;
                cir.setReturnValue(color);
            }
        }
    }
}