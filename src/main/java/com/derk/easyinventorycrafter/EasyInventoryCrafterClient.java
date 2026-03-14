package com.derk.easyinventorycrafter;

import com.derk.easyinventorycrafter.client.EasyHighlightRenderer;
import com.derk.easyinventorycrafter.client.EasyHighlightRenderLayer;
import com.derk.easyinventorycrafter.client.NearbyItemsClientState;
import com.derk.easyinventorycrafter.net.NearbyItemsPayload;
import com.derk.easyinventorycrafter.net.NearbyHighlightResponsePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class EasyInventoryCrafterClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(NearbyItemsPayload.ID, (payload, context) -> {
			NearbyItemsClientState.applyPayload(payload);
		});

		ClientPlayNetworking.registerGlobalReceiver(NearbyHighlightResponsePayload.ID, (payload, context) -> {
			NearbyItemsClientState.setHighlight(payload.positions(), 100);
		});

		ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
			private int tickCounter = 0;

			@Override
			public void onEndTick(MinecraftClient client) {
				NearbyItemsClientState.tickHighlight(client);
				if (!(client.currentScreen instanceof CraftingScreen)) {
					tickCounter = 0;
					return;
				}

				tickCounter++;
				if (tickCounter >= 20) {
					NearbyItemsClientState.requestUpdate();
					tickCounter = 0;
				}
			}
		});

		WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {
			if (!NearbyItemsClientState.hasHighlight()) {
				return;
			}

			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null) {
				return;
			}

			Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
			float alpha = NearbyItemsClientState.getHighlightAlpha();

			var matrices = context.matrices();
			matrices.push();

			var consumer = context.consumers().getBuffer(EasyHighlightRenderLayer.outline());

			for (BlockPos pos : NearbyItemsClientState.getHighlightPositions()) {
				BlockState state = client.world.getBlockState(pos);
				if (state.isAir()) {
					continue;
				}

				VoxelShape shape = state.getOutlineShape(client.world, pos);
				if (shape.isEmpty()) {
					continue;
				}

				for (Box shapeBox : shape.getBoundingBoxes()) {
					Box box = shapeBox.offset(pos).expand(0.002);
					EasyHighlightRenderer.renderOutlineBox(matrices, consumer, cameraPos, box, alpha);
				}
			}

			matrices.pop();
		});
	}
}
