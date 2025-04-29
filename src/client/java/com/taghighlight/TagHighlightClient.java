package com.taghighlight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagHighlightClient implements ClientModInitializer {
	private static final double HIGHLIGHT_RADIUS = 50.0;
	private static final List<Entity> entitiesToHighlight = new ArrayList<>();
	private static final List<Entity> statsMatchingEntities = new ArrayList<>();
	private static final Map<String, Integer> entityTypeCount = new HashMap<>();

	public static TagHighlightConfig CONFIG;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("tag-highlight.json").toFile();

	// Track the currently held name tag's custom name
	private static String currentHeldTagName = null;

	public static final Identifier MY_HUD_LAYER = Identifier.of("modid", "my_hud_layer");


	@Override
	public void onInitializeClient() {
		// Load config first
		loadConfig();

		// Register keybinds
		TagHighlightKeybinds.register();

		// Register name tag handler to prevent duplicate names
		TagNameHandler.register();

		// Register tick event untuk menemukan entitas
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world == null || client.player == null) return;

			// Clear entity lists
			entitiesToHighlight.clear();
			statsMatchingEntities.clear();
			entityTypeCount.clear();

			// Check if player is holding a name tag with custom name
			currentHeldTagName = null;
			ItemStack heldItem = client.player.getMainHandStack();
			if (heldItem.getItem() == Items.NAME_TAG && heldItem.getCustomName() != null ) {
				currentHeldTagName = heldItem.getName().getString();
			}

			Vec3d playerPos = client.player.getPos();
			Box searchBox = new Box(
					playerPos.x - HIGHLIGHT_RADIUS, playerPos.y - HIGHLIGHT_RADIUS, playerPos.z - HIGHLIGHT_RADIUS,
					playerPos.x + HIGHLIGHT_RADIUS, playerPos.y + HIGHLIGHT_RADIUS, playerPos.z + HIGHLIGHT_RADIUS
			);

			// Find entities based on current mode
			if (CONFIG.statsMode && currentHeldTagName != null) {
				// Stats mode - find entities with same name as the held tag
				for (Entity entity : client.world.getEntitiesByClass(MobEntity.class, searchBox, e -> true)) {
					if (entity.hasCustomName() && entity.getCustomName() != null && entity.getCustomName().getString().equals(currentHeldTagName)) {
						statsMatchingEntities.add(entity);

						// Count by entity type
						String typeName = getEntityTypeName(entity);
						entityTypeCount.put(typeName, entityTypeCount.getOrDefault(typeName, 0) + 1);
					}
				}
			} else if (!CONFIG.statsMode) {
				// Normal mode - find entities without custom names
				for (Entity entity : client.world.getEntitiesByClass(MobEntity.class, searchBox, e -> true)) {
					if (!entity.hasCustomName() && entity != client.player) {
						entitiesToHighlight.add(entity);
					}
				}
			}
		});

		// Register render event untuk menggambar outline box
		WorldRenderEvents.AFTER_TRANSLUCENT.register((context) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null || client.player == null) return;

			MatrixStack matrices = context.matrixStack();
			Vec3d cameraPos = context.camera().getPos();
			VertexConsumerProvider vertexConsumers = context.consumers();
			if (vertexConsumers == null) return;

			// Simpan state OpenGL
			boolean depthEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
			boolean blendEnabled = GL11.glGetBoolean(GL11.GL_BLEND);
			boolean cullEnabled  = GL11.glGetBoolean(GL11.GL_CULL_FACE);
			int oldDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);

			// Setup state untuk outline (x-ray effect)
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderSystem.disableCull();
			GL11.glDepthFunc(GL11.GL_ALWAYS);
			RenderSystem.enableDepthTest();
			RenderSystem.depthMask(false);

			// Dapatkan buffer untuk menggambar garis
			VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
			VertexConsumer lineConsumer = immediate.getBuffer(RenderLayer.getLines());

			// Chose which entity list and colors to use based on current mode
			List<Entity> entitiesToRender = CONFIG.statsMode ? statsMatchingEntities : entitiesToHighlight;
			float red, green, blue, alpha;

			if (CONFIG.statsMode) {
				red = CONFIG.statsModeRed;
				green = CONFIG.statsModeGreen;
				blue = CONFIG.statsModeBlue;
				alpha = CONFIG.statsModeAlpha;
			} else {
				red = CONFIG.outlineRed;
				green = CONFIG.outlineGreen;
				blue = CONFIG.outlineBlue;
				alpha = CONFIG.outlineAlpha;
			}

			// Jika outline diaktifkan di config dan ada entitas untuk di-render
			if (CONFIG.outlineEnabled && !entitiesToRender.isEmpty()) {

				// Get the tickDelta from the context
				float tickDelta = context.tickCounter().getTickDelta(true);

				for (Entity entity : entitiesToRender) {
                    assert matrices != null;
                    matrices.push();
					// Use interpolated positions with tickDelta
					Vec3d interpolatedPos = entity.getLerpedPos(tickDelta);
					double x = interpolatedPos.x - cameraPos.x;
					double y = interpolatedPos.y - cameraPos.y;
					double z = interpolatedPos.z - cameraPos.z;

					matrices.translate(x, y, z);

					Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
					drawOutlineBox(matrices, lineConsumer, box, red, green, blue, alpha);
					matrices.pop();
				}
			}

			immediate.draw();

			// Kembalikan state OpenGL
			RenderSystem.depthMask(true);
			GL11.glDepthFunc(oldDepthFunc);
			if (!depthEnabled) RenderSystem.disableDepthTest();
			if (!blendEnabled) RenderSystem.disableBlend();
			if (cullEnabled) RenderSystem.enableCull();
		});

		HudLayerRegistrationCallback.EVENT.register(layers -> layers.attachLayerBefore(IdentifiedLayer.CHAT, MY_HUD_LAYER, (drawContext, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null || client.player == null || !CONFIG.statsMode || currentHeldTagName == null) {
				return;
			}

			// Only render if we have stats to show
			if (!entityTypeCount.isEmpty()) {
				int y = 10;

				// Calculate total count
				int totalCount = 0;
				for (int count : entityTypeCount.values()) {
					totalCount += count;
				}

				// Draw header
				drawContext.getMatrices().push();
				//drawContext.getMatrices().scale(1.2f, 1.2f, 1.0f);
				drawContext.drawText(client.textRenderer, Text.literal("Stats for: " + currentHeldTagName), 10, (int)(y / 1.2f), 0xDDAA00, true);
				drawContext.getMatrices().pop();
				y += 15;

				// Draw total count
				drawContext.drawText(client.textRenderer, Text.literal("All: " + totalCount), 10, y, 0xDDAA00, true);
				y += 12;

				// Draw counts for each entity type
				for (Map.Entry<String, Integer> entry : entityTypeCount.entrySet()) {
					drawContext.drawText(client.textRenderer,
							Text.literal(entry.getKey() + ": " + entry.getValue()),
							10, y, 0xDDAA00, true);
					y += 12;
				}
			}
		}));
	}

	// Helper method to get readable entity type name
	private String getEntityTypeName(Entity entity) {
		Identifier id = Registries.ENTITY_TYPE.getId(entity.getType());
		String path = id.getPath();

		// Capitalize first letter and replace underscores with spaces
		if (!path.isEmpty()) {
			path = Character.toUpperCase(path.charAt(0)) + path.substring(1);
		}

		return path.replace('_', ' ');
	}

	// Custom drawing untuk outline box
	private void drawOutlineBox(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		float minX = (float) box.minX;
		float minY = (float) box.minY;
		float minZ = (float) box.minZ;
		float maxX = (float) box.maxX;
		float maxY = (float) box.maxY;
		float maxZ = (float) box.maxZ;

		// Gambar semua sisi kotak
		line(vertexConsumer, matrix, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, minX, minY, maxZ, minX, minY, minZ, red, green, blue, alpha);

		line(vertexConsumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);

		line(vertexConsumer, matrix, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
	}

	// Helper untuk menggambar garis
	private void line(VertexConsumer vertexConsumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
		float nx = x2 - x1;
		float ny = y2 - y1;
		float nz = z2 - z1;
		float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
		if (len > 0) {
			nx /= len;
			ny /= len;
			nz /= len;
		}
		vertexConsumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).normal(nx, ny, nz);
		vertexConsumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).normal(nx, ny, nz);
	}

	// Load dan simpan konfigurasi
	private void loadConfig() {
		if (CONFIG_FILE.exists()) {
			try (FileReader reader = new FileReader(CONFIG_FILE)) {
				CONFIG = GSON.fromJson(reader, TagHighlightConfig.class);
			} catch (IOException e) {
				TagHighlight.LOGGER.error("Failed to load config file, creating default", e);
				CONFIG = new TagHighlightConfig();
				saveConfig();
			}
		} else {
			CONFIG = new TagHighlightConfig();
			saveConfig();
		}
	}

	public static void saveConfig() {
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			GSON.toJson(CONFIG, writer);
		} catch (IOException e) {
			TagHighlight.LOGGER.error("Failed to save config file", e);
		}
	}
}