package com.taghighlight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TagHighlightClient implements ClientModInitializer {
	private static final double HIGHLIGHT_RADIUS = 50.0;
	private static final List<Entity> entitiesToHighlight = new ArrayList<>();

	public static TagHighlightConfig CONFIG;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("tag-highlight.json").toFile();

	@Override
	public void onInitializeClient() {
		// Load config first
		loadConfig();

		// Register keybinds
		TagHighlightKeybinds.register();

		// Register tick event untuk menemukan entitas
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world == null || client.player == null) return;

			entitiesToHighlight.clear();

			Vec3d playerPos = client.player.getPos();
			Box searchBox = new Box(
					playerPos.x - HIGHLIGHT_RADIUS, playerPos.y - HIGHLIGHT_RADIUS, playerPos.z - HIGHLIGHT_RADIUS,
					playerPos.x + HIGHLIGHT_RADIUS, playerPos.y + HIGHLIGHT_RADIUS, playerPos.z + HIGHLIGHT_RADIUS
			);

			for (Entity entity : client.world.getEntitiesByClass(MobEntity.class, searchBox, e -> true)) {
				if (!entity.hasCustomName() && entity != client.player) {
					entitiesToHighlight.add(entity);
				}
			}
		});

		// Register render event untuk menggambar outline box
		WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
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
			// Jangan set glLineWidth agar menghindari warning

			// Dapatkan buffer untuk menggambar garis
			VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
			VertexConsumer lineConsumer = immediate.getBuffer(RenderLayer.getLines());

			// Jika outline diaktifkan di config, gambar outline box untuk tiap entitas
			if (CONFIG.outlineEnabled) {
				for (Entity entity : entitiesToHighlight) {
					matrices.push();
					double x = entity.getX() - cameraPos.x;
					double y = entity.getY() - cameraPos.y;
					double z = entity.getZ() - cameraPos.z;
					matrices.translate(x, y, z);

					Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
					// Gunakan warna outline dari config
					drawOutlineBox(matrices, lineConsumer, box, CONFIG.outlineRed, CONFIG.outlineGreen, CONFIG.outlineBlue, CONFIG.outlineAlpha);
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