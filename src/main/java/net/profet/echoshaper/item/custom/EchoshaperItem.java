package net.profet.echoshaper.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class EchoshaperItem extends Item {
    private static final int MAX_CHARGE_TIME = 60;  // Max time for charge (in ticks)
    private static final int MIN_CHARGE_TIME = 20;  // Minimum charge time for small effect

    public EchoshaperItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Play the charging sound when the player starts charging the item
        if (!world.isClient) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        // Start charging (returns a pass to let the charge continue)
        user.setCurrentHand(hand);
        return TypedActionResult.pass(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        int chargeTime = this.getMaxUseTime(stack) - remainingUseTicks;
        if (!world.isClient) {
            // Determine the charge level and adjust the projectile accordingly
            int radius = calculateHoleRadius(chargeTime);

            // Play the sonic boom sound when the projectile is released
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.0F, 1.0F);

            // Spawn the projectile with different sizes based on charge
            EchoshaperProjectileEntity projectile = new EchoshaperProjectileEntity(world, player);
            EchoshaperProjectileEntity.setBaseHoleRadius(radius);

            // Spawn the projectile in the world
            world.spawnEntity(projectile);
        }

        // Set cooldown (this could vary depending on charge time)
        player.getItemCooldownManager().set(this, 20);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return MAX_CHARGE_TIME;  // Maximum time the player can charge
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;  // Charging animation
    }

    private int calculateHoleRadius(int chargeTime) {
        if (chargeTime >= MAX_CHARGE_TIME) {
            return 15;  // Maximum radius for fully charged
        } else if (chargeTime >= MIN_CHARGE_TIME) {
            return 10;  // Mid-range hole size
        } else {
            return 5;  // Minimum hole size for short charge
        }
    }
}
