package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Slowly repairs the damaged item on flower pedestals. */
public class RepairBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<RepairBeeEffect> CODEC = makeCodec(40, RepairBeeEffect::new);
	private static final int REPAIR_PER_TICK_CYCLE = 8;

	public RepairBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		ItemStack stack = pedestal.getStack();
		if (stack.isDamaged()) {
			stack.setDamageValue(Math.max(0, stack.getDamageValue() - REPAIR_PER_TICK_CYCLE));
			pedestal.setStack(stack);
			level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 2, 0.15, 0.1, 0.15, 0.0);
		}
	}
}
