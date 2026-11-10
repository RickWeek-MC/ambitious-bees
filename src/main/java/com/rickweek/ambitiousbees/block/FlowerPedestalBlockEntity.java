package com.rickweek.ambitiousbees.block;

import com.rickweek.ambitiousbees.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Holds a single item that bee effects can work on. */
public class FlowerPedestalBlockEntity extends BlockEntity {
	private ItemStack stack = ItemStack.EMPTY;

	public FlowerPedestalBlockEntity(BlockPos pos, BlockState state) {
		super(ModRegistries.FLOWER_PEDESTAL_BE.get(), pos, state);
	}

	public ItemStack getStack() {
		return stack;
	}

	public void setStack(ItemStack newStack) {
		this.stack = newStack;
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
			if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
				// Explicit sync: send the fresh state to every player tracking this chunk.
				Packet<ClientGamePacketListener> packet = getUpdatePacket();
				for (net.minecraft.server.level.ServerPlayer player
						: serverLevel.getChunkSource().chunkMap.getPlayers(new net.minecraft.world.level.ChunkPos(worldPosition), false)) {
					player.connection.send(packet);
				}
			}
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		// Always write the key: vanilla ignores empty update tags, so an empty pedestal must not send {}.
		tag.put("Item", stack.isEmpty() ? new CompoundTag() : stack.save(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		stack = tag.contains("Item") && !tag.getCompound("Item").isEmpty()
				? ItemStack.parseOptional(registries, tag.getCompound("Item")) : ItemStack.EMPTY;
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
