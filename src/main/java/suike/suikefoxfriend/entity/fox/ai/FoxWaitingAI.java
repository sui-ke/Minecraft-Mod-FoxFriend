package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;

public class FoxWaitingAI extends EntityAIBase {
    private final FoxEntity fox;

    public FoxWaitingAI(FoxEntity fox) {
        this.fox = fox;
        this.setMutexBits(1);
    }

    // 是否开始执行
    @Override
    public boolean shouldExecute() {
        if (this.fox.isWaiting()) {
            return true;
        }
        return false;
    }

    // 是否继续执行
    @Override
    public boolean shouldContinueExecuting() {
        return this.fox.isWaiting();
    }

    @Override
    public void startExecuting() {
        if (this.fox.isSleepingWaiting()) {
            this.fox.setSleeping();
        } else {
            this.fox.setSitting();
        }
        this.fox.stabilizePosition();
    }

    @Override
    public void updateTask() {
        if (this.isInLiquid()) {
            this.fox.setWaiting(false);
            return;
        }
        if (this.fox.ticksExisted % 20 == 0) {
            if (this.fox.isSleepingWaiting()) {
                this.fox.setSleeping();
            } else {
                this.fox.setSitting();
            }
        }
    }

    private boolean isInLiquid() {
        BlockPos pos = this.fox.getPos();
        Material material = this.fox.world.getBlockState(pos).getMaterial();
        return material.isLiquid();
    }

    @Override public void resetTask() {}
}