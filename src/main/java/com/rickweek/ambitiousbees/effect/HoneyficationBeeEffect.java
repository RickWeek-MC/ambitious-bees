package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import com.rickweek.ambitiousbees.registry.ModRegistries;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Iron Honeyfication: an iron ingot on a flower pedestal in the territory becomes a honey-comb ingot. */
public class HoneyficationBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<HoneyficationBeeEffect> CODEC = makeCodec(100, HoneyficationBeeEffect::new);

	public HoneyficationBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		ItemStack stack = pedestal.getStack();
		if (!stack.is(Items.IRON_INGOT)) {
			return;
		}
		pedestal.setStack(new ItemStack(ModRegistries.HONEY_COMB_INGOT.get(), stack.getCount()));
		level.sendParticles(ParticleTypes.FALLING_HONEY, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.15, 0.1, 0.15, 0.01);
	}
}
