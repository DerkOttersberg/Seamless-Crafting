package com.derk.easyinventorycrafter;

import com.derk.easyinventorycrafter.client.EasyHighlightRenderer;
import com.derk.easyinventorycrafter.client.NearbyItemsClientState;
import com.derk.easyinventorycrafter.net.NearbyItemsPayload;
import com.derk.easyinventorycrafter.net.NearbyHighlightResponsePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class EasyInventoryCrafterClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EasyInventoryCrafterConfig.load();

		ClientPlayNetworking.registerGlobalReceiver(NearbyItemsPayload.ID, (payload, context) -> {
			NearbyItemsClientState.applyPayload(payload);
		});

		ClientPlayNetworking.registerGlobalReceiver(NearbyHighlightResponsePayload.ID, (payload, context) -> {
			NearbyItemsClientState.setHighlight(payload.positions(), EasyInventoryCrafterConfig.getHighlightDurationTicks());
		});

		ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
			private int tickCounter = 0;

			@Override
			public void onEndTick(MinecraftClient client) {
				NearbyItemsClientState.tickHighlight(client);
				if (!(client.currentScreen instanceof CraftingScreen)
						&& !(client.currentScreen instanceof InventoryScreen)) {
					tickCounter = 0;
					return;
				}

				tickCounter++;
				if (tickCounter >= EasyInventoryCrafterConfig.getAutoRefreshTicks()) {
					NearbyItemsClientState.requestUpdate();
					tickCounter = 0;
				}
			}
		});

		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
			if (!NearbyItemsClientState.hasHighlight()) {
				return;
			}

			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null) {
				return;
			}

			PlayerEntity player = client.player;
			Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
			float alpha = NearbyItemsClientState.getHighlightAlpha();

			var matrices = context.matrixStack();
			matrices.push();

			for (BlockPos pos : NearbyItemsClientState.getHighlightPositions()) {
				var consumer = context.consumers().getBuffer(RenderLayer.getLightning());
				Box fullBox = renderHighlightedBlock(client, matrices, consumer, cameraPos, pos, alpha);
				if (fullBox == null) {
					continue;
				}

				BlockPos partnerPos = getConnectedChestPos(client, pos);
				if (partnerPos != null) {
					Box partnerBox = renderHighlightedBlock(client, matrices, consumer, cameraPos, partnerPos, alpha);
					if (partnerBox != null) {
						fullBox = combineBoxes(fullBox, partnerBox);
					}
				}

				if (EasyInventoryCrafterConfig.isDistanceLabelEnabled()) {
					EasyHighlightRenderer.renderDistanceLabel(
						client,
						matrices,
						context.consumers(),
						cameraPos,
						fullBox,
						player == null ? cameraPos : player.getEyePos(),
						alpha
					);
				}
			}

			matrices.pop();
		});
	}

	private static @Nullable Box renderHighlightedBlock(
		MinecraftClient client,
		net.minecraft.client.util.math.MatrixStack matrices,
		net.minecraft.client.render.VertexConsumer consumer,
		Vec3d cameraPos,
		BlockPos pos,
		float alpha
	) {
		if (client.world == null) {
			return null;
		}

		BlockState state = client.world.getBlockState(pos);
		if (state.isAir()) {
			return null;
		}

		VoxelShape shape = state.getOutlineShape(client.world, pos);
		if (shape.isEmpty()) {
			return null;
		}

		Box fullBox = shape.getBoundingBox().offset(pos).expand(0.002);
		for (Box shapeBox : shape.getBoundingBoxes()) {
			Box box = shapeBox.offset(pos).expand(0.002);
			EasyHighlightRenderer.renderBox(matrices, consumer, cameraPos, box, alpha);
		}

		return fullBox;
	}

	private static @Nullable BlockPos getConnectedChestPos(MinecraftClient client, BlockPos pos) {
		if (client.world == null) {
			return null;
		}

		BlockState state = client.world.getBlockState(pos);
		if (!(state.getBlock() instanceof ChestBlock)) {
			return null;
		}

		ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
		if (chestType == ChestType.SINGLE) {
			return null;
		}

		Direction facing = state.get(ChestBlock.FACING);
		Direction offset = chestType == ChestType.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();
		BlockPos partnerPos = pos.offset(offset);
		BlockState partnerState = client.world.getBlockState(partnerPos);
		if (!(partnerState.getBlock() instanceof ChestBlock)) {
			return null;
		}

		if (partnerState.get(ChestBlock.FACING) != facing) {
			return null;
		}

		if (partnerState.get(ChestBlock.CHEST_TYPE) != chestType.getOpposite()) {
			return null;
		}

		return partnerPos.toImmutable();
	}

	private static Box combineBoxes(Box first, Box second) {
		return new Box(
			Math.min(first.minX, second.minX),
			Math.min(first.minY, second.minY),
			Math.min(first.minZ, second.minZ),
			Math.max(first.maxX, second.maxX),
			Math.max(first.maxY, second.maxY),
			Math.max(first.maxZ, second.maxZ)
		);
	}
}
