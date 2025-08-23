package suike.suikefoxfriend.client.render.entity.fox.animation;

import suike.suikefoxfriend.client.render.entity.fox.FoxModel;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

// 空闲动画
public class Idling extends FoxModel {

    public Idling(int a) { super(a); }
    public Idling(boolean a) { super(a); }

// 设置动画模型默认角度
    @Override
    public void setAnimationDefaultAngles(boolean isChild) {
        // 尾巴旋转
        this.tail.rotateAngleX = -0.05235988F;
    }

// 动态部分
    @Override
    public void setAnimationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        // 基础呼吸: 速度 * 幅度
        float breath = MathHelper.cos(ageInTicks * 0.1F) * 0.08F;
        this.body.rotationPointY = this.bodyOriginalY + breath;
        this.head.rotationPointY = this.headOriginalY + (breath * 0.8F);

        if (limbSwingAmount > 0.01F) {
            float runBreath = MathHelper.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.15F;
            this.body.rotationPointY += runBreath;
        }

        // 腿部动画
        this.rightHindLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftHindLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.rightFrontLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        this.leftFrontLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        // 头部动画
        this.head.rotateAngleX = headPitch * 0.017453292F;
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;

        // 尾巴动画
        float tailSwing;
        if (limbSwingAmount > 0.01F) { // 移动状态
            tailSwing = MathHelper.cos(limbSwing * 0.6662F) * 0.2F * limbSwingAmount;
        } else { // 空闲状态
            tailSwing = MathHelper.cos(ageInTicks * 0.1F) * 0.05F;
        }
        this.tail.rotateAngleY = tailSwing;
        this.tail.rotateAngleZ = tailSwing;
    }
}