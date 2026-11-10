package com.rickweek.ambitiousbees.item;

import com.rickweek.ambitiousbees.AmbitiousBees;
import com.rickweek.ambitiousbees.entity.FlyingBeeEntity;
import com.rickweek.ambitiousbees.effect.AreaBeeEffect;
import com.rickweek.ambitiousbees.menu.GunMenu;
import com.rickweek.ambitiousbees.registry.ModRegistries;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeEffect;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.IIndividual;
import forestry.api.core.genetics.alleles.BeeChromosomes;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Fires the effect of the SELECTED queen in a cone in front of the player (about 8 blocks).
 * Right click shoots (also when aiming at a block with a GUI, which is not opened);
 * shift + right click opens the loading screen: a feed slot that turns honey items into the gun's
 * honey tank, and six hexagonal slots for queens of bees whose effect can be fired on demand
 * (right click a bee in the screen to select it). A shot costs {@link #SHOT_COST} honey.
 */
public class BeeBeeGunItem extends Item {
	public static final int BEE_SLOTS = 6;
	public static final int SLOTS = BEE_SLOTS + 1;
	public static final int FEED_SLOT = 0;
	public static final int FIRST_BEE_SLOT = 1;
	public static final int CAPACITY = 4000;
	public static final int SHOT_COST = 100;
	private static final double RANGE = 8.0;
	private static final int COOLDOWN = 10;
	/** The cone is a chain of growing boxes: {distance of the center, half size}. */
	private static final double[][] SEGMENTS = {{1.5, 1.0}, {3.5, 1.8}, {5.5, 2.6}};

	public BeeBeeGunItem(Properties properties) {
		super(properties);
	}

	public static NonNullList<ItemStack> readContents(ItemStack gun) {
		NonNullList<ItemStack> list = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		gun.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(list);
		return list;
	}

	public static void writeContents(ItemStack gun, List<ItemStack> contents) {
		gun.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
	}

	public static int getHoney(ItemStack gun) {
		return gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Honey");
	}

	public static void setHoney(ItemStack gun, int honey) {
		CustomData.update(DataComponents.CUSTOM_DATA, gun, tag -> tag.putInt("Honey", Math.max(0, Math.min(CAPACITY, honey))));
	}

	public static int getSelected(ItemStack gun) {
		int selected = gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Selected");
		return Math.max(0, Math.min(BEE_SLOTS - 1, selected));
	}

	public static void setSelected(ItemStack gun, int selected) {
		CustomData.update(DataComponents.CUSTOM_DATA, gun, tag -> tag.putInt("Selected", selected));
	}

	/** How much honey one item adds to the tank (0 if it is not a honey source). */
	public static int honeyValue(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}
		if (stack.is(ModRegistries.HONEY_COMB_INGOT.get())) {
			return 1000;
		}
		if (stack.is(Items.HONEY_BOTTLE)) {
			return 250;
		}
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		if ("forestry".equals(id.getNamespace())) {
			return switch (id.getPath()) {
				case "honey_drop" -> 100;
				case "bucket_honey" -> 1000;
				default -> 0;
			};
		}
		return 0;
	}

	/** What is left of a honey item once its honey has been poured into the tank. */
	public static ItemStack honeyRemainder(ItemStack stack) {
		if (stack.is(Items.HONEY_BOTTLE)) {
			return new ItemStack(Items.GLASS_BOTTLE);
		}
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		if ("forestry".equals(id.getNamespace()) && "bucket_honey".equals(id.getPath())) {
			return new ItemStack(Items.BUCKET);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.ambitious_bees.gun_honey", getHoney(stack), CAPACITY));
		tooltip.add(Component.translatable("tooltip.ambitious_bees.gun_help"));
	}

	/** The (active) effect of a bee item, or null. */
	private static IBeeEffect effectOf(ItemStack stack) {
		if (!IIndividualHandlerItem.hasIndividual(stack)) {
			return null;
		}
		IGenome genome = IIndividualHandlerItem.getGenome(stack);
		return genome.resolveActive(BeeChromosomes.EFFECT);
	}

	/** True for a queen whose effect can be fired by the gun. */
	public static boolean isLoadableQueen(ItemStack stack) {
		return !stack.isEmpty() && IIndividualHandlerItem.hasIndividual(stack)
				&& IIndividualHandlerItem.getLifeStage(stack) == BeeLifeStage.QUEEN
				&& effectOf(stack) instanceof AreaBeeEffect;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResultHolder.pass(stack);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			trigger(serverPlayer, stack);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || context.getHand() != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			trigger(serverPlayer, context.getItemInHand());
		}
		return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
	}

	private void trigger(ServerPlayer player, ItemStack gun) {
		if (player.isShiftKeyDown()) {
			player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new GunMenu(id, inventory),
					Component.translatable("container.ambitious_bees.bee_bee_gun")));
		} else {
			fire(player, gun);
		}
	}

	private void fire(ServerPlayer player, ItemStack gun) {
		if (player.getCooldowns().isOnCooldown(this) || !(player.level() instanceof ServerLevel level)) {
			return;
		}
		NonNullList<ItemStack> contents = readContents(gun);
		// Only the selected bee fires; if its slot is empty, fall back to the first loaded bee.
		ItemStack queen = contents.get(FIRST_BEE_SLOT + getSelected(gun));
		if (!isLoadableQueen(queen)) {
			queen = ItemStack.EMPTY;
			for (int i = FIRST_BEE_SLOT; i < SLOTS; i++) {
				if (isLoadableQueen(contents.get(i))) {
					queen = contents.get(i);
					break;
				}
			}
		}
		if (queen.isEmpty()) {
			player.displayClientMessage(Component.translatable("message.ambitious_bees.gun_no_bees"), true);
			return;
		}
		int honey = getHoney(gun);
		if (honey < SHOT_COST) {
			player.displayClientMessage(Component.translatable("message.ambitious_bees.gun_no_honey"), true);
			level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 1.0f);
			return;
		}
		setHoney(gun, honey - SHOT_COST);
		player.displayClientMessage(Component.translatable("message.ambitious_bees.gun_honey_left",
				honey - SHOT_COST, CAPACITY), true);
		player.getCooldowns().addCooldown(this, COOLDOWN);

		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		HitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(RANGE)),
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		// Stop the cone just behind the first block it meets (that block is still affected).
		double limit = hit.getType() == HitResult.Type.MISS ? RANGE : eye.distanceTo(hit.getLocation()) + 1.5;

		GunHousing housing = new GunHousing(level, player);
		for (int s = 0; s < SEGMENTS.length; s++) {
			double distance = SEGMENTS[s][0];
			if (s > 0 && distance > limit) {
				break;
			}
			double size = SEGMENTS[s][1] * 2;
			Vec3 center = eye.add(look.scale(distance));
			AABB box = AABB.ofSize(center, size, size, size);
			housing.setPos(BlockPos.containing(center));
			IGenome genome = IIndividualHandlerItem.getGenome(queen);
			IBeeEffect effect = genome.resolveActive(BeeChromosomes.EFFECT);
			if (effect instanceof AreaBeeEffect area) {
				try {
					area.fire(level, genome, housing, box);
				} catch (RuntimeException e) {
					AmbitiousBees.LOGGER.error("Bee-Bee Gun: effect failed", e);
				}
			}
		}

		launchBee(level, queen, eye.add(look.scale(0.6)).add(0, -0.3, 0), look, Math.min(limit, RANGE));
		level.playSound(null, player.blockPosition(), SoundEvents.BEEHIVE_EXIT, SoundSource.PLAYERS, 1.0f, 0.8f);
	}

	/** Body and outline color of the queen's species (they tint the trail of the flying bee). */
	private static int[] beeColors(ItemStack queen) {
		IIndividual individual = IIndividualHandlerItem.getIndividual(queen);
		if (individual instanceof IBee bee) {
			return new int[] {bee.getSpecies().getBody(), bee.getSpecies().getOutline()};
		}
		return new int[] {0xF0B323, 0xF0B323};
	}

	/** Launches the queen that was fired: it flies straight to the end of the cone in {@code LIFETIME} ticks. */
	private static void launchBee(ServerLevel level, ItemStack queen, Vec3 muzzle, Vec3 look, double reach) {
		int[] colors = beeColors(queen);
		FlyingBeeEntity bee = new FlyingBeeEntity(ModRegistries.FLYING_BEE.get(), level);
		bee.launch(queen, muzzle, look.scale(reach / FlyingBeeEntity.LIFETIME), colors[0], colors[1]);
		level.addFreshEntity(bee);
	}
}
