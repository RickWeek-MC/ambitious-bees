package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;


/** Like the miner, but with a fortune III pickaxe. */
public class FortunateMinerBeeEffect extends MinerBeeEffect {
	public static final MapCodec<FortunateMinerBeeEffect> CODEC = makeCodec(100, FortunateMinerBeeEffect::new);

	public FortunateMinerBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected ItemStack createTool(ServerLevel level) {
		return enchanted(level, Enchantments.FORTUNE, 3);
	}
}
