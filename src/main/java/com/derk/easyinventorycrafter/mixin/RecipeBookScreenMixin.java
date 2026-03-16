package com.derk.easyinventorycrafter.mixin;

import com.derk.easyinventorycrafter.client.NearbyPanelAccess;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class RecipeBookScreenMixin {
	@Inject(method = "charTyped(CI)Z", at = @At("HEAD"), cancellable = true, require = 0)
	private void derk$handleCharTyped(char ch, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if ((Object)this instanceof NearbyPanelAccess access) {
			if (access.derk$handleCharTyped(ch, modifiers)) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true, require = 0)
	private void derk$handleKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if ((Object)this instanceof NearbyPanelAccess access) {
			if (access.derk$handleKeyPressed(keyCode, scanCode, modifiers)) {
				cir.setReturnValue(true);
			}
		}
	}
}
