package com.rickweek.ambitiousbees.item;

import com.mojang.authlib.GameProfile;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.core.HumidityType;
import forestry.api.core.IErrorLogic;
import forestry.api.core.TemperatureType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * A throw-away "housing" used by the Bee-Bee Gun to fire bee effects. It has no bees of its own
 * and sits at the position of the box currently being hit. Products go to the shooter.
 * Beekeeping logic and error logic are not provided (the effects used by the gun never touch them).
 */
public final class GunHousing implements IBeeHousing {
	private final ServerLevel level;
	private final ServerPlayer shooter;
	private BlockPos pos;

	private final IBeeHousingInventory inventory = new IBeeHousingInventory() {
		@Override
		public ItemStack getQueen() {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack getDrone() {
			return ItemStack.EMPTY;
		}

		@Override
		public void setQueen(ItemStack stack) {}

		@Override
		public void setDrone(ItemStack stack) {}

		@Override
		public boolean addProduct(ItemStack product, boolean all) {
			shooter.getInventory().placeItemBackInInventory(product.copy());
			return true;
		}
	};

	public GunHousing(ServerLevel level, ServerPlayer shooter) {
		this.level = level;
		this.shooter = shooter;
		this.pos = shooter.blockPosition();
	}

	public ServerPlayer shooter() {
		return shooter;
	}

	public void setPos(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return List.of();
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return List.of();
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return inventory;
	}

	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return null;
	}

	@Override
	public int getBlockLightValue() {
		return level.getMaxLocalRawBrightness(pos);
	}

	@Override
	public boolean canBlockSeeTheSky() {
		return level.canSeeSky(pos);
	}

	@Override
	public boolean isRaining() {
		return level.isRainingAt(pos);
	}

	@Override
	public GameProfile getOwner() {
		return shooter.getGameProfile();
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		return Vec3.atCenterOf(pos);
	}

	@Override
	public IErrorLogic getErrorLogic() {
		return null;
	}

	@Override
	public TemperatureType temperature() {
		return TemperatureType.NORMAL;
	}

	@Override
	public HumidityType humidity() {
		return HumidityType.NORMAL;
	}

	@Override
	public Holder<Biome> getBiome() {
		return level.getBiome(pos);
	}

	@Override
	public BlockPos getBlockPos() {
		return pos;
	}

	@Override
	public Level getLevel() {
		return level;
	}
}
