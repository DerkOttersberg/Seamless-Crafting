package com.derk.easyinventorycrafter.mixin;

import com.derk.easyinventorycrafter.AbstractCraftingScreenHandlerAccess;
import com.derk.easyinventorycrafter.NearbyCraftingAccess;
import com.derk.easyinventorycrafter.NearbyInventoryScanner;
import com.derk.easyinventorycrafter.NearbyInventoryScanner.WorldPos;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public class AbstractCraftingScreenHandlerMixin implements AbstractCraftingScreenHandlerAccess {

	@Override
	public Inventory derk$getCraftingInventory() {
		for (Field field : this.getClass().getDeclaredFields()) {
			if (RecipeInputInventory.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				try {
					return (Inventory) field.get(this);
				} catch (IllegalAccessException ignored) {}
			}
		}
		for (Slot slot : ((ScreenHandler)(Object)this).slots) {
			if (slot.inventory instanceof RecipeInputInventory) {
				return slot.inventory;
			}
		}
		return null;
	}

	@Inject(method = "populateRecipeFinder", at = @At("TAIL"))
	private void derk$addNearbyItems(RecipeMatcher finder, CallbackInfo ci) {
		if (!((Object)this instanceof NearbyCraftingAccess access)) {
			return;
		}

		ScreenHandlerContext context = access.derk$getContext();
		WorldPos worldPos = NearbyInventoryScanner.getWorldPos(context);
		if (worldPos == null) {
			return;
		}
		if (worldPos.world().isClient()) {
			return;
		}

		List<Inventory> inventories = NearbyInventoryScanner.findNearbyInventories(
				worldPos.world(),
				worldPos.pos(),
				NearbyInventoryScanner.getConfiguredRadius()
		);
		for (Inventory inventory : inventories) {
			for (int i = 0; i < inventory.size(); i++) {
				ItemStack stack = inventory.getStack(i);
				finder.addInput(stack);
			}
		}
	}
}
