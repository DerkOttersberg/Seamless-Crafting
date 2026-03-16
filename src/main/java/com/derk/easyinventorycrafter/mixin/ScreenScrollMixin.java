package com.derk.easyinventorycrafter.mixin;

import com.derk.easyinventorycrafter.client.NearbyPanelAccess;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenScrollMixin {
	@Inject(method = "mouseScrolled(DDD)Z", at = @At("HEAD"), cancellable = true, require = 0)
	private void derk$handleScroll(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
		if ((Object)this instanceof NearbyPanelAccess access) {
			if (access.derk$handleScroll(mouseX, mouseY, amount)) {
				cir.setReturnValue(true);
			}
		}
	}
}
