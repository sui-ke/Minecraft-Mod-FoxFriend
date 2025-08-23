package suike.suikefoxfriend.client.render.entity.fox.animation;

import suike.suikefoxfriend.client.render.entity.fox.FoxModel;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

// 坐下动画
public class Sitting extends FoxModel {

    private float tailOriginalAngleZ;

    public Sitting(int a) { super(a); }
    public Sitting(boolean a) { super(a); }

// 设置动画模型默认角度
    @Override
    public void setAnimationDefaultAngles(boolean isChild) {
        // 头部位置调整
        if (isChild) {
            this.head.rotationPointY = 6.4F;
        } else {
            this.head.rotationPointY = 11.2F;
        }
        this.head.rotationPointZ = -0.25F;

        // 身体前倾
        this.body.rotateAngleX = 0.5235988F;
        this.body.rotationPointY = 10.2F;
        this.body.rotationPointZ = -3.0F;

        // 前腿脚尖聚拢
        this.rightFrontLeg.rotateAngleY = -0.1F; // 右前腿向内
        this.leftFrontLeg.rotateAngleY = 0.1F;   // 左前腿向内

        // 后腿脚尖分开
        this.rightHindLeg.rotateAngleY = 0.15F;  // 右后腿向外
        this.leftHindLeg.rotateAngleY = -0.15F; // 左后腿向外

        // 腿部弯曲
        this.rightHindLeg.rotateAngleX = -1.3089969F;
        this.rightFrontLeg.rotateAngleX = -0.2617994F;
        this.leftHindLeg.rotateAngleX = -1.3089969F;
        this.leftFrontLeg.rotateAngleX = -0.2617994F;
        // 右腿位置
        this.rightFrontLeg.rotationPointX = 1.2F;
        this.rightFrontLeg.rotationPointZ = -1.2F;
        this.rightHindLeg.rotationPointX = 1.2F;
        this.rightHindLeg.rotationPointY = 21.5F;
        this.rightHindLeg.rotationPointZ = 5.55F;
        // 左腿位置
        this.leftFrontLeg.rotationPointX = -1.2F;
        this.leftFrontLeg.rotationPointZ = -1.2F;
        this.leftHindLeg.rotationPointX = -1.2F;
        this.leftHindLeg.rotationPointY = 21.5F;
        this.leftHindLeg.rotationPointZ = 5.55F;

        // 尾巴位置
        this.tail.rotationPointY = 16.0F;
        this.tail.rotationPointZ = -0.2F;
        this.tail.rotateAngleX = (float) Math.PI / 3;
        this.tailOriginalAngleZ = this.tail.rotateAngleZ;
    }

// 动态部分
    @Override
    public void setAnimationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        // 基础呼吸: 速度 * 幅度
        float breath = MathHelper.cos(ageInTicks * 0.08F) * 0.08F;
        this.body.rotationPointY = this.bodyOriginalY + breath;
        this.head.rotationPointY = this.headOriginalY + (breath * 0.8F);

        // 尾巴动画
        this.tail.rotateAngleZ = this.tailOriginalAngleZ + (breath * 0.4F);

        // 头部动画
        this.head.rotateAngleX = headPitch * 0.017453292F;
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
    }
}