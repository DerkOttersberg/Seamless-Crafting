package com.derk.easyinventorycrafter.mixin;

import com.derk.easyinventorycrafter.NearbyCraftingAccess;
import com.derk.easyinventorycrafter.NearbyInventoryScanner;
import com.derk.easyinventorycrafter.NearbyInventoryScanner.WorldPos;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputSlotFiller.class)
public abstract class InputSlotFillerMixin {
	@Shadow
	protected PlayerInventory inventory;

	@Shadow
	protected AbstractRecipeScreenHandler handler;

	@Inject(method = "fillInputSlot", at = @At("HEAD"), cancellable = true)
	private void derk$fillFromNearby(Slot slot, ItemStack item, int count, CallbackInfoReturnable<Integer> cir) {
		int targetCount = count;
		ItemStack slotStack = slot.getStack();
		int availableInPlayer = this.derk$countInPlayerInventory(item, slotStack);
		if (availableInPlayer >= targetCount) {
			return;
		}

		CraftingScreenHandler screenHandler = this.derk$resolveScreenHandler();
		if (screenHandler == null || !(screenHandler instanceof NearbyCraftingAccess access)) {
			cir.setReturnValue(-1);
			return;
		}

		ScreenHandlerContext context = access.derk$getContext();
		WorldPos worldPos = NearbyInventoryScanner.getWorldPos(context);
		if (worldPos == null) {
			cir.setReturnValue(-1);
			return;
		}

		World world = worldPos.world();
		List<Inventory> inventories = NearbyInventoryScanner.findNearbyInventories(
				world,
				worldPos.pos(),
				NearbyInventoryScanner.getConfiguredRadius()
		);

		int availableInNearby = derk$countInInventories(inventories, item, slotStack);
		if (availableInPlayer + availableInNearby < targetCount) {
			cir.setReturnValue(-1);
			return;
		}

		int remaining = targetCount;
		remaining = this.derk$takeFromPlayerInventory(item, slotStack, slot, remaining);
		slotStack = slot.getStack();
		if (remaining <= 0) {
			cir.setReturnValue(0);
			return;
		}
		for (Inventory inv : inventories) {
			for (int i = 0; i < inv.size(); i++) {
				ItemStack stack = inv.getStack(i);
				if (stack.isEmpty()) {
					continue;
				}

				if (!this.derk$matchesItem(stack, item) || !this.derk$isUsableWhenFillingSlot(stack)) {
					continue;
				}

				if (!slotStack.isEmpty() && !ItemStack.areItemsAndComponentsEqual(slotStack, stack)) {
					continue;
				}

				int baselineCount = slotStack.isEmpty() ? 0 : slotStack.getCount();
				int removeCount = Math.min(remaining, stack.getCount());
				ItemStack removed = inv.removeStack(i, removeCount);
				if (removed.isEmpty()) {
					continue;
				}

				if (slotStack.isEmpty()) {
					slot.setStackNoCallbacks(removed);
					slotStack = removed;
				} else {
					slotStack.increment(removed.getCount());
					slot.markDirty();
				}

				access.derk$recordNearbyWithdrawal(inv, i, this.derk$getCraftingSlotIndex(screenHandler, slot), removed, removed.getCount(), baselineCount);
				inv.markDirty();
				remaining -= removed.getCount();
				if (remaining <= 0) {
					cir.setReturnValue(0);
					return;
				}
			}
		}

		cir.setReturnValue(remaining == targetCount ? -1 : remaining);
	}

	@Inject(method = "returnInputs", at = @At("HEAD"))
	private void derk$returnNearbyInputsToOrigin(CallbackInfo ci) {
		CraftingScreenHandler screenHandler = this.derk$resolveScreenHandler();
		if (screenHandler instanceof NearbyCraftingAccess access) {
			access.derk$prepareNearbyWithdrawalsForAutofill();
		}
	}

	private int derk$countInInventories(List<Inventory> inventories, ItemStack item, ItemStack slotStack) {
		int total = 0;
		for (Inventory inv : inventories) {
			for (int i = 0; i < inv.size(); i++) {
				ItemStack stack = inv.getStack(i);
				if (stack.isEmpty()) {
					continue;
				}
				if (!this.derk$matchesItem(stack, item) || !this.derk$isUsableWhenFillingSlot(stack)) {
					continue;
				}
				if (!slotStack.isEmpty() && !ItemStack.areItemsAndComponentsEqual(slotStack, stack)) {
					continue;
				}
				total += stack.getCount();
				if (total >= Integer.MAX_VALUE - 1) {
					return total;
				}
			}
		}
		return total;
	}

	private int derk$countInPlayerInventory(ItemStack item, ItemStack slotStack) {
		int total = 0;
		for (ItemStack stack : this.inventory.main) {
			if (stack.isEmpty()) {
				continue;
			}
			if (!this.derk$matchesItem(stack, item) || !this.derk$isUsableWhenFillingSlot(stack)) {
				continue;
			}
			if (!slotStack.isEmpty() && !ItemStack.areItemsAndComponentsEqual(slotStack, stack)) {
				continue;
			}
			total += stack.getCount();
		}
		return total;
	}

	private int derk$takeFromPlayerInventory(ItemStack item, ItemStack slotStack, Slot slot, int remaining) {
		int stillNeeded = remaining;
		for (int i = 0; i < this.inventory.main.size() && stillNeeded > 0; i++) {
			ItemStack stack = this.inventory.getStack(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (!this.derk$matchesItem(stack, item) || !this.derk$isUsableWhenFillingSlot(stack)) {
				continue;
			}
			if (!slotStack.isEmpty() && !ItemStack.areItemsAndComponentsEqual(slotStack, stack)) {
				continue;
			}

			int removeCount = Math.min(stillNeeded, stack.getCount());
			ItemStack removed = this.inventory.removeStack(i, removeCount);
			if (removed.isEmpty()) {
				continue;
			}

			if (slotStack.isEmpty()) {
				slot.setStackNoCallbacks(removed);
				slotStack = removed;
			} else {
				slotStack.increment(removed.getCount());
				slot.markDirty();
			}

			stillNeeded -= removed.getCount();
		}

		return stillNeeded;
	}

	@Nullable
	private CraftingScreenHandler derk$resolveScreenHandler() {
		if (this.handler instanceof CraftingScreenHandler screenHandler) {
			return screenHandler;
		}

		Object handlerObj = this.handler;
		if (handlerObj == null) {
			return null;
		}

		for (Field field : handlerObj.getClass().getDeclaredFields()) {
			if (CraftingScreenHandler.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				try {
					return (CraftingScreenHandler)field.get(handlerObj);
				} catch (IllegalAccessException ignored) {
					return null;
				}
			}
		}

		return null;
	}

	private int derk$getCraftingSlotIndex(CraftingScreenHandler screenHandler, Slot slot) {
		int slotId = slot.id;
		if (slotId <= 0 || slotId > screenHandler.getCraftingSlotCount()) {
			return -1;
		}
		return slotId - 1;
	}

	private boolean derk$matchesItem(ItemStack candidate, ItemStack expected) {
		return ItemStack.areItemsEqual(candidate, expected);
	}

	private boolean derk$isUsableWhenFillingSlot(ItemStack stack) {
		return !stack.isEmpty() && !stack.isDamaged();
	}
}
