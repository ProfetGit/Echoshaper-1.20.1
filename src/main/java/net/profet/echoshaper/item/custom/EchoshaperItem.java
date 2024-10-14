package net.profet.echoshaper.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleTypes;
import net.profet.echoshaper.Echoshaper;

public class EchoshaperItem extends Item {
    public EchoshaperItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);


        try {
            // Play sound
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.0F, 1.0F);


            if (!world.isClient) {
                // Spawn the projectile entity
                EchoshaperProjectileEntity projectile = new EchoshaperProjectileEntity(world, user);
                // setPosition and setVelocity are already handled in the constructor
                world.spawnEntity(projectile);
            }

            // Set cooldown
            user.getItemCooldownManager().set(this, 20);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return TypedActionResult.success(itemStack);
    }
}