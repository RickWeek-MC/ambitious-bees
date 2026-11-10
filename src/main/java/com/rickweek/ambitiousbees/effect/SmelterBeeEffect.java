package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/** Smelts the item lying on flower pedestals inside the bee's territory. */
public class SmelterBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<SmelterBeeEffect> CODEC = makeCodec(100, SmelterBeeEffect::new);

	public SmelterBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		SingleRecipeInput input = new SingleRecipeInput(pedestal.getStack());
		level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level).ifPresent(holder -> {
			ItemStack result = holder.value().assemble(input, level.registryAccess());
			if (!result.isEmpty()) {
				pedestal.setStack(result.copyWithCount(1));
				level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.01);
			}
		});
	}
}
