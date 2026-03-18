package com.derk.seamlesscrafting.net;

import com.derk.seamlesscrafting.SeamlessCraftingMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestNearbyItemsPayload() implements CustomPayload {
	public static final CustomPayload.Id<RequestNearbyItemsPayload> ID = new CustomPayload.Id<>(
			Identifier.of(SeamlessCraftingMod.MOD_ID, "request_nearby_items")
	);
	public static final PacketCodec<RegistryByteBuf, RequestNearbyItemsPayload> CODEC = PacketCodec.unit(new RequestNearbyItemsPayload());

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}

