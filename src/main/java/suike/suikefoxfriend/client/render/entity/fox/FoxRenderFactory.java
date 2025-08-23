package suike.suikefoxfriend.client.render.entity.fox;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.entity.EntityFlying;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;

import net.minecraftforge.fml.client.registry.IRenderFactory;

public class FoxRenderFactory implements IRenderFactory<FoxEntity> {
    @Override
    public Render<? super FoxEntity> createRenderFor(RenderManager manager) {
        return new FoxRender(manager);
    }
}