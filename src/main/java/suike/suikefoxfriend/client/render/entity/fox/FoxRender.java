package suike.suikefoxfriend.client.render.entity.fox;

import java.util.Calendar;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelGhast;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;

public class FoxRender extends RenderLiving<FoxEntity> {

    public static final FoxModelBase foxModelBase = new FoxModelBase();
    public static final ThreadLocal<FoxRender> CURRENT_RENDER = new ThreadLocal<>();
    private static final ResourceLocation RED_FOX = new ResourceLocation(SuiKe.MODID, "textures/entity/fox/fox.png");
    private static final ResourceLocation RED_FOX_SLEEP = new ResourceLocation(SuiKe.MODID, "textures/entity/fox/fox_sleep.png");
    private static final ResourceLocation SNOW_FOX = new ResourceLocation(SuiKe.MODID, "textures/entity/fox/snow_fox.png");
    private static final ResourceLocation SNOW_FOX_SLEEP = new ResourceLocation(SuiKe.MODID, "textures/entity/fox/snow_fox_sleep.png");

    public FoxRender(RenderManager renderManager) {
        super(renderManager, foxModelBase, 0.2F);
    }

    @Override
    public ResourceLocation getEntityTexture(FoxEntity fox) {
        if (fox.isSleeping()) {
            return fox.isSnowType() ? SNOW_FOX_SLEEP : RED_FOX_SLEEP;
        }

        // 检查是否需要眨眼
        if (fox.ticksExisted >= fox.getNextBlinkTick()) {
            // 眨眼持续 4 ticks (0.2秒)
            if (fox.ticksExisted - fox.getNextBlinkTick() < 4) {
                return fox.isSnowType() ? SNOW_FOX_SLEEP : RED_FOX_SLEEP;
            } else {
                fox.resetBlinkTimer(); // 眨眼结束，重置计时器
            }
        }
        return fox.isSnowType() ? SNOW_FOX : RED_FOX;
    }
}