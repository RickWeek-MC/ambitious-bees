package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;


/** Like the miner, but ores drop as blocks (silk touch). */
public class SilkyMinerBeeEffect extends MinerBeeEffect {
	public static final MapCodec<SilkyMinerBeeEffect> CODEC = makeCodec(100, SilkyMinerBeeEffect::new);

	public SilkyMinerBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected ItemStack createTool(ServerLevel level) {
		return enchanted(level, Enchantments.SILK_TOUCH, 1);
	}
}
