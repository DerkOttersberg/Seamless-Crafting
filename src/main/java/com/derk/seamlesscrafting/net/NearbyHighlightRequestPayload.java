package com.derk.seamlesscrafting.net;

import com.derk.seamlesscrafting.SeamlessCraftingMod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NearbyHighlightRequestPayload(ItemStack stack) implements CustomPayload {
	public static final CustomPayload.Id<NearbyHighlightRequestPayload> ID = new CustomPayload.Id<>(
			Identifier.of(SeamlessCraftingMod.MOD_ID, "highlight_request")
	);
	public static final PacketCodec<RegistryByteBuf, NearbyHighlightRequestPayload> CODEC = PacketCodec.of(
			NearbyHighlightRequestPayload::encode,
			NearbyHighlightRequestPayload::decode
	);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}

	private static NearbyHighlightRequestPayload decode(RegistryByteBuf buf) {
		ItemStack stack = ItemStack.PACKET_CODEC.decode(buf);
		return new NearbyHighlightRequestPayload(stack);
	}

	private static void encode(NearbyHighlightRequestPayload payload, RegistryByteBuf buf) {
		ItemStack.PACKET_CODEC.encode(buf, payload.stack());
	}
}
