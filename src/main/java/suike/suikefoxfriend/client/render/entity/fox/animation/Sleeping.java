package suike.suikefoxfriend.client.render.entity.fox.animation;

import suike.suikefoxfriend.client.render.entity.fox.FoxModel;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

// 睡觉动画
public class Sleeping extends FoxModel {

    public Sleeping(int a) { super(a); }
    public Sleeping(boolean a) { super(a); }

// 设置动画模型默认角度
    @Override
    public void setAnimationDefaultAngles(boolean isChild) {
        // 身体侧躺
        this.body.rotateAngleZ = -(float)Math.PI / 2;

        if (isChild) { // 幼年
            // 头部
            this.head.rotationPointY = 12.5F;
            this.head.rotationPointZ = 1.6F;
            // 身体
            this.body.rotationPointZ = -1.3F;
            // 尾巴
            this.tail.rotationPointY = 15.0F;
            this.tail.rotationPointZ = -1.0F;
        }
        else {         // 成年
            // 头部
            this.head.rotationPointY = 19.5F;
            this.head.rotationPointZ = -2.5F;
            // 身体
            this.body.rotationPointZ = -5.5F;
            // 尾巴
            this.tail.rotationPointY = 14.5F;
            this.tail.rotationPointZ = -2.0F;
        }
        this.head.rotationPointX = 0.5F;

        this.body.rotationPointY = 21.0F;

        // 头部角度
        this.head.rotateAngleY = -(float) Math.PI / 1.5F;

        // 尾巴卷曲
        this.tail.rotateAngleX = isChild ? -2.1816616F : -2.6179938F;

        // 隐藏腿部
        this.rightHindLeg.showModel = false;
        this.leftHindLeg.showModel = false;
        this.rightFrontLeg.showModel = false;
        this.leftFrontLeg.showModel = false;

        // 左耳睡眠姿势
        this.rightEar.rotationPointY = -2.0F;
        this.rightEar.rotationPointX = -0.46F;
        this.rightEar.rotateAngleY = -0.15F;
        this.rightEar.rotateAngleZ = 0.05F;

        // 右耳睡眠姿势
        this.leftEar.rotationPointY = -2.0F;
        this.leftEar.rotationPointX = 0.46F;
        this.leftEar.rotateAngleY = 0.15F;
        this.leftEar.rotateAngleZ = -0.05F;
    }

// 动态部分
    @Override
    public void setAnimationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        // 基础呼吸: 速度 * 幅度
        float breath = MathHelper.cos(ageInTicks * 0.08F) * 0.06F;
        this.body.rotationPointY = this.bodyOriginalY + breath;

        // 头部动画
        this.head.rotateAngleZ = MathHelper.cos(ageInTicks * 0.027F) / 22.0F;
    }

    @Override
    public void earAnimation(float ageInTicks) {
        // 基础呼吸节奏
        float breath = MathHelper.cos(ageInTicks * 0.05F) * 0.03F;

        // 左耳睡眠动画
        this.rightEar.rotateAngleX = (float) Math.PI/4 + breath;

        // 右耳睡眠动画
        this.leftEar.rotateAngleX = (float) Math.PI/4 + breath;

        // 添加轻微颤动
        if (this.fox.ticksExisted >= this.fox.getNextTwitchTick()) {
            if (this.fox.ticksExisted - fox.getNextTwitchTick() < 5) {
                float twitch = MathHelper.sin(ageInTicks * 2.0F) * 0.02F;
                this.leftEar.rotateAngleX += twitch;
                this.rightEar.rotateAngleX += twitch;
            } else {
                this.fox.resetTwitchTimer();
            }
        }
    }
}