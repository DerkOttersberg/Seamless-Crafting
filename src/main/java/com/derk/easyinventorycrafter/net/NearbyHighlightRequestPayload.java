package com.derk.easyinventorycrafter.net;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record NearbyHighlightRequestPayload(ItemStack stack) {
	public static final Identifier ID = new Identifier("easyinventorycrafter", "highlight_request");

	public static NearbyHighlightRequestPayload decode(PacketByteBuf buf) {
		return new NearbyHighlightRequestPayload(buf.readItemStack());
	}

	public void encode(PacketByteBuf buf) {
		buf.writeItemStack(stack);
	}
}
