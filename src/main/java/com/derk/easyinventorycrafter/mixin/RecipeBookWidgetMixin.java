package com.derk.easyinventorycrafter.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.util.InputUtil;
import net.minecraft.recipe.RecipeEntry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {
	@Shadow
	@Nullable
	private MinecraftClient client;

	@Shadow
	private RecipeBookResults recipesArea;

	@Shadow
	private RecipeBookGhostSlots ghostSlots;

	@Shadow
	protected abstract void refreshInputs();

	@Inject(method = "refresh", at = @At("HEAD"))
	private void derk$refreshInputsForNearbyPayload(CallbackInfo ci) {
		this.refreshInputs();
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void derk$spacebarAddsOneSet(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (keyCode != InputUtil.GLFW_KEY_SPACE) {
			return;
		}

		RecipeEntry<?> recipe = this.recipesArea.getLastClickedRecipe();
		RecipeResultCollection results = this.recipesArea.getLastClickedResults();
		if (recipe == null || results == null || !results.isCraftable(recipe)) {
			return;
		}

		if (this.ghostSlots.getRecipe() == recipe) {
			return;
		}

		this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0f));
		this.ghostSlots.reset();
		this.client.interactionManager.clickRecipe(this.client.player.currentScreenHandler.syncId, recipe, false);
		cir.setReturnValue(true);
	}
}
