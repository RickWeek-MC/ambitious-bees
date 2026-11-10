package com.rickweek.ambitiousbees.effect;

import com.rickweek.ambitiousbees.AmbitiousBees;
import com.rickweek.ambitiousbees.registry.ModRegistries;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.core.genetics.IIndividual;
import forestry.api.core.genetics.ISpecies;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A Student drone can be shift-right-clicked onto a bee housing: the housing is then watched
 * and players nearby are alerted (sound, particles, action bar) while its queen is gone.
 * The drone is stored in the housing block itself (data attachment): shift-right-clicking with an
 * empty hand, or breaking the housing, gives it back and stops the watch.
 * The saved position list below is only an index so that we do not scan every block entity.
 */
@EventBusSubscriber(modid = AmbitiousBees.MOD_ID)
public final class StudentAlert {
	private static final ResourceLocation STUDENT = ResourceLocation.fromNamespaceAndPath(AmbitiousBees.MOD_ID, "student");
	/** Ticks between two checks (and so between two alerts while the queen is missing). */
	private static final int CHECK_INTERVAL = 200;
	private static final double NOTIFY_RADIUS = 32.0;

	private StudentAlert() {}

	/** The watched housings of one dimension, saved with the world. */
	public static final class Watched extends SavedData {
		/** Watched housing position -> a copy of the drone stored in it (given back if the housing vanishes). */
		private final Map<BlockPos, ItemStack> positions = new HashMap<>();

		public Watched() {}

		static Watched load(CompoundTag tag, HolderLookup.Provider registries) {
			Watched data = new Watched();
			ListTag list = tag.getList("Watched", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				ItemStack drone = entry.contains("Drone")
						? ItemStack.parseOptional(registries, entry.getCompound("Drone")) : ItemStack.EMPTY;
				data.positions.put(BlockPos.of(entry.getLong("Pos")), drone);
			}
			// Older saves only stored the positions.
			for (long packed : tag.getLongArray("Positions")) {
				data.positions.putIfAbsent(BlockPos.of(packed), ItemStack.EMPTY);
			}
			return data;
		}

		@Override
		public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
			ListTag list = new ListTag();
			positions.forEach((pos, drone) -> {
				CompoundTag entry = new CompoundTag();
				entry.putLong("Pos", pos.asLong());
				if (!drone.isEmpty()) {
					entry.put("Drone", drone.save(registries));
				}
				list.add(entry);
			});
			tag.put("Watched", list);
			return tag;
		}
	}

	private static Watched watched(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(Watched::new, Watched::load, DataFixTypes.LEVEL), "ambitious_bees_student_alerts");
	}

	private static boolean isStudentDrone(ItemStack stack) {
		if (stack.isEmpty() || !IIndividualHandlerItem.hasIndividual(stack)
				|| IIndividualHandlerItem.getLifeStage(stack) != BeeLifeStage.DRONE) {
			return false;
		}
		IIndividual individual = IIndividualHandlerItem.getIndividual(stack);
		ISpecies<?> species = individual.getSpecies();
		return STUDENT.equals(species.id());
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		if (!player.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND) {
			return;
		}
		ItemStack held = event.getItemStack();
		boolean emptyHand = held.isEmpty();
		if (!emptyHand && !isStudentDrone(held)) {
			return;
		}
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof IBeeHousing)) {
			return;
		}
		boolean watching = blockEntity.hasData(ModRegistries.STUDENT_DRONE.get());
		if (emptyHand && !watching) {
			return;
		}
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		Watched data = watched(serverLevel);
		if (watching && emptyHand) {
			ItemStack drone = blockEntity.getData(ModRegistries.STUDENT_DRONE.get());
			blockEntity.removeData(ModRegistries.STUDENT_DRONE.get());
			blockEntity.setChanged();
			data.positions.remove(pos);
			data.setDirty();
			if (!drone.isEmpty()) {
				player.getInventory().placeItemBackInInventory(drone);
			}
			player.displayClientMessage(Component.translatable("message.ambitious_bees.student_off"), true);
		} else if (watching) {
			player.displayClientMessage(Component.translatable("message.ambitious_bees.student_already"), true);
		} else {
			ItemStack stored = held.copyWithCount(1);
			blockEntity.setData(ModRegistries.STUDENT_DRONE.get(), stored);
			blockEntity.setChanged();
			if (!player.getAbilities().instabuild) {
				held.shrink(1);
			}
			data.positions.put(pos.immutable(), stored.copy());
			data.setDirty();
			player.displayClientMessage(Component.translatable("message.ambitious_bees.student_on"), true);
			serverLevel.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, 1.0f);
		}
	}

	/** Breaking a watched housing returns the stored drone to the world. */
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}
		BlockPos pos = event.getPos();
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null || !blockEntity.hasData(ModRegistries.STUDENT_DRONE.get())) {
			return;
		}
		ItemStack drone = blockEntity.getData(ModRegistries.STUDENT_DRONE.get());
		blockEntity.removeData(ModRegistries.STUDENT_DRONE.get());
		Watched data = watched(level);
		data.positions.remove(pos);
		data.setDirty();
		if (!drone.isEmpty() && !event.getPlayer().isCreative()) {
			Block.popResource(level, pos, drone);
		}
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % CHECK_INTERVAL != 0) {
			return;
		}
		Watched data = watched(level);
		if (data.positions.isEmpty()) {
			return;
		}
		boolean changed = false;
		Iterator<Map.Entry<BlockPos, ItemStack>> it = data.positions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<BlockPos, ItemStack> entry = it.next();
			BlockPos pos = entry.getKey();
			if (!level.isLoaded(pos)) {
				continue;
			}
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!(blockEntity instanceof IBeeHousing housing) || !blockEntity.hasData(ModRegistries.STUDENT_DRONE.get())) {
				// The housing is gone (explosion, command...): give the drone back where it stood.
				if (!entry.getValue().isEmpty()) {
					Block.popResource(level, pos, entry.getValue().copy());
				}
				it.remove();
				changed = true;
				continue;
			}
			ItemStack queen = housing.getBeeInventory().getQueen();
			if (!queen.isEmpty() && IIndividualHandlerItem.getLifeStage(queen) == BeeLifeStage.QUEEN) {
				continue;
			}
			alert(level, pos);
		}
		if (changed) {
			data.setDirty();
		}
	}

	private static void alert(ServerLevel level, BlockPos pos) {
		level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.5f, 1.0f);
		level.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0.0);
		Component message = Component.translatable("message.ambitious_bees.student_alert", pos.getX(), pos.getY(), pos.getZ());
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= NOTIFY_RADIUS * NOTIFY_RADIUS) {
				player.displayClientMessage(message, true);
			}
		}
	}
}
