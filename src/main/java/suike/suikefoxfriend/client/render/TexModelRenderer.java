package suike.suikefoxfriend.client.render;

import java.util.function.Supplier;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class TexModelRenderer extends ModelRenderer {
    private final Supplier<Boolean> shouldRender;

    public TexModelRenderer(ModelBase model, int texOffX, int texOffY, Supplier<Boolean> shouldRender) {
        super(model, texOffX, texOffY);
        this.shouldRender = shouldRender;
    }

    @Override
    public void render(float scale) {
        GlStateManager.pushMatrix();
        if (shouldRender.get()) {
            GlStateManager.scale(1.4F, 1.4F, 1.4F);
        }
        super.render(scale);
        GlStateManager.popMatrix();
    }
}