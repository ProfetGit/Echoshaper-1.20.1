package net.profet.echoshaper.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class EchoshaperItem extends Item {
    public EchoshaperItem(Settings settings) {
        super(settings);
    }

    // Enumeration for Frequency Modes
    public enum FrequencyMode {
        SONIC_BOOM,
        SONIC_SMOOTH
    }

}
