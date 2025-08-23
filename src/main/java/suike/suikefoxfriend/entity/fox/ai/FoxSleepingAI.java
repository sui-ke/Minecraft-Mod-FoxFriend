package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.world.World;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;

public class FoxSleepingAI extends EntityAIBase {
    private final FoxEntity fox;
    private int delayCounter;
    private int sittingTime;
    private int sittingCool;
    private float sleepingYaw;

    public FoxSleepingAI(FoxEntity fox) {
        this.fox = fox;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.sittingCool > 0) {
            this.sittingCool--;
        }
        else if (this.canSleeping()) {
            this.fox.setSleeping();
            this.sittingTime = 180 + this.fox.getRandom().nextInt(240);
            this.sleepingYaw = this.fox.rotationYaw;
            return true;
        }

        return false;
    }

    private boolean canSleeping() {
        if (this.fox.isSleeping() || this.fox.getMoveHelper().action == EntityMoveHelper.Action.MOVE_TO) {
            return true;
        }

        // 基础条件检查
        if (!this.fox.isIdling() || 
            this.isInLiquid() || 
            this.fox.isAttacking() || 
            this.fox.getRandom().nextFloat() >= 0.01F) {
            return false;
        }

        // 天气检查
        if (this.fox.world.isThundering()) {
            return false;
        }

        return this.suitableLight();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.fox.isSleeping() && !this.fox.world.isThundering() && this.suitableLight();
    }

    @Override
    public void startExecuting() {
        this.delayCounter = 0;
        this.fox.stabilizePosition();
    }

    @Override
    public void updateTask() {
        if (++this.delayCounter >= this.sittingTime || this.isInLiquid()) {
            this.fox.setIdling();
            return;
        }

        this.fox.rotationYaw = this.sleepingYaw;
    }

    @Override
    public void resetTask() {
        this.sittingCool = 200;
        this.fox.setIdling();
    }

    private boolean isInLiquid() {
        Material material = this.fox.world.getBlockState(this.fox.getPos()).getMaterial();
        return material.isLiquid();
    }

    // 光照检查
    private boolean suitableLight() {
        return !this.fox.world.canSeeSky(this.fox.getPos().up());
    }
}