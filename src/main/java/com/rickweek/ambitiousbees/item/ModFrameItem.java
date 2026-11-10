package com.rickweek.ambitiousbees.item;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.hives.IHiveFrame;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** A hive frame with its own modifier, optional per-cycle hook and optional immunity to wear. */
public class ModFrameItem extends Item implements IHiveFrame {
	/** Called every time the frame is used by a production cycle of a housing. */
	@FunctionalInterface
	public interface UseHook {
		void onUse(IBeeHousing housing);
	}

	private final IBeeModifier modifier;
	private final boolean unbreakable;
	private final UseHook hook;
	private final String tooltipKey;

	public ModFrameItem(Properties properties, IBeeModifier modifier, boolean unbreakable, UseHook hook, String tooltipKey) {
		super(properties);
		this.modifier = modifier;
		this.unbreakable = unbreakable;
		this.hook = hook;
		this.tooltipKey = tooltipKey;
	}

	@Override
	public ItemStack frameUsed(IBeeHousing housing, ItemStack frame, IBee queen, int wear) {
		if (hook != null) {
			hook.onUse(housing);
		}
		if (unbreakable) {
			return frame;
		}
		Level level = housing.getLevel();
		if (level instanceof ServerLevel serverLevel) {
			frame.hurtAndBreak(wear, serverLevel, null, item -> {
			});
		}
		return frame.isEmpty() ? ItemStack.EMPTY : frame;
	}

	@Override
	public IBeeModifier getBeeModifier(ItemStack frame) {
		return modifier;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable(tooltipKey));
		if (!unbreakable && stack.isDamaged()) {
			tooltip.add(Component.translatable("item.forestry.durability", stack.getMaxDamage() - stack.getDamageValue()));
		}
	}
}
