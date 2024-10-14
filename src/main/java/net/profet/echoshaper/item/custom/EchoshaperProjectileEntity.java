// Remove the smoothing mode component completely
package net.profet.echoshaper.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.profet.echoshaper.registry.EchoshaperEntities;

import java.util.*;

public class EchoshaperProjectileEntity extends ProjectileEntity {
    public enum Mode {
        HOLE
    }

    private static final int MAX_RANGE = 32;
    private static final float ENTITY_DAMAGE = 15.0f;
    private static final float BLOCK_DAMAGE = 8.0f;
    private static int BASE_HOLE_RADIUS = 5;
    private static final float RANDOMNESS_FACTOR = 0.3f;

    private Vec3d startPos;
    private final Random random = new Random();
    private Mode mode;

    public EchoshaperProjectileEntity(EntityType<? extends EchoshaperProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public EchoshaperProjectileEntity(World world, LivingEntity owner, Mode mode) {
        this(EchoshaperEntities.ECHOSHAPER_PROJECTILE, world);
        this.setOwner(owner);
        this.setPosition(owner.getEyePos());
        this.setVelocity(owner, owner.getPitch(), owner.getYaw(), 0.0F, 2.0F, 1.0F);
        this.startPos = owner.getEyePos();
        this.mode = mode;
    }

    @Override
    protected void initDataTracker() {
        // No data trackers needed
    }

    @Override
    public void tick() {
        super.tick();

        if (startPos != null && this.getPos().distanceTo(startPos) > MAX_RANGE) {
            handleCollisionEffect();
            discard();
            return;
        }

        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        if (hitResult.getType() != HitResult.Type.MISS) {
            onCollision(hitResult);
        }

        checkBlockCollision();
        updatePosition();
        spawnParticles();
    }

    private void updatePosition() {
        Vec3d velocity = this.getVelocity();
        setPosition(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
    }

    private void spawnParticles() {
        if (this.getWorld().isClient()) {
            for (int i = 0; i < 8; i++) {
                this.getWorld().addParticle(
                        ParticleTypes.SONIC_BOOM,
                        this.getX() + (random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0
                );
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient()) {
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                Entity hitEntity = ((EntityHitResult) hitResult).getEntity();
                damageEntity(hitEntity);
            } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                handleCollisionEffect();
            }
            createShockwaveEffect(hitResult.getPos());
            discard();
        }
    }

    private void handleCollisionEffect() {
        destroyBlocksWithEffects();
    }

    private void damageEntity(Entity entity) {
        entity.damage(getDamageSources().thrown(this, getOwner()), ENTITY_DAMAGE);
    }

    private void destroyBlocksWithEffects() {
        BlockPos center = this.getBlockPos();
        int maxRadius = (int) (BASE_HOLE_RADIUS * (1 + RANDOMNESS_FACTOR));

        Set<BlockPos> blocksToDestroy = new HashSet<>();

        for (int x = -maxRadius; x <= maxRadius; x++) {
            for (int y = -maxRadius; y <= maxRadius; y++) {
                for (int z = -maxRadius; z <= maxRadius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    double randomizedRadius = BASE_HOLE_RADIUS * (1 + (random.nextDouble() - 0.5) * RANDOMNESS_FACTOR);

                    if (distance <= randomizedRadius) {
                        BlockPos pos = center.add(x, y, z);
                        if (canDestroyBlock(pos)) {
                            blocksToDestroy.add(pos.toImmutable());
                        }
                    }
                }
            }
        }

        for (BlockPos pos : blocksToDestroy) {
            spawnBlockBreakingParticles(pos, this.getWorld().getBlockState(pos));
            destroyBlock(pos);
        }

        damageNearbyEntities(center);
    }

    private boolean canDestroyBlock(BlockPos pos) {
        BlockState state = this.getWorld().getBlockState(pos);
        return !state.isAir() && state.getBlock().getBlastResistance() < BLOCK_DAMAGE;
    }

    private void damageNearbyEntities(BlockPos center) {
        Box damageBox = new Box(center).expand(BASE_HOLE_RADIUS * 2.0);
        List<Entity> nearbyEntities = this.getWorld().getOtherEntities(this, damageBox);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                double distance = entity.squaredDistanceTo(this);
                if (distance < BASE_HOLE_RADIUS * BASE_HOLE_RADIUS * 3.0) {
                    damageEntity(entity);
                }
            }
        }
    }

    private void destroyBlock(BlockPos blockPos) {
        if (!this.getWorld().isClient() && this.getWorld() instanceof ServerWorld serverWorld) {
            createSculkDestructionEffect(blockPos);
            serverWorld.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    private void spawnBlockBreakingParticles(BlockPos blockPos, BlockState blockState) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            if (random.nextDouble() < 0.05) { // 5% chance to spawn particles
                serverWorld.spawnParticles(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState),
                        blockPos.getX() + 0.5,
                        blockPos.getY() + 0.5,
                        blockPos.getZ() + 0.5,
                        2,
                        0.5,
                        0.5,
                        0.5,
                        0.1
                );
            }
        }
    }

    private void createSculkDestructionEffect(BlockPos blockPos) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            if (random.nextDouble() < 0.05) { // 5% chance to spawn particles
                serverWorld.spawnParticles(
                        ParticleTypes.SCULK_CHARGE_POP,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        2,
                        0.5,
                        0.5,
                        0.5,
                        0.05
                );
                serverWorld.spawnParticles(
                        ParticleTypes.SONIC_BOOM,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        3,
                        1.0,
                        1.0,
                        1.0,
                        0.1
                );
            }
            serverWorld.playSound(null, blockPos, SoundEvents.BLOCK_SCULK_SENSOR_BREAK, SoundCategory.BLOCKS, 1.0F, 0.5F);
        }
    }

    private void createShockwaveEffect(Vec3d pos) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 360; i += 10) {
                double angle = Math.toRadians(i);
                double offsetX = Math.cos(angle) * (BASE_HOLE_RADIUS + random.nextDouble() - 0.5);
                double offsetZ = Math.sin(angle) * (BASE_HOLE_RADIUS + random.nextDouble() - 0.5);
                serverWorld.spawnParticles(
                        ParticleTypes.SONIC_BOOM,
                        pos.x + offsetX,
                        pos.y,
                        pos.z + offsetZ,
                        1,
                        0,
                        0,
                        0,
                        0.1
                );
            }
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putDouble("StartPosX", startPos.x);
        nbt.putDouble("StartPosY", startPos.y);
        nbt.putDouble("StartPosZ", startPos.z);
        nbt.putString("Mode", mode.name());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        double x = nbt.getDouble("StartPosX");
        double y = nbt.getDouble("StartPosY");
        double z = nbt.getDouble("StartPosZ");
        this.startPos = new Vec3d(x, y, z);
        this.mode = Mode.valueOf(nbt.getString("Mode"));
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public EntitySpawnS2CPacket createSpawnPacket() {
        Entity owner = getOwner();
        return new EntitySpawnS2CPacket(this, owner == null ? 0 : owner.getId());
    }

    public static void setBaseHoleRadius(int radius) {
        BASE_HOLE_RADIUS = radius;
    }
}