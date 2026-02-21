package suike.suikefoxfriend.entity.ai;

import java.util.*;

import suike.suikefoxfriend.api.IFoxTamed;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.math.Vec3d;

public class FoxWaitingGoal extends Goal {
    private final FoxEntity fox;
    private final IFoxTamed ifox;

    public FoxWaitingGoal(FoxEntity fox) {
        this.fox = fox;
        this.ifox = (IFoxTamed) fox;
        this.setControls(EnumSet.allOf(Goal.Control.class)); // 完全阻止其他AI
    }

    @Override
    public boolean canStart() {
        return this.ifox.isWaiting();
    }

    @Override
    public boolean shouldContinue() {
        return this.ifox.isWaiting();
    }

    @Override
    public void start() {
        this.ifox.mixinStopActions();
        this.fox.getNavigation().stop();
        this.fox.setUpwardSpeed(0);
        this.fox.setForwardSpeed(0);
        this.fox.setSidewaysSpeed(0);
        this.ifox.mixinSetAggressive(false);

        if (this.ifox.isSleepingWaiting()) {
            this.ifox.mixinSetSleeping(true);
        } else {
            this.fox.setSitting(true);
        }
    }

    @Override
    public void stop() {
        this.fox.setJumping(true);
        this.fox.setSitting(false);
        this.fox.setRollingHead(true);
        this.fox.setCanPickUpLoot(true);
        this.ifox.mixinSetSleeping(false);
    }

    @Override
    public void tick() {
        if (this.ifox.mixinIsAggressive())
            this.ifox.mixinSetAggressive(false);

        // 睡觉时
        if (this.ifox.isSleepingWaiting()) {
            this.fox.setRollingHead(false);   // 锁定视角
            this.fox.setCanPickUpLoot(false); // 不允许收集
            if (!this.fox.isSleeping()) {
                this.ifox.mixinSetSleeping(true); // 设为睡觉
            }
        }
        // 不睡觉时
        else {
            if (!this.fox.isSitting()) {
                this.fox.setSitting(true); // 坐下
            }

            // 看向主人
            LivingEntity owner = this.ifox.getOwner();
            if (owner != null && this.fox.squaredDistanceTo(owner) < 5.0D) {
                this.fox.getLookControl().lookAt(owner, 10.0F, (float) this.fox.getMaxLookPitchChange());
            }
        }
    }
}