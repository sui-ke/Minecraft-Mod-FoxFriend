package suike.suikefoxfriend.client.render.entity.fox;

import suike.suikefoxfriend.entity.fox.FoxEntity;
import suike.suikefoxfriend.entity.fox.FoxEntity.State;
import suike.suikefoxfriend.client.render.TexModelRenderer;

import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class FoxModel extends ModelBase {

    public FoxEntity fox;
    public float bodyOriginalY;
    public float headOriginalY;

    public final ModelRenderer root;

    public final ModelRenderer body;
    public final TexModelRenderer head;
    public final ModelRenderer nose;
    public final ModelRenderer rightEar;
    public final ModelRenderer leftEar;
    public final ModelRenderer rightHindLeg;
    public final ModelRenderer leftHindLeg;
    public final ModelRenderer rightFrontLeg;
    public final ModelRenderer leftFrontLeg;
    public final ModelRenderer tail;

    private final ItemRendererModel itemRenderer;

// 成年模型
    public FoxModel(int a) {
        this();
        this.root.setRotationPoint(0.0F, 0.0F, -0.5F);
        this.head.setRotationPoint(0.0F, 16.5F, -3.0F);

        this.setAnimationDefaultAngles(false);
        this.saveDefaultAngles();
    }

// 幼年模型
    public FoxModel(boolean a) {
        this();
        this.root.setRotationPoint(0.0F, 24.0F, -0.4F);
        this.head.setRotationPoint(0.0F, 9.4F, 0.5F);

        this.setAnimationDefaultAngles(true);
        this.saveDefaultAngles();
    }

// 模型
    private FoxModel() {
        this.textureWidth = 48;
        this.textureHeight = 32;

        // 创建根节点
        this.root = new ModelRenderer(this);

        // 创建身体部件并添加到根部件
        this.body = new ModelRenderer(this, 24, 15);
        this.body.addBox(-3.0F, 4.0F, -3.5F, 6, 11, 6);
        this.body.setRotationPoint(0.0F, 16.0F, -6.0F);
        this.body.rotateAngleX = (float) Math.PI / 2;
        this.root.addChild(this.body);

        // 创建腿部部件并添加到根部件
        this.rightFrontLeg = new ModelRenderer(this, 13, 24);
        this.rightFrontLeg.addBox(-4.0F, 0.5F, -1.0F, 2, 6, 2, 0.001F);
        this.rightFrontLeg.setRotationPoint(1.0F, 17.5F, 0.0F);
        this.root.addChild(this.rightFrontLeg);

        this.leftFrontLeg = new ModelRenderer(this, 4, 24);
        this.leftFrontLeg.addBox(2.0F, 0.5F, -1.0F, 2, 6, 2, 0.001F);
        this.leftFrontLeg.setRotationPoint(-1.0F, 17.5F, 0.0F);
        this.root.addChild(this.leftFrontLeg);

        this.rightHindLeg = new ModelRenderer(this, 13, 24);
        this.rightHindLeg.addBox(-4.0F, 0.5F, -1.0F, 2, 6, 2, 0.001F);
        this.rightHindLeg.setRotationPoint(1.0F, 17.5F, 7.0F);
        this.root.addChild(this.rightHindLeg);

        this.leftHindLeg = new ModelRenderer(this, 4, 24);
        this.leftHindLeg.addBox(2.0F, 0.5F, -1.0F, 2, 6, 2, 0.001F);
        this.leftHindLeg.setRotationPoint(-1.0F, 17.5F, 7.0F);
        this.root.addChild(this.leftHindLeg);

        // 创建尾巴并添加到身体
        this.tail = new ModelRenderer(this, 30, 0);
        this.tail.addBox(-2.0F, 0.0F, -1.0F, 4, 9, 5);
        this.tail.setRotationPoint(0.0F, 15.0F, -1.0F);
        this.body.addChild(this.tail);

        // 创建头部部件并添加到根部件
        this.head = new TexModelRenderer(this, 1, 5,
            () -> this.fox.isChild()
        );
        this.head.addBox(-4.0F, -2.0F, -5.0F, 8, 6, 6);
        this.root.addChild(this.head);

        // 创建耳朵并添加到头部
        this.rightEar = new ModelRenderer(this, 8, 1);
        this.rightEar.addBox(-4.0F, -4.0F, -4.0F, 2, 2, 1);
        this.head.addChild(this.rightEar);

        this.leftEar = new ModelRenderer(this, 15, 1);
        this.leftEar.addBox(2.0F, -4.0F, -4.0F, 2, 2, 1);
        this.head.addChild(this.leftEar);

        // 创建鼻子并添加到头部
        this.nose = new ModelRenderer(this, 6, 18);
        this.nose.addBox(-2.0F, 2.01F, -8.0F, 4, 2, 3);
        this.head.addChild(this.nose);

        // 物品
        this.itemRenderer = new ItemRendererModel(this);
        this.nose.addChild(this.itemRenderer);
    }

// 渲染
    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.fox = ((FoxEntity) entity);
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.itemRenderer.setItemStack(this.fox.getHandItem());

        GlStateManager.pushMatrix();
        if (this.fox.isChild()) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
        }
        this.root.render(scale);

        // this.renderDebugCube(this.tail, scale);
        // this.renderAllDebugCube(scale);

        GlStateManager.popMatrix();
    }

// 渲染测试轴
    private void renderAllDebugCube(float scale) {
        for (ModelRenderer child : this.root.childModels) {
            this.renderDebugCube(child, scale);
        }
    }
    private void renderDebugCube(ModelRenderer part, float scale) {
        GlStateManager.pushMatrix();

        // 应用部件的旋转点变换
        GlStateManager.translate(
            part.rotationPointX * scale,
            part.rotationPointY * scale,
            part.rotationPointZ * scale
        );

        // 禁用光照和纹理 (纯色立方体)
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();

        // 渲染白色中心立方体 (边长 0.2 格)
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.8F); // 白色半透明
        ModelRenderer debugCube = new ModelRenderer(this);
        debugCube.addBox(-0.1F, -0.1F, -0.1F, 1, 1, 1);
        debugCube.render(0.2F * scale);

        // 渲染坐标轴 (长度 0.5 格，宽度 0.05 格)
        int axisLength = 500;
        int axisWidth = 50;

        // X轴 (红色)
        GlStateManager.color(1.0F, 0.0F, 0.0F, 0.8F);
        ModelRenderer xAxis = new ModelRenderer(this);
        xAxis.addBox(0.0F, -axisWidth/2, -axisWidth/2, axisLength, axisWidth, axisWidth);
        xAxis.render(scale * 0.001F);
        // Y轴 (绿色)
        GlStateManager.color(0.0F, 1.0F, 0.0F, 0.8F);
        ModelRenderer yAxis = new ModelRenderer(this);
        yAxis.addBox(-axisWidth/2, 0.0F, -axisWidth/2, axisWidth, axisLength, axisWidth);
        yAxis.render(scale * 0.001F);
        // Z轴 (蓝色)
        GlStateManager.color(0.0F, 0.0F, 1.0F, 0.8F);
        ModelRenderer zAxis = new ModelRenderer(this);
        zAxis.addBox(-axisWidth/2, -axisWidth/2, 0.0F, axisWidth, axisWidth, axisLength);
        zAxis.render(scale * 0.001F);

        // 恢复渲染状态
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

// 动画
    @Override
    public final void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        this.setAnimationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        this.earAnimation(ageInTicks);
    }

    public void setAnimationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {}

    // 耳朵
    public void earAnimation(float ageInTicks) {
        float earWiggle = MathHelper.cos(ageInTicks * 0.1F);
        this.rightEar.rotateAngleZ = earWiggle * 0.05F - 0.05F;
        this.leftEar.rotateAngleZ = MathHelper.cos(ageInTicks * 0.1F + (float) Math.PI) * 0.05F + 0.05F;
    }

    private void saveDefaultAngles() {
        this.bodyOriginalY = this.body.rotationPointY;
        this.headOriginalY = this.head.rotationPointY;
    }

    public void setAnimationDefaultAngles() {}
    public void setAnimationDefaultAngles(boolean isChild) { this.setAnimationDefaultAngles(); }

// 渲染嘴部物品
    private static class ItemRendererModel extends ModelRenderer {
        private static final Minecraft MC = Minecraft.getMinecraft();
        private ItemStack itemStack = ItemStack.EMPTY;
        private FoxModel model;

        private ItemRendererModel(FoxModel model) {
            super(model);
            this.model = model;
        }

        private void setItemStack(ItemStack stack) {
            this.itemStack = stack;
        }

        @Override
        public void render(float scale) {
            if (itemStack.isEmpty()) return;

            Item item = itemStack.getItem();

            GlStateManager.pushMatrix();
            // 应用父级 (鼻子) 的变换
            this.applyParentTransformations(scale);

            // 更具类型调整 位置 和 缩放
            if (item instanceof ItemBlock) {
                GlStateManager.translate(0.0F, 0.26F, -0.305F);
                GlStateManager.rotate(90F, 1, 0, 0);
                GlStateManager.scale(0.26F, 0.26F, 0.26F);
            }
            else {
                if (item instanceof ItemTool || item instanceof ItemSword) {
                    GlStateManager.translate(0.0F, 0.26F, -0.5F);
                    GlStateManager.rotate(270F, 1, 0, 0); // 使物品横放
                } else {
                    GlStateManager.translate(0.0F, 0.26F, -0.405F);
                    GlStateManager.rotate(90F, 1, 0, 0); // 使物品横放
                }
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }

            MC.getRenderItem().renderItem(itemStack, TransformType.NONE);
            GlStateManager.popMatrix();
        }

        // 应用所有父级变换
        private void applyParentTransformations(float scale) {
            GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);

            if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
                GlStateManager.translate(
                    this.rotationPointX * scale,
                    this.rotationPointY * scale,
                    this.rotationPointZ * scale
                );
            }

            if (this.rotateAngleZ != 0.0F) {
                GlStateManager.rotate(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
            }
            if (this.rotateAngleY != 0.0F) {
                GlStateManager.rotate(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
            }
            if (this.rotateAngleX != 0.0F) {
                GlStateManager.rotate(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
            }

            if (this.model.nose != null) {
                this.model.nose.postRender(scale);
            }
        }
    }
}