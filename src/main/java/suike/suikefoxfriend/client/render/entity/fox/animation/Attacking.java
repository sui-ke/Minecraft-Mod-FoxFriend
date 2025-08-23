/*package suike.suikefoxfriend.client.render.entity.fox.animation;

import suike.suikefoxfriend.entity.fox.FoxEntity;
import suike.suikefoxfriend.client.render.entity.fox.FoxModel;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class Attacking extends FoxModel {

    public Attacking() { super(0); }

// 设置动画模型默认角度
    @Override
    public void setAnimationDefaultAngles() {
        // 身体旋转
        this.body.rotateAngleX = (float) Math.PI / 2;
    }

// 动态部分
    @Override
    public void setAnimationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        FoxEntity fox = (FoxEntity) entity;
        int schedule = fox.getJumpAttackSchedule();
        float progress = MathHelper.clamp(schedule / 12f, 0f, 1f); // 总时长约0.6秒

        // 抛物线运动曲线
        float parabola = 4 * progress * (1 - progress);

        // 身体朝向移动方向
        float motionYaw = (float)Math.toDegrees(Math.atan2(fox.motionZ, fox.motionX)) - 90;
        this.head.rotateAngleY = motionYaw * 0.017453292F;

        // 身体随抛物线起伏
        this.body.rotationPointY = 16 - parabola * 4;
        this.body.rotateAngleX = (float)Math.PI/2 + parabola * 0.3f;

        // 腿部动画
        float legSwing = MathHelper.sin(progress * (float)Math.PI * 2) * 0.6f;
        this.rightFrontLeg.rotateAngleX = -0.5f - legSwing;
        this.leftFrontLeg.rotateAngleX = -0.5f - legSwing;
        this.rightHindLeg.rotateAngleX = 0.8f + legSwing;
        this.leftHindLeg.rotateAngleX = 0.8f + legSwing;

        // 尾巴平衡
        this.tail.rotateAngleX = -0.05235988F + parabola * 0.2f;

        // 头部跟随实际视角
        this.head.rotateAngleX = headPitch * 0.017453292F;
        this.head.rotateAngleY += netHeadYaw * 0.017453292F;
    }
}*/