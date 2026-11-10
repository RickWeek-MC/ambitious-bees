package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

/** Enchants the item on the pedestal: adds or raises one random compatible (non-curse) enchantment per run. */
public class PriestBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<PriestBeeEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.BOOL.optionalFieldOf("dominant", false).forGetter(e -> e.dominant),
			Codec.INT.optionalFieldOf("throttle", 1200).forGetter(e -> e.throttle),
			Codec.INT.optionalFieldOf("max_level", 3).forGetter(e -> e.maxLevel),
			Codec.INT.optionalFieldOf("max_enchantments", 3).forGetter(e -> e.maxEnchantments)
	).apply(i, PriestBeeEffect::new));

	private final int maxLevel;
	private final int maxEnchantments;

	public PriestBeeEffect(boolean dominant, int throttle, int maxLevel, int maxEnchantments) {
		super(dominant, throttle);
		this.maxLevel = maxLevel;
		this.maxEnchantments = maxEnchantments;
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		ItemStack stack = pedestal.getStack();
		int existingCount = stack.getEnchantments().size();
		List<Holder<Enchantment>> candidates = new ArrayList<>();
		for (Holder.Reference<Enchantment> holder : level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().toList()) {
			if (holder.is(EnchantmentTags.CURSE) || !holder.value().isSupportedItem(stack)) {
				continue;
			}
			int current = EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
			if (current >= Math.min(maxLevel, holder.value().getMaxLevel())) {
				continue;
			}
			if (current == 0 && existingCount >= maxEnchantments) {
				continue;
			}
			boolean compatible = true;
			if (current == 0) {
				for (Holder<Enchantment> existing : stack.getEnchantments().keySet()) {
					if (!Enchantment.areCompatible(holder, existing)) {
						compatible = false;
						break;
					}
				}
			}
			if (compatible) {
				candidates.add(holder);
			}
		}
		if (candidates.isEmpty()) {
			return;
		}
		Holder<Enchantment> chosen = candidates.get(level.random.nextInt(candidates.size()));
		stack.enchant(chosen, EnchantmentHelper.getItemEnchantmentLevel(chosen, stack) + 1);
		pedestal.setStack(stack);
		level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.4);
	}
}
