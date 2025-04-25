package com.taghighlight;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class TagNameHandler {

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // Check if we're on the server side and it's the main hand
            if (world.isClient || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            // Check if the entity is a mob
            if (!(entity instanceof MobEntity mob)) {
                return ActionResult.PASS;
            }

            // Check if player is holding a name tag
            ItemStack heldItem = player.getStackInHand(hand);
            if (heldItem.getItem() != Items.NAME_TAG) {
                return ActionResult.PASS;
            }

            // Check if the name tag has a custom name
            if (heldItem.getCustomName() == null) {
                return ActionResult.PASS;
            }

            // Get the name from the name tag
            String nameTagText = heldItem.getName().getString();

            // Check if prevent duplicate names feature is enabled
            if (TagHighlightClient.CONFIG.preventDuplicateNamesEnabled) {
                // Check if mob already has a name and if it's the same as the name tag
                if (mob.hasCustomName() && mob.getCustomName().getString().equals(nameTagText)) {
                    // Cancel the interaction and notify the player
                    player.sendMessage(Text.translatable("message.tag-highlight.same_name"), true);
                    return ActionResult.FAIL;
                }
            }

            // If the name is different or the mob has no name, or feature is disabled, allow the interaction
            return ActionResult.PASS;
        });
    }
}