package net.profet.echoshaper.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.profet.echoshaper.Echoshaper;
import net.profet.echoshaper.item.custom.EchoshaperProjectileEntity;

public class EchoshaperEntities {
    public static final EntityType<EchoshaperProjectileEntity> ECHOSHAPER_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Echoshaper.MOD_ID, "echoshaper_projectile"),
            FabricEntityTypeBuilder.<EchoshaperProjectileEntity>create(SpawnGroup.MISC, EchoshaperProjectileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F)) // Adjust size as needed
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(20)
                    .build()
    );

    public static void registerEntities() {
        // No additional actions needed as registration occurs during static initialization
    }
}