package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

/** Sells the item on the pedestal for emeralds (the better the rarity, the better the price). */
public class BusinessBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<BusinessBeeEffect> CODEC = makeCodec(200, BusinessBeeEffect::new);

	public BusinessBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	/** Items needed to earn one emerald. */
	private static int itemsPerEmerald(ItemStack stack) {
		Rarity rarity = stack.getRarity();
		boolean enchanted = stack.isEnchanted();
		if (rarity == Rarity.EPIC) {
			return 1;
		}
		if (rarity == Rarity.RARE || enchanted) {
			return 2;
		}
		if (rarity == Rarity.UNCOMMON) {
			return 4;
		}
		return stack.isDamageableItem() ? 4 : 16;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		ItemStack stack = pedestal.getStack();
		if (stack.is(Items.EMERALD) || stack.is(Items.EMERALD_BLOCK)) {
			return;
		}
		int per = itemsPerEmerald(stack);
		int emeralds = stack.getCount() / per;
		if (level.random.nextInt(per) < stack.getCount() % per) {
			emeralds++;
		}
		emeralds = Math.max(1, emeralds);
		pedestal.setStack(new ItemStack(Items.EMERALD, Math.min(64, emeralds)));
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.25, 0.25, 0.25, 0.0);
	}
}
