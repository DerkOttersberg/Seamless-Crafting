package com.derk.easyinventorycrafter.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.recipe.Recipe;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {
	@Shadow
	private MinecraftClient client;

	@Shadow
	private RecipeBookResults recipesArea;

	@Shadow
	private TextFieldWidget searchField;

	@Shadow
	public abstract boolean isOpen();

	@Invoker("refreshInputs")
	protected abstract void derk$invokeRefreshInputs();

	@Inject(method = "refresh", at = @At("HEAD"))
	private void derk$refreshInputsForNearbyPayload(CallbackInfo ci) {
		this.derk$invokeRefreshInputs();
	}

	@Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
	private void derk$spacebarAddsOneSet(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (keyCode != GLFW.GLFW_KEY_SPACE) {
			return;
		}
		if (!this.isOpen()) {
			return;
		}
		if (this.searchField != null && this.searchField.isFocused()) {
			return;
		}

		Recipe<?> recipe = this.recipesArea.getLastClickedRecipe();
		RecipeResultCollection results = this.recipesArea.getLastClickedResults();
		if (recipe == null || results == null) {
			return;
		}
		if (!results.isCraftable(recipe)) {
			return;
		}

		MinecraftClient mc = this.client;
		if (mc == null || mc.player == null || mc.interactionManager == null) {
			return;
		}

		mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
		mc.interactionManager.clickRecipe(
				mc.player.currentScreenHandler.syncId,
				recipe,
				false
		);
		cir.setReturnValue(true);
	}
}
