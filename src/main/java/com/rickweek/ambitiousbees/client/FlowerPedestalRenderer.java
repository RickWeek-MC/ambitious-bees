package com.rickweek.ambitiousbees.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the held item floating and slowly spinning above the flower. */
public class FlowerPedestalRenderer implements BlockEntityRenderer<FlowerPedestalBlockEntity> {
	private final BlockEntityRendererProvider.Context context;

	public FlowerPedestalRenderer(BlockEntityRendererProvider.Context context) {
		this.context = context;
	}

	@Override
	public void render(FlowerPedestalBlockEntity be, float partialTick, PoseStack poseStack,
					   MultiBufferSource buffer, int packedLight, int packedOverlay) {
		ItemStack stack = be.getStack();
		if (stack.isEmpty() || be.getLevel() == null) {
			return;
		}
		float time = be.getLevel().getGameTime() + partialTick;
		poseStack.pushPose();
		poseStack.translate(0.5, 1.15 + Math.sin(time / 10.0) * 0.03, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.0f));
		poseStack.scale(0.3f, 0.3f, 0.3f);
		int light = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
		context.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light,
				OverlayTexture.NO_OVERLAY, poseStack, buffer, be.getLevel(), (int) be.getBlockPos().asLong());
		poseStack.popPose();
	}
}
