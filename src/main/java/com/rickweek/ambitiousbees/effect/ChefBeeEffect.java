package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

/** Glazes food lying on flower pedestals with honey: eating it grants Absorption. */
public class ChefBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<ChefBeeEffect> CODEC = makeCodec(100, ChefBeeEffect::new);

	private static final String GLAZED_KEY = "ambitious_bees_glazed";
	private static final int ABSORPTION_TICKS = 2400;
	private static final int ABSORPTION_AMPLIFIER = 1;

	public ChefBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		ItemStack stack = pedestal.getStack();
		FoodProperties food = stack.get(DataComponents.FOOD);
		if (food == null || stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains(GLAZED_KEY)) {
			return;
		}
		ItemStack glazed = stack.copy();

		List<FoodProperties.PossibleEffect> effects = new ArrayList<>(food.effects());
		effects.add(new FoodProperties.PossibleEffect(
				() -> new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_TICKS, ABSORPTION_AMPLIFIER), 1.0f));
		glazed.set(DataComponents.FOOD, new FoodProperties(food.nutrition(), food.saturation(), food.canAlwaysEat(),
				food.eatSeconds(), food.usingConvertsTo(), effects));

		CustomData.update(DataComponents.CUSTOM_DATA, glazed, tag -> tag.putBoolean(GLAZED_KEY, true));

		List<Component> lore = new ArrayList<>(glazed.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines());
		lore.add(Component.translatable("tooltip.ambitious_bees.honey_glazed").withStyle(ChatFormatting.GOLD));
		glazed.set(DataComponents.LORE, new ItemLore(lore));

		pedestal.setStack(glazed);
		level.sendParticles(ParticleTypes.FALLING_HONEY, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 6, 0.15, 0.1, 0.15, 0.0);
	}
}
