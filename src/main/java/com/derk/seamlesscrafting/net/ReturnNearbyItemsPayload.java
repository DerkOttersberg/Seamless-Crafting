package com.derk.seamlesscrafting.net;

import com.derk.seamlesscrafting.SeamlessCraftingMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReturnNearbyItemsPayload() implements CustomPayload {
	public static final CustomPayload.Id<ReturnNearbyItemsPayload> ID = new CustomPayload.Id<>(
			Identifier.of(SeamlessCraftingMod.MOD_ID, "return_nearby_items")
	);
	public static final PacketCodec<RegistryByteBuf, ReturnNearbyItemsPayload> CODEC = PacketCodec.unit(new ReturnNearbyItemsPayload());

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
