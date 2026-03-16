package com.derk.easyinventorycrafter.mixin;

import com.derk.easyinventorycrafter.NearbyCraftingAccess;
import com.derk.easyinventorycrafter.NearbyInventoryScanner;
import com.derk.easyinventorycrafter.NearbyInventoryScanner.WorldPos;
import java.util.List;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InputSlotFiller.class)
public abstract class InputSlotFillerMixin {
	@Shadow
	private PlayerInventory inventory;

	@Shadow
	private AbstractRecipeScreenHandler handler;

	@Inject(method = "fillInputSlot", at = @At("HEAD"), cancellable = true)
	private void derk$fillFromNearby(Slot slot, ItemStack stack, CallbackInfo ci) {
		if (stack == null || stack.isEmpty()) {
			return;
		}

		if (!(this.handler instanceof NearbyCraftingAccess access)) {
			return;
		}

		// If player inventory has at least one matching stack, vanilla can handle this item.
		if (this.inventory.indexOf(stack) != -1) {
			return;
		}

		ItemStack slotStack = slot.getStack();
		if (!slotStack.isEmpty() && !derk$stacksMatch(slotStack, stack)) {
			return;
		}

		int maxCount = Math.min(stack.getMaxCount(), slot.inventory.getMaxCountPerStack());
		if (!slotStack.isEmpty() && slotStack.getCount() >= maxCount) {
			return;
		}

		ScreenHandlerContext context = access.derk$getContext();
		WorldPos worldPos = NearbyInventoryScanner.getWorldPos(context);
		if (worldPos == null) {
			return;
		}

		World world = worldPos.world();
		if (world.isClient()) {
			return;
		}

		List<Inventory> nearbyInvs = NearbyInventoryScanner.findNearbyInventories(
				world,
				worldPos.pos(),
				NearbyInventoryScanner.getConfiguredRadius()
		);
		if (nearbyInvs.isEmpty()) {
			return;
		}

		for (Inventory inv : nearbyInvs) {
			for (int i = 0; i < inv.size(); i++) {
				ItemStack candidate = inv.getStack(i);
				if (candidate.isEmpty()) {
					continue;
				}
				if (!derk$stacksMatch(candidate, stack)) {
					continue;
				}

				int baselineCount = slotStack.isEmpty() ? 0 : slotStack.getCount();
				ItemStack removed = inv.removeStack(i, 1);
				if (removed.isEmpty()) {
					continue;
				}

				if (slotStack.isEmpty()) {
					slot.setStackNoCallbacks(removed);
				} else {
					slotStack.increment(1);
					slot.markDirty();
				}

				access.derk$recordNearbyWithdrawal(inv, i, slot.getIndex(), removed, 1, baselineCount);
				inv.markDirty();
				ci.cancel();
				return;
			}
		}
	}

	@Inject(method = "returnInputs", at = @At("HEAD"))
	private void derk$returnNearbyInputsToOrigin(CallbackInfo ci) {
		if (this.handler instanceof NearbyCraftingAccess access) {
			access.derk$prepareNearbyWithdrawalsForAutofill();
		}
	}

	@Unique
	private static boolean derk$stacksMatch(ItemStack a, ItemStack b) {
		return ItemStack.canCombine(a, b);
	}
}
