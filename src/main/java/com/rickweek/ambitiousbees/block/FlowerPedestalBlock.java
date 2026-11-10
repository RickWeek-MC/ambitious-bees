package com.rickweek.ambitiousbees.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Block;

/** Small pedestal holding one item. Right-click with an item to place it, with an empty hand to take it. */
public class FlowerPedestalBlock extends BaseEntityBlock {
	public static final MapCodec<FlowerPedestalBlock> CODEC = simpleCodec(FlowerPedestalBlock::new);
	private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 14, 12);

	public FlowerPedestalBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FlowerPedestalBlockEntity(pos, state);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof FlowerPedestalBlockEntity pedestal)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (pedestal.getStack().isEmpty() && !held.isEmpty()) {
			if (!level.isClientSide) {
				pedestal.setStack(held.copyWithCount(1));
				if (!player.getAbilities().instabuild) {
					held.shrink(1);
				}
			}
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
																   Player player, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof FlowerPedestalBlockEntity pedestal && !pedestal.getStack().isEmpty()) {
			if (!level.isClientSide) {
				ItemStack out = pedestal.getStack();
				pedestal.setStack(ItemStack.EMPTY);
				if (!player.getInventory().add(out)) {
					player.drop(out, false);
				}
			}
			return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
		}
		return net.minecraft.world.InteractionResult.PASS;
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FlowerPedestalBlockEntity pedestal) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), pedestal.getStack());
		}
		super.onRemove(state, level, pos, newState, moving);
	}
}
