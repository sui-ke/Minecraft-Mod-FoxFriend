package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.entity.ai.EntityAITarget;

public class FoxAttackingTargetAI extends EntityAITarget {
    private final FoxEntity fox;

    public FoxAttackingTargetAI(FoxEntity fox) {
        super(fox, false);
        this.fox = fox;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.fox.isTamed() || !this.fox.isIdling()) return false;
        return this.isSuitableTarget(this.fox.attackTarget, true)
            && this.fox.shouldAttackEntity(this.fox.attackTarget, this.fox.getOwner());
    }

    @Override
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.fox.attackTarget);
        super.startExecuting();
    }

    @Override
    public void resetTask() {
        this.fox.attackTarget = null;
    }
}