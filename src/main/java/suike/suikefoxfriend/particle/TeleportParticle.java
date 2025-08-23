package suike.suikefoxfriend.particle;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSmokeNormal;

public class TeleportParticle extends ParticleSmokeNormal {
    private static final int COLOR = 0x11B9e;
    private static final float SCALE = 1.2F;     // 粒子大小
    private static final float GRAVITY = -0.0008F; 

    private final int circleDelay; // 圈数延迟

    private TeleportParticle(World world, double x, double y, double z, int circle) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D, 1.0F);
        this.circleDelay = circle * 6;

        // 设置粒子属性
        this.particleMaxAge = 50 + this.circleDelay;
        this.particleScale = SCALE;

        // 设置颜色
        this.particleRed = ((COLOR >> 16) & 0xFF) / 255.0F;
        this.particleGreen = ((COLOR >> 8) & 0xFF) / 255.0F;
        this.particleBlue = (COLOR & 0xFF) / 255.0F;

        // 粒子不透明
        this.particleAlpha = 0.0F;

        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;

        this.particleScale = 0.02F;
        this.setParticleTextureIndex(this.rand.nextInt(2) + 4);
    }

// 粒子更新
    @Override
    public void onUpdate() {
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
            return;
        }

        // 延迟逻辑
        if (particleAge < circleDelay) {
            // 延迟期间 - 淡入效果
            this.particleAlpha = (float) particleAge / circleDelay;
            return;
        }

        this.particleAlpha = 1.0F; // 完全可见

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.motionY += GRAVITY;

        this.posY += this.motionY;

        // 粒子淡出效果
        if (this.particleAge > this.particleMaxAge * 0.7F) {
            this.particleAlpha = 1.0F - (float)(this.particleAge - this.particleMaxAge * 0.7F) / (float)(this.particleMaxAge * 0.3F);
        }
    }

// 生成粒子效果方法
    private static final Minecraft MC = Minecraft.getMinecraft();
    public static void spawnParticle(World world, double x, double y, double z, int circle) {
        MC.effectRenderer.addEffect(
            new TeleportParticle(world, x, y, z, circle)
        );
    }
}