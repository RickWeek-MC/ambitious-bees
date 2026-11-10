package com.rickweek.ambitiousbees.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * The bee shown flying out of the Bee-Bee Gun: it displays the queen that was fired (the real item, with all
 * its layers), flies straight at a constant speed for {@link #LIFETIME} ticks, leaves a light trail in the
 * colors of its species and ends with a small puff. It has no collision and does no damage.
 */
public class FlyingBeeEntity extends Entity implements ItemSupplier {
	public static final int LIFETIME = 10;
	private static final EntityDataAccessor<ItemStack> DATA_ITEM =
			SynchedEntityData.defineId(FlyingBeeEntity.class, EntityDataSerializers.ITEM_STACK);

	private int age;
	private int bodyColor = 0xF0B323;
	private int outlineColor = 0xF0B323;

	public FlyingBeeEntity(EntityType<? extends FlyingBeeEntity> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	/** Server side: sets the queen to display, the start point, the velocity (blocks per tick) and the trail colors. */
	public void launch(ItemStack bee, Vec3 start, Vec3 velocity, int body, int outline) {
		this.entityData.set(DATA_ITEM, bee.copyWithCount(1));
		this.setPos(start);
		this.setDeltaMovement(velocity);
		this.bodyColor = body;
		this.outlineColor = outline;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_ITEM, ItemStack.EMPTY);
	}

	@Override
	public ItemStack getItem() {
		return this.entityData.get(DATA_ITEM);
	}

	@Override
	public void tick() {
		super.tick();
		Vec3 motion = getDeltaMovement();
		setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
		age++;
		if (level() instanceof ServerLevel server) {
			int color = (age & 1) == 0 ? bodyColor : outlineColor;
			server.sendParticles(dust(color, 0.7F), getX(), getY(), getZ(), 2, 0.06, 0.06, 0.06, 0.0);
			if (age >= LIFETIME) {
				server.sendParticles(dust(bodyColor, 0.9F), getX(), getY(), getZ(), 8, 0.3, 0.3, 0.3, 0.0);
				server.sendParticles(dust(outlineColor, 0.9F), getX(), getY(), getZ(), 6, 0.3, 0.3, 0.3, 0.0);
				discard();
			}
		} else if (age > LIFETIME + 2) {
			discard();
		}
	}

	private static DustParticleOptions dust(int rgb, float scale) {
		return new DustParticleOptions(new Vector3f(((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F), scale);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {}

	@Override
	public boolean isNoGravity() {
		return true;
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}
}
