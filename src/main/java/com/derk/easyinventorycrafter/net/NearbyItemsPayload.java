package com.derk.easyinventorycrafter.net;

import com.derk.easyinventorycrafter.NearbyInventoryScanner.NearbyItemEntry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record NearbyItemsPayload(List<NearbyItemEntry> entries, List<ItemStack> recipeFinderStacks) {
	public static final Identifier ID = new Identifier("easyinventorycrafter", "nearby_items");

	public static NearbyItemsPayload decode(PacketByteBuf buf) {
		int entryCount = buf.readVarInt();
		List<NearbyItemEntry> entries = new ArrayList<>(entryCount);
		for (int i = 0; i < entryCount; i++) {
			ItemStack stack = buf.readItemStack();
			int count = buf.readVarInt();
			entries.add(new NearbyItemEntry(stack, count));
		}

		int stackCount = buf.readVarInt();
		List<ItemStack> recipeFinderStacks = new ArrayList<>(stackCount);
		for (int i = 0; i < stackCount; i++) {
			recipeFinderStacks.add(buf.readItemStack());
		}

		return new NearbyItemsPayload(entries, recipeFinderStacks);
	}

	public void encode(PacketByteBuf buf) {
		buf.writeVarInt(entries.size());
		for (NearbyItemEntry entry : entries) {
			buf.writeItemStack(entry.stack());
			buf.writeVarInt(entry.count());
		}

		buf.writeVarInt(recipeFinderStacks.size());
		for (ItemStack stack : recipeFinderStacks) {
			buf.writeItemStack(stack);
		}
	}
}
