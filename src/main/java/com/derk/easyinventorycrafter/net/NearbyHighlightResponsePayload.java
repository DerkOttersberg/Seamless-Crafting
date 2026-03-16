package com.derk.easyinventorycrafter.net;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record NearbyHighlightResponsePayload(List<BlockPos> positions) {
	public static final Identifier ID = new Identifier("easyinventorycrafter", "highlight_response");

	public static NearbyHighlightResponsePayload decode(PacketByteBuf buf) {
		int size = buf.readVarInt();
		List<BlockPos> positions = new ArrayList<>(Math.max(0, size));
		for (int i = 0; i < size; i++) {
			positions.add(buf.readBlockPos());
		}
		return new NearbyHighlightResponsePayload(positions);
	}

	public void encode(PacketByteBuf buf) {
		buf.writeVarInt(positions.size());
		for (BlockPos pos : positions) {
			buf.writeBlockPos(pos);
		}
	}
}
