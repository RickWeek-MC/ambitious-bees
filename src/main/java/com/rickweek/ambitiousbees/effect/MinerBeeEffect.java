package com.rickweek.ambitiousbees.effect;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

/** Breaks ore blocks in the territory and stores their drops. */
public class MinerBeeEffect extends TerritoryBeeEffect {
	public static final MapCodec<MinerBeeEffect> CODEC = makeCodec(100, MinerBeeEffect::new);

	/** Tool used to compute drops; subclasses add enchantments. */
	protected ItemStack createTool(ServerLevel level) {
		return new ItemStack(Items.NETHERITE_PICKAXE);
	}

	protected static ItemStack enchanted(ServerLevel level, net.minecraft.resources.ResourceKey<Enchantment> key, int lvl) {
		ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
		Holder<Enchantment> holder = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
		tool.enchant(holder, lvl);
		return tool;
	}

	public MinerBeeEffect(boolean dominant, int throttle) {
		super(dominant, throttle);
	}

	@Override
	public MapCodec<? extends IBeeEffect> codec() {
		return CODEC;
	}

	@Override
	protected void applyEffect(ServerLevel level, IGenome genome, IBeeHousing housing, AABB area) {
		int mined = 0;
		ItemStack tool = createTool(level);
		for (BlockPos pos : randomPositions(level, area, 64)) {
			BlockState state = level.getBlockState(pos);
			if (!state.is(Tags.Blocks.ORES) || state.getDestroySpeed(level, pos) < 0 || level.getBlockEntity(pos) != null) {
				continue;
			}
			if (!BlockGuard.canModify(level, pos, state, housing)) {
				continue;
			}
			List<ItemStack> drops = Block.getDrops(state, level, pos, null, null, tool);
			level.destroyBlock(pos, false);
			for (ItemStack drop : drops) {
				give(level, housing, drop, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
			}
			if (++mined >= 2) {
				break;
			}
		}
	}
}
