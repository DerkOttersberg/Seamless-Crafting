package com.derk.seamlesscrafting.mixin;

import com.derk.seamlesscrafting.NearbyInventoryScanner.NearbyItemEntry;
import com.derk.seamlesscrafting.client.NearbyItemsClientState;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.screen.AbstractCraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCraftingScreenHandler.class)
public class AbstractCraftingScreenHandlerClientMixin {
	@Inject(method = "populateRecipeFinder", at = @At("TAIL"))
	private void derk$addNearbyClientItems(RecipeFinder finder, CallbackInfo ci) {
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

			finder.addInputIfUsable(stack);
		}
	}
}
