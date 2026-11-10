package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Raises one enchantment on the pedestal item up to a maximum level, if the item supports it.
 * Used by Armorer (armor, protection) and Sharpening (weapons, sharpness) through their JSON definitions.
 */
public class EnchantBuffBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<EnchantBuffBeeEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.BOOL.optionalFieldOf("dominant", false).forGetter(e -> e.dominant),
			Codec.INT.optionalFieldOf("throttle", 200).forGetter(e -> e.throttle),
			ResourceLocation.CODEC.fieldOf("enchantment").forGetter(e -> e.enchantment),
			Codec.INT.optionalFieldOf("max_level", 2).forGetter(e -> e.maxLevel)
	).apply(i, EnchantBuffBeeEffect::new));

	private final ResourceLocation enchantment;
	private final int maxLevel;

	public EnchantBuffBeeEffect(boolean dominant, int throttle, ResourceLocation enchantment, int maxLevel) {
		super(dominant, throttle);
		this.enchantment = enchantment;
		this.maxLevel = maxLevel;
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		Holder<Enchantment> holder = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
				.getHolder(enchantment).orElse(null);
		ItemStack stack = pedestal.getStack();
		if (holder == null || !holder.value().isSupportedItem(stack)) {
			return;
		}
		int current = EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
		if (current >= maxLevel) {
			return;
		}
		// Do not mix in an enchantment that conflicts with what is already there.
		if (current == 0) {
			for (Holder<Enchantment> existing : stack.getEnchantments().keySet()) {
				if (!Enchantment.areCompatible(holder, existing)) {
					return;
				}
			}
		}
		stack.enchant(holder, current + 1);
		pedestal.setStack(stack);
		level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.3);
	}
}
