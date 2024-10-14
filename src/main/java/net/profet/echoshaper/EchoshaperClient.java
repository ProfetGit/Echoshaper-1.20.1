package net.profet.echoshaper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.profet.echoshaper.client.render.EchoshaperProjectileRenderer;
import net.profet.echoshaper.registry.EchoshaperEntities;

public class EchoshaperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EchoshaperEntities.ECHOSHAPER_PROJECTILE, EchoshaperProjectileRenderer::new);
    }
}