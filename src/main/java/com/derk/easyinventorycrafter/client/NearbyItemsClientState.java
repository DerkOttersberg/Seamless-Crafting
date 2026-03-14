package com.derk.easyinventorycrafter.client;

import com.derk.easyinventorycrafter.EasyInventoryCrafterConfig;
import com.derk.easyinventorycrafter.NearbyInventoryScanner.NearbyItemEntry;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.derk.easyinventorycrafter.net.NearbyItemsPayload;
import com.derk.easyinventorycrafter.net.RequestNearbyItemsPayload;
import com.derk.easyinventorycrafter.net.NearbyHighlightRequestPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public final class NearbyItemsClientState {
	private static List<NearbyItemEntry> entries = Collections.emptyList();
	private static List<BlockPos> highlightPositions = Collections.emptyList();
	private static int highlightTicks;
	private static int highlightTotalTicks;
	private static boolean aimAtNextHighlight;

	private NearbyItemsClientState() {
	}

	public static List<NearbyItemEntry> getEntries() {
		return entries;
	}

	public static void clear() {
		entries = Collections.emptyList();
		highlightPositions = Collections.emptyList();
		highlightTicks = 0;
		highlightTotalTicks = 0;
		aimAtNextHighlight = false;
	}

	public static void requestUpdate() {
		if (ClientPlayNetworking.canSend(RequestNearbyItemsPayload.ID)) {
			ClientPlayNetworking.send(new RequestNearbyItemsPayload());
		}
	}

	public static void applyPayload(NearbyItemsPayload payload) {
		entries = payload.entries();
		MinecraftClient client = MinecraftClient.getInstance();
		client.execute(() -> {
			if (client.currentScreen instanceof RecipeBookProvider recipeBookProvider) {
				recipeBookProvider.refreshRecipeBook();
			}
		});
	}

	public static void requestHighlight(ItemStack stack) {
		requestHighlight(stack, false);
	}

	public static void requestHighlightAndAim(ItemStack stack) {
		requestHighlight(stack, true);
	}

	private static void requestHighlight(ItemStack stack, boolean aimAfterResponse) {
		if (stack == null || stack.isEmpty()) {
			return;
		}
		aimAtNextHighlight = aimAfterResponse && EasyInventoryCrafterConfig.isSnapAimEnabled();
		if (ClientPlayNetworking.canSend(NearbyHighlightRequestPayload.ID)) {
			ClientPlayNetworking.send(new NearbyHighlightRequestPayload(stack.copyWithCount(1)));
		}
	}

	public static void setHighlight(List<BlockPos> positions, int ticks) {
		highlightPositions = positions == null ? Collections.emptyList() : positions;
		highlightTicks = ticks;
		highlightTotalTicks = ticks;
		if (aimAtNextHighlight) {
			aimAtNextHighlight = false;
			derk$aimAtNearestHighlight();
		}
	}

	public static List<BlockPos> getHighlightPositions() {
		return highlightPositions;
	}

	public static boolean hasHighlight() {
		return !highlightPositions.isEmpty() && highlightTicks > 0;
	}

	public static float getHighlightAlpha() {
		if (highlightTotalTicks <= 0) {
			return 1.0f;
		}
		float progress = 1.0f - (highlightTicks / (float)highlightTotalTicks);
		float alpha = (float)Math.sin(Math.PI * progress);
		return Math.max(0.0f, Math.min(1.0f, alpha));
	}

	public static void tickHighlight(MinecraftClient client) {
		if (highlightPositions.isEmpty() || highlightTicks <= 0 || client.world == null) {
			if (highlightTicks <= 0) {
				highlightPositions = Collections.emptyList();
				highlightTotalTicks = 0;
			}
			return;
		}
		highlightTicks--;
		if (highlightTicks <= 0) {
			highlightPositions = Collections.emptyList();
			highlightTotalTicks = 0;
			return;
		}
	}

	private static void derk$aimAtNearestHighlight() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null || highlightPositions.isEmpty()) {
			return;
		}

		Vec3d eyePos = client.player.getEyePos();
		BlockPos nearestPos = null;
		double nearestDistance = Double.MAX_VALUE;
		for (BlockPos pos : highlightPositions) {
			double distance = eyePos.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestPos = pos;
			}
		}

		if (nearestPos == null) {
			return;
		}

		Vec3d target = new Vec3d(nearestPos.getX() + 0.5, nearestPos.getY() + 0.5, nearestPos.getZ() + 0.5);
		Vec3d delta = target.subtract(eyePos);
		double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		float yaw = (float)(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
		float pitch = (float)(-Math.toDegrees(Math.atan2(delta.y, horizontalDistance)));
		client.player.setYaw(yaw);
		client.player.setPitch(pitch);
	}
}
