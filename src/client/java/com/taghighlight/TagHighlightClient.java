package com.taghighlight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class TagHighlightClient implements ClientModInitializer {
	private static final double HIGHLIGHT_RADIUS = 50.0;
	private static final List<Entity> entitiesToHighlight = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		// Register tick event to find entities without nametags
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

		// Register render event to highlight entities
		WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null || client.player == null) return;

			MatrixStack matrices = context.matrixStack();
			Vec3d cameraPos = context.camera().getPos();
			VertexConsumerProvider vertexConsumers = context.consumers();

			if (vertexConsumers == null) return;

			// Use the debug lines render layer which has proper format
			VertexConsumer lines = vertexConsumers.getBuffer(RenderLayer.getLines());

			for (Entity entity : entitiesToHighlight) {
				matrices.push();

				// Get entity position relative to camera
				double x = entity.getX() - cameraPos.x;
				double y = entity.getY() - cameraPos.y;
				double z = entity.getZ() - cameraPos.z;

				matrices.translate(x, y, z);

				// Draw box using entity's bounding box
				Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
				drawOutlineBox(matrices, lines, box, 1.0f, 0.0f, 0.0f, 1.0f);

				matrices.pop();
			}
		});
	}

	// Custom implementation for drawing outline boxes with normal data
	private void drawOutlineBox(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		float minX = (float)box.minX;
		float minY = (float)box.minY;
		float minZ = (float)box.minZ;
		float maxX = (float)box.maxX;
		float maxY = (float)box.maxY;
		float maxZ = (float)box.maxZ;

		// Bottom face - with normals
		line(vertexConsumer, matrix, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, minX, minY, maxZ, minX, minY, minZ, red, green, blue, alpha);

		// Top face - with normals
		line(vertexConsumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);

		// Vertical connections - with normals
		line(vertexConsumer, matrix, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
		line(vertexConsumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
	}

	// Helper method for drawing a line with proper normals
	private void line(VertexConsumer vertexConsumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
		// Calculate normal direction vector from line endpoints
		float nx = x2 - x1;
		float ny = y2 - y1;
		float nz = z2 - z1;
		float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
		if (len > 0) {
			nx /= len;
			ny /= len;
			nz /= len;
		}

		// Draw line with proper normal data
		vertexConsumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).normal(nx, ny, nz);
		vertexConsumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).normal(nx, ny, nz);
	}
}