package com.derk.seamlesscrafting;

import com.derk.seamlesscrafting.net.NearbyItemsSync;
import com.derk.seamlesscrafting.net.NearbyItemsPayload;
import com.derk.seamlesscrafting.net.RequestNearbyItemsPayload;
import com.derk.seamlesscrafting.net.NearbyHighlightRequestPayload;
import com.derk.seamlesscrafting.net.NearbyHighlightResponsePayload;
import com.derk.seamlesscrafting.net.ReturnNearbyItemsPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;

public class SeamlessCraftingMod implements ModInitializer {
	public static final String MOD_ID = "seamless_crafting";

	@Override
	public void onInitialize() {
		SeamlessCraftingConfig.load();
		PayloadTypeRegistry.playC2S().register(RequestNearbyItemsPayload.ID, RequestNearbyItemsPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(NearbyItemsPayload.ID, NearbyItemsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(NearbyHighlightRequestPayload.ID, NearbyHighlightRequestPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(NearbyHighlightResponsePayload.ID, NearbyHighlightResponsePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ReturnNearbyItemsPayload.ID, ReturnNearbyItemsPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RequestNearbyItemsPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				if (context.player().currentScreenHandler instanceof CraftingScreenHandler
						|| context.player().currentScreenHandler instanceof PlayerScreenHandler) {
					NearbyItemsSync.sendNearbyItems(context.player());
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(NearbyHighlightRequestPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				if (!(context.player().currentScreenHandler instanceof CraftingScreenHandler)
						&& !(context.player().currentScreenHandler instanceof PlayerScreenHandler)) {
					return;
				}
				java.util.List<net.minecraft.util.math.BlockPos> positions = NearbyItemsSync.findHighlightPositions(
						context.player(),
						payload.stack()
				);
				if (positions != null && !positions.isEmpty()) {
					ServerPlayNetworking.send(context.player(), new NearbyHighlightResponsePayload(positions));
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ReturnNearbyItemsPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				if (context.player().currentScreenHandler instanceof NearbyCraftingAccess access) {
					access.derk$cancelNearbyWithdrawals();
				}
			});
		});
	}
}

