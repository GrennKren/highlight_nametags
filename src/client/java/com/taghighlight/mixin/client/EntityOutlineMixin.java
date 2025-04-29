package com.taghighlight.mixin.client;

import com.taghighlight.TagHighlightClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class EntityOutlineMixin {
    @Inject(method = "hasOutline", at = @At("RETURN"), cancellable = true)
    private void showTagHighlightOutline(Entity entity, CallbackInfoReturnable<Boolean> info) {
        // Only apply outline if using entity style (style == 1) and outlines are enabled
        if (TagHighlightClient.CONFIG != null &&
                TagHighlightClient.CONFIG.outlineEnabled &&
                TagHighlightClient.CONFIG.outlineStyle == 1) {

            // Check which list to use based on mode - now this will respect Minecraft's render distance
            if ((!TagHighlightClient.CONFIG.statsMode && TagHighlightClient.getEntitiesToHighlight().contains(entity)) ||
                    (TagHighlightClient.CONFIG.statsMode && TagHighlightClient.getStatsMatchingEntities().contains(entity))) {
                info.setReturnValue(true);
            }
        }
    }
}