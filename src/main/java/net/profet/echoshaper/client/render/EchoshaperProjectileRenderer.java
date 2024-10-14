package net.profet.echoshaper.client.render;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.profet.echoshaper.Echoshaper;
import net.profet.echoshaper.item.custom.EchoshaperProjectileEntity;

public class EchoshaperProjectileRenderer extends EntityRenderer<EchoshaperProjectileEntity> {
    private static final Identifier TEXTURE = new Identifier(Echoshaper.MOD_ID, "textures/entity/echoshaper_projectile.png");

    public EchoshaperProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(EchoshaperProjectileEntity entity) {
        return TEXTURE;
    }
}