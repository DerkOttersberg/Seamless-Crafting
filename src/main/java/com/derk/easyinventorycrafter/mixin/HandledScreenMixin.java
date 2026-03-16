package com.derk.easyinventorycrafter.mixin;

import com.derk.easyinventorycrafter.client.NearbyPanelAccess;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
	@Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
	private void derk$handleMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if ((Object)this instanceof NearbyPanelAccess access) {
			if (access.derk$handleMouseClick(mouseX, mouseY, button, false)) {
				cir.setReturnValue(true);
			}
		}
	}
}
