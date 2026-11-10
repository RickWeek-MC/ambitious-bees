package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

/** Swaps similar natural blocks (ores, leaves, sand, dirt, flowers...) in the territory. */
public class QuantumStrangeBeeEffect extends QuantumSwapBeeEffect {
	public static final MapCodec<QuantumStrangeBeeEffect> CODEC = makeCodec(100, QuantumStrangeBeeEffect::new);
	private static final List<TagKey<Block>> GROUPS = List.of(Tags.Blocks.ORES, BlockTags.LEAVES, BlockTags.SAND, BlockTags.DIRT, BlockTags.SMALL_FLOWERS);

	public QuantumStrangeBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected List<TagKey<Block>> groups() {
		return GROUPS;
	}
}
