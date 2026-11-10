package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

/** Turns raw stone items lying on flower pedestals into dressed stone bricks. */
public class MasonBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<MasonBeeEffect> CODEC = makeCodec(100, MasonBeeEffect::new);

	private static final Map<Item, Item> CONVERSIONS = Map.of(
			Items.COBBLESTONE, Items.STONE_BRICKS,
			Items.STONE, Items.STONE_BRICKS,
			Items.COBBLED_DEEPSLATE, Items.DEEPSLATE_BRICKS,
			Items.DEEPSLATE, Items.DEEPSLATE_BRICKS,
			Items.MUD, Items.MUD_BRICKS
	);

	public MasonBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		Item result = CONVERSIONS.get(pedestal.getStack().getItem());
		if (result != null) {
			pedestal.setStack(new ItemStack(result));
			level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.01);
		}
	}
}
