package net.profet.echoshaper.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.profet.echoshaper.registry.EchoshaperEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EchoshaperProjectileEntity extends ProjectileEntity {
    private static final int MAX_RANGE = 32; // Maximum range of 32 blocks
    private static final float ENTITY_DAMAGE = 12.0f;
    private static final float BLOCK_DAMAGE = 8.0f;
    private static int BASE_HOLE_RADIUS = 5;
    private static final float RANDOMNESS_FACTOR = 0.3f;
    private Vec3d startPos;
    private final Random random = new Random();


    public EchoshaperProjectileEntity(EntityType<? extends EchoshaperProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public EchoshaperProjectileEntity(World world, LivingEntity owner) {
        this(EchoshaperEntities.ECHOSHAPER_PROJECTILE, world);
        this.setOwner(owner);
        this.setPosition(owner.getEyePos());
        this.setVelocity(owner, owner.getPitch(), owner.getYaw(), 0.0F, 2.0F, 1.0F);
        this.startPos = owner.getEyePos();
    }

    @Override
    protected void initDataTracker() {}

    @Override
    public void tick() {
        super.tick();

        if (!isRemoved()) {
            // Ensure startPos is not null before using it
            if (startPos != null && this.getPos().distanceTo(startPos) > MAX_RANGE) {
                destroyBlocks();
                discard();
            }

            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            if (hitResult.getType() != HitResult.Type.MISS) {
                onCollision(hitResult);
            }

            checkBlockCollision();
            updatePosition();
            spawnParticles();
        }
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
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
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
                destroyBlocks();
            }
            createHitEffect(hitResult.getPos());
            discard();
        }
    }

    private void damageEntity(Entity entity) {
        entity.damage(getDamageSources().thrown(this, getOwner()), ENTITY_DAMAGE);
    }

    private void destroyBlocks() {
        BlockPos center = this.getBlockPos();
        int maxRadius = (int)(BASE_HOLE_RADIUS * (1 + RANDOMNESS_FACTOR));
        List<BlockPos> blocksToDestroy = new ArrayList<>();

        for (int x = -maxRadius; x <= maxRadius; x++) {
            for (int y = -maxRadius; y <= maxRadius; y++) {
                for (int z = -maxRadius; z <= maxRadius; z++) {
                    double distance = Math.sqrt(x*x + y*y + z*z);
                    double randomizedRadius = BASE_HOLE_RADIUS * (1 + (random.nextDouble() - 0.5) * RANDOMNESS_FACTOR);

                    if (distance <= randomizedRadius) {
                        BlockPos pos = center.add(x, y, z);
                        if (canDestroyBlock(pos)) {
                            blocksToDestroy.add(pos);
                        }
                    }
                }
            }
        }

        for (BlockPos pos : blocksToDestroy) {
            // Destroy the block without sound or item drops
            this.getWorld().setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
        }

        // Damage nearby entities
        damageNearbyEntities(center);
    }


    private boolean canDestroyBlock(BlockPos pos) {
        BlockState state = this.getWorld().getBlockState(pos);
        return !state.isAir() && state.getBlock().getBlastResistance() < BLOCK_DAMAGE;
    }

    private void damageNearbyEntities(BlockPos center) {
        Box damageBox = new Box(center).expand(BASE_HOLE_RADIUS * 2.0); // Increase damage range when fully charged
        List<Entity> nearbyEntities = this.getWorld().getOtherEntities(this, damageBox);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                double distance = entity.squaredDistanceTo(this);
                if (distance < BASE_HOLE_RADIUS * BASE_HOLE_RADIUS * 3.0) { // Increase damage radius factor for more impact
                    damageEntity(entity);
                }
            }
        }
    }


    private void createHitEffect(Vec3d pos) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Sonic Boom particles
            for (int i = 0; i < 20; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 2;
                double offsetY = (random.nextDouble() - 0.5) * 2;
                double offsetZ = (random.nextDouble() - 0.5) * 2;
                serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        1, 0, 0, 0, 0);
            }

            // Sculk Pop particles
            for (int i = 0; i < 30; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 3;
                double offsetY = (random.nextDouble() - 0.5) * 3;
                double offsetZ = (random.nextDouble() - 0.5) * 3;
                serverWorld.spawnParticles(ParticleTypes.SCULK_CHARGE_POP,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        1, 0, 0.1, 0, 0.05);
            }

            // Shriek particles
            serverWorld.spawnParticles(ParticleTypes.SCULK_CHARGE_POP,
                    pos.x, pos.y, pos.z,
                    1, 0, 0, 0, 0);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putDouble("StartPosX", startPos.x);
        nbt.putDouble("StartPosY", startPos.y);
        nbt.putDouble("StartPosZ", startPos.z);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        double x = nbt.getDouble("StartPosX");
        double y = nbt.getDouble("StartPosY");
        double z = nbt.getDouble("StartPosZ");
        this.startPos = new Vec3d(x, y, z);
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