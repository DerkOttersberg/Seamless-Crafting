package com.derk.seamlesscrafting.net;

import com.derk.seamlesscrafting.SeamlessCraftingMod;
import com.derk.seamlesscrafting.NearbyInventoryScanner.NearbyItemEntry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NearbyItemsPayload(List<NearbyItemEntry> entries, List<ItemStack> recipeFinderStacks) implements CustomPayload {
	public static final CustomPayload.Id<NearbyItemsPayload> ID = new CustomPayload.Id<>(
			Identifier.of(SeamlessCraftingMod.MOD_ID, "nearby_items")
	);
	public static final PacketCodec<RegistryByteBuf, NearbyItemsPayload> CODEC = PacketCodec.of(
			NearbyItemsPayload::encode,
			NearbyItemsPayload::decode
	);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}

	private static NearbyItemsPayload decode(RegistryByteBuf buf) {
		int entryCount = buf.readVarInt();
		List<NearbyItemEntry> entries = new ArrayList<>(entryCount);
		for (int i = 0; i < entryCount; i++) {
			ItemStack stack = ItemStack.PACKET_CODEC.decode(buf);
			int count = buf.readVarInt();
			entries.add(new NearbyItemEntry(stack, count));
		}

		int stackCount = buf.readVarInt();
		List<ItemStack> recipeFinderStacks = new ArrayList<>(stackCount);
		for (int i = 0; i < stackCount; i++) {
			recipeFinderStacks.add(ItemStack.PACKET_CODEC.decode(buf));
		}

		return new NearbyItemsPayload(entries, recipeFinderStacks);
	}

	private static void encode(NearbyItemsPayload payload, RegistryByteBuf buf) {
		buf.writeVarInt(payload.entries.size());
		for (NearbyItemEntry entry : payload.entries) {
			ItemStack.PACKET_CODEC.encode(buf, entry.stack());
			buf.writeVarInt(entry.count());
		}

		buf.writeVarInt(payload.recipeFinderStacks.size());
		for (ItemStack stack : payload.recipeFinderStacks) {
			ItemStack.PACKET_CODEC.encode(buf, stack);
		}
	}
}

