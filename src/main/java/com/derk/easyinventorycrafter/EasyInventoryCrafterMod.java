package com.derk.easyinventorycrafter;

import com.derk.easyinventorycrafter.net.NearbyHighlightRequestPayload;
import com.derk.easyinventorycrafter.net.NearbyHighlightResponsePayload;
import com.derk.easyinventorycrafter.net.NearbyItemsPayload;
import com.derk.easyinventorycrafter.net.NearbyItemsSync;
import com.derk.easyinventorycrafter.net.RequestNearbyItemsPayload;
import com.derk.easyinventorycrafter.net.ReturnNearbyItemsPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.BlockPos;
import java.util.List;

public class EasyInventoryCrafterMod implements ModInitializer {
	public static final String MOD_ID = "derk_easy_inventory_crafter";

	@Override
	public void onInitialize() {
		EasyInventoryCrafterConfig.load();

		ServerPlayNetworking.registerGlobalReceiver(RequestNearbyItemsPayload.ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				if (player.currentScreenHandler instanceof CraftingScreenHandler
						|| player.currentScreenHandler instanceof PlayerScreenHandler) {
					NearbyItemsSync.sendNearbyItems(player);
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(NearbyHighlightRequestPayload.ID, (server, player, handler, buf, responseSender) -> {
			NearbyHighlightRequestPayload payload = NearbyHighlightRequestPayload.decode(buf);
			server.execute(() -> {
				if (!(player.currentScreenHandler instanceof CraftingScreenHandler)
						&& !(player.currentScreenHandler instanceof PlayerScreenHandler)) {
					return;
				}
				List<BlockPos> positions = NearbyItemsSync.findHighlightPositions(player, payload.stack());
				if (positions != null && !positions.isEmpty()) {
					NearbyHighlightResponsePayload response = new NearbyHighlightResponsePayload(positions);
					net.minecraft.network.PacketByteBuf responseBuf = PacketByteBufs.create();
					response.encode(responseBuf);
					ServerPlayNetworking.send(player, NearbyHighlightResponsePayload.ID, responseBuf);
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ReturnNearbyItemsPayload.ID, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				if (player.currentScreenHandler instanceof NearbyCraftingAccess access) {
					access.derk$cancelNearbyWithdrawals();
				}
			});
		});
	}
}
