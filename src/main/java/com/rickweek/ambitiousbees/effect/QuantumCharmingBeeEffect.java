package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

/** Swaps similar building blocks (planks, logs, wool, stones...) in the territory. */
public class QuantumCharmingBeeEffect extends QuantumSwapBeeEffect {
	public static final MapCodec<QuantumCharmingBeeEffect> CODEC = makeCodec(100, QuantumCharmingBeeEffect::new);
	private static final List<TagKey<Block>> GROUPS = List.of(BlockTags.PLANKS, BlockTags.LOGS, BlockTags.WOOL, BlockTags.STONE_BRICKS, Tags.Blocks.STONES, Tags.Blocks.COBBLESTONES);

	public QuantumCharmingBeeEffect(boolean dominant, int throttle) {
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
