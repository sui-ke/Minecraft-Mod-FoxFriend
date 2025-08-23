package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;

public class FoxSittingAI extends EntityAIBase {
    private final FoxEntity fox;
    private int delayCounter;
    private int sittingTime;
    private int sittingCool;

    public FoxSittingAI(FoxEntity fox) {
        this.fox = fox;
        this.setMutexBits(1);
    }

    // 是否开始执行
    @Override
    public boolean shouldExecute() {
        if (this.sittingCool > 0) {
            this.sittingCool--;
        }
        else if (this.canSitting()) {
            this.fox.setSitting();
            this.sittingTime = 80 + this.fox.getRandom().nextInt(160);
            return true;
        }

        return false;
    }

    private boolean canSitting() {
        if (this.fox.isSitting() || this.fox.getMoveHelper().action == EntityMoveHelper.Action.MOVE_TO) {
            return true;
        }

        return this.fox.isIdling()
            && !this.isInLiquid()
            && !this.fox.isAttacking()
            && this.fox.getRandom().nextFloat() < 0.01F;
    }

    // 是否继续执行
    @Override
    public boolean shouldContinueExecuting() {
        return this.fox.isSitting() || this.fox.getAttackTarget() != null;
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
    }

    @Override
    public void resetTask() {
        this.sittingCool = 200;
        this.fox.setIdling();
    }

    private boolean isInLiquid() {
        BlockPos pos = this.fox.getPos();
        Material material = this.fox.world.getBlockState(pos).getMaterial();
        return material.isLiquid();
    }
}