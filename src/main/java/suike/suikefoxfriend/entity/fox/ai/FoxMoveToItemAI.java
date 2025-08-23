package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;

public class FoxMoveToItemAI extends EntityAIBase {
    private final FoxEntity fox;
    private final double speed;
    private int delayCounter;

    public FoxMoveToItemAI(FoxEntity fox, double speed) {
        this.fox = fox;
        this.speed = speed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return this.fox.isValidTargetItem();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.fox.isValidTargetItem();
    }

    @Override
    public void startExecuting() {
        this.delayCounter = 0;
    }

    @Override
    public void updateTask() {
        EntityItem target = fox.targetItem;
        if (target == null) return;

        if (--this.delayCounter <= 0) {
            this.delayCounter = 10;
            this.fox.getNavigator().tryMoveToXYZ(
                target.posX,
                target.posY,
                target.posZ,
                speed
            );
        }
    }

    @Override
    public void resetTask() {
        this.fox.targetItem = null;
        this.fox.getNavigator().clearPath();
    }
}