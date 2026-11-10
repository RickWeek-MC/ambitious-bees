package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import com.rickweek.ambitiousbees.block.FlowerPedestalBlockEntity;
import forestry.api.apiculture.genetics.IBeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.Optional;

/**
 * Crushes ores/raw materials/ingots/gems on flower pedestals into dust, fully tag based:
 * an item tagged {@code c:ores/<material>} (or raw_materials, ingots, gems) becomes an item
 * from {@code c:dusts/<material>}. Works with any mod that follows the common tag convention.
 */
public class OreCrusherBeeEffect extends PedestalBeeEffect {
	public static final MapCodec<OreCrusherBeeEffect> CODEC = makeCodec(100, OreCrusherBeeEffect::new);

	public OreCrusherBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void process(ServerLevel level, BlockPos pos, FlowerPedestalBlockEntity pedestal) {
		ItemStack input = pedestal.getStack();
		for (TagKey<Item> tag : (Iterable<TagKey<Item>>) input.getTags()::iterator) {
			ResourceLocation id = tag.location();
			if (!id.getNamespace().equals("c")) {
				continue;
			}
			String path = id.getPath();
			int perItem;
			String material;
			if (path.startsWith("ores/")) {
				perItem = 2 + level.random.nextInt(2); // 2-3 dusts per ore
				material = path.substring("ores/".length());
			} else if (path.startsWith("raw_materials/")) {
				perItem = 2;
				material = path.substring("raw_materials/".length());
			} else if (path.startsWith("ingots/")) {
				perItem = 1;
				material = path.substring("ingots/".length());
			} else if (path.startsWith("gems/")) {
				perItem = 1;
				material = path.substring("gems/".length());
			} else {
				continue;
			}
			Optional<Item> dust = findDust(material);
			if (dust.isPresent()) {
				int count = Math.min(perItem * input.getCount(), dust.get().getDefaultMaxStackSize());
				pedestal.setStack(new ItemStack(dust.get(), count));
				level.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.15, 0.1, 0.15, 0.05);
				return;
			}
		}
	}

	/** First item (sorted by registry id, so it is deterministic) in {@code c:dusts/<material>}. */
	private static Optional<Item> findDust(String material) {
		TagKey<Item> dustTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "dusts/" + material));
		return BuiltInRegistries.ITEM.getTag(dustTag).flatMap(set -> set.stream()
				.map(Holder::value)
				.min(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString())));
	}
}
