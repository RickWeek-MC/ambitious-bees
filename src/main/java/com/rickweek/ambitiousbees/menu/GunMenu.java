package com.rickweek.ambitiousbees.menu;

import com.rickweek.ambitiousbees.item.BeeBeeGunItem;
import com.rickweek.ambitiousbees.registry.ModRegistries;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Loading screen of the Bee-Bee Gun held in the main hand: a feed slot (honey items are poured into the
 * tank), and six hexagonal slots for queens. Which queen fires is chosen with {@link #clickMenuButton}.
 */
public class GunMenu extends AbstractContainerMenu {
	private static final int SIZE = BeeBeeGunItem.SLOTS;
	/** Top-left corner of the six bee slots, clockwise from the top (the hexagons form a ring). */
	private static final int[][] BEE_POSITIONS = {{80, 11}, {101, 23}, {101, 47}, {80, 59}, {59, 47}, {59, 23}};

	private final SimpleContainer gunContainer = new SimpleContainer(SIZE);
	private final DataSlot honey = DataSlot.standalone();
	private final DataSlot selected = DataSlot.standalone();
	private final Player player;
	private boolean busy;

	public GunMenu(int containerId, Inventory inventory) {
		super(ModRegistries.GUN_MENU.get(), containerId);
		this.player = inventory.player;
		boolean server = !player.level().isClientSide;
		if (server) {
			ItemStack gun = player.getMainHandItem();
			if (gun.getItem() instanceof BeeBeeGunItem) {
				NonNullList<ItemStack> contents = BeeBeeGunItem.readContents(gun);
				for (int i = 0; i < SIZE; i++) {
					gunContainer.setItem(i, contents.get(i));
				}
				honey.set(BeeBeeGunItem.getHoney(gun));
				selected.set(BeeBeeGunItem.getSelected(gun));
			}
		}
		addDataSlot(honey);
		addDataSlot(selected);

		addSlot(new Slot(gunContainer, BeeBeeGunItem.FEED_SLOT, 20, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return BeeBeeGunItem.honeyValue(stack) > 0;
			}
		});
		for (int i = 0; i < BeeBeeGunItem.BEE_SLOTS; i++) {
			addSlot(new Slot(gunContainer, BeeBeeGunItem.FIRST_BEE_SLOT + i, BEE_POSITIONS[i][0], BEE_POSITIONS[i][1]) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return BeeBeeGunItem.isLoadableQueen(stack);
				}

				@Override
				public int getMaxStackSize() {
					return 1;
				}
			});
		}
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addPlayerSlot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18);
			}
		}
		for (int col = 0; col < 9; col++) {
			addPlayerSlot(inventory, col, 8 + col * 18, 142);
		}

		if (server) {
			absorbHoney();
			gunContainer.addListener(this::onChanged);
		}
	}

	/** Player slots; the gun itself cannot be picked up while the screen is open. */
	private void addPlayerSlot(Inventory inventory, int index, int x, int y) {
		addSlot(new Slot(inventory, index, x, y) {
			@Override
			public boolean mayPickup(Player p) {
				return !(getItem().getItem() instanceof BeeBeeGunItem);
			}
		});
	}

	public int getHoney() {
		return honey.get();
	}

	public int getSelected() {
		return selected.get();
	}

	/** The queen in the selected slot (empty if none). */
	public ItemStack getSelectedBee() {
		return gunContainer.getItem(BeeBeeGunItem.FIRST_BEE_SLOT + getSelected());
	}

	/** 0..5 if the slot is one of the six bee slots, otherwise -1. */
	public int beeIndex(Slot slot) {
		if (slot.container != gunContainer) {
			return -1;
		}
		int index = slot.getContainerSlot() - BeeBeeGunItem.FIRST_BEE_SLOT;
		return index >= 0 && index < BeeBeeGunItem.BEE_SLOTS ? index : -1;
	}

	/** Button id 0..5 selects that bee slot as the one that fires. */
	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id < 0 || id >= BeeBeeGunItem.BEE_SLOTS) {
			return false;
		}
		selected.set(id);
		persist();
		return true;
	}

	/** Pours the items of the feed slot into the tank while they fit. */
	private void absorbHoney() {
		ItemStack in = gunContainer.getItem(BeeBeeGunItem.FEED_SLOT);
		while (!in.isEmpty()) {
			int value = BeeBeeGunItem.honeyValue(in);
			if (value <= 0 || honey.get() + value > BeeBeeGunItem.CAPACITY) {
				break;
			}
			ItemStack remainder = BeeBeeGunItem.honeyRemainder(in);
			honey.set(honey.get() + value);
			in.shrink(1);
			if (!remainder.isEmpty()) {
				player.getInventory().placeItemBackInInventory(remainder);
			}
		}
		if (in.isEmpty()) {
			gunContainer.setItem(BeeBeeGunItem.FEED_SLOT, ItemStack.EMPTY);
		}
	}

	private void onChanged(Container container) {
		if (busy) {
			return;
		}
		busy = true;
		try {
			absorbHoney();
			persist();
		} finally {
			busy = false;
		}
	}

	private void persist() {
		ItemStack gun = player.getMainHandItem();
		if (!(gun.getItem() instanceof BeeBeeGunItem)) {
			return;
		}
		List<ItemStack> contents = new ArrayList<>(SIZE);
		for (int i = 0; i < SIZE; i++) {
			contents.add(gunContainer.getItem(i).copy());
		}
		BeeBeeGunItem.writeContents(gun, contents);
		BeeBeeGunItem.setHoney(gun, honey.get());
		BeeBeeGunItem.setSelected(gun, selected.get());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		if (index < SIZE) {
			if (!moveItemStackTo(stack, SIZE, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, 0, SIZE, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return original;
	}

	@Override
	public boolean stillValid(Player player) {
		return player.getMainHandItem().getItem() instanceof BeeBeeGunItem;
	}
}
