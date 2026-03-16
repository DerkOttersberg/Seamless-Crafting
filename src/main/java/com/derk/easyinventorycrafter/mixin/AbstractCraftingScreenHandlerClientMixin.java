package com.derk.easyinventorycrafter.mixin;

import com.derk.easyinventorycrafter.NearbyInventoryScanner.NearbyItemEntry;
import com.derk.easyinventorycrafter.client.NearbyItemsClientState;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public class AbstractCraftingScreenHandlerClientMixin {
	@Inject(method = "populateRecipeFinder", at = @At("TAIL"))
	private void derk$addNearbyClientItems(RecipeMatcher finder, CallbackInfo ci) {
		if ((Object)this instanceof PlayerScreenHandler) {
			return;
		}

		if (!MinecraftClient.getInstance().isOnThread()) {
			return;
		}

		List<ItemStack> nearbyStacks = NearbyItemsClientState.getRecipeFinderStacks();
		for (ItemStack stack : nearbyStacks) {
			if (stack.isEmpty()) {
				continue;
			}

			finder.addInput(stack);
		}
	}
}
