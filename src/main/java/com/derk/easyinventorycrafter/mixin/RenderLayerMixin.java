package com.derk.easyinventorycrafter.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderLayer.class)
public interface RenderLayerMixin {
	@Invoker("of")
	static RenderLayer easyinventorycrafter$of(String name, RenderSetup renderSetup) {
		throw new AssertionError();
	}
}