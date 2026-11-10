package com.rickweek.ambitiousbees.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

/** Any potion (drinkable, splash or lingering, vanilla or modded) that has the poison effect. */
public final class PoisonPotionIngredient implements ICustomIngredient {
	public static final PoisonPotionIngredient INSTANCE = new PoisonPotionIngredient();
	public static final MapCodec<PoisonPotionIngredient> CODEC = MapCodec.unit(INSTANCE);
	public static final IngredientType<PoisonPotionIngredient> TYPE =
			new IngredientType<>(CODEC, StreamCodec.<RegistryFriendlyByteBuf, PoisonPotionIngredient>unit(INSTANCE));

	private PoisonPotionIngredient() {}

	@Override
	public boolean test(ItemStack stack) {
		if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) {
			return false;
		}
		PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
		if (contents == null) {
			return false;
		}
		for (MobEffectInstance effect : contents.getAllEffects()) {
			if (effect.getEffect().is(MobEffects.POISON)) {
				return true;
			}
		}
		return false;
	}

	/** Shown in recipe viewers. */
	@Override
	public Stream<ItemStack> getItems() {
		Stream.Builder<ItemStack> builder = Stream.builder();
		for (Item item : new Item[]{Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION}) {
			builder.add(PotionContents.createItemStack(item, Potions.POISON));
			builder.add(PotionContents.createItemStack(item, Potions.LONG_POISON));
			builder.add(PotionContents.createItemStack(item, Potions.STRONG_POISON));
		}
		return builder.build();
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IngredientType<?> getType() {
		return TYPE;
	}
}
