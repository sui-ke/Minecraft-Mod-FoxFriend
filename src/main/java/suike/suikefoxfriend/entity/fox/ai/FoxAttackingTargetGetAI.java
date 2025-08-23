package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;

public class FoxAttackingTargetGetAI extends EntityAIBase {
    private final FoxEntity fox;

    public FoxAttackingTargetGetAI(FoxEntity fox) {
        this.fox = fox;
    }

    @Override
    public boolean shouldExecute() {
        if (!this.fox.isTamed() || this.fox.isWaiting() || this.fox.isSleeping()) return false;

        // 必须手持剑或者斧子
        if (this.fox.canAttacking()) {
            this.fox.attackTarget = this.fox.getRevengeTarget();
            EntityLivingBase owner = this.fox.getOwner();
            if (this.fox.attackTarget == null && owner != null) {
                this.tryGetOwnerAttackingTarget(owner);
            }
            if (this.fox.attackTarget != null && EntitySelectors.IS_ALIVE.apply(this.fox.attackTarget)) {
                this.fox.setIdling();
            } else {
                this.fox.attackTarget = null;
            }
            return false;
        }

        this.fox.attackTarget = null;
        return false;
    }

    private void tryGetOwnerAttackingTarget(EntityLivingBase owner) {
        if (owner instanceof EntityPlayer) {
            // 优先攻击攻击主人的实体
            this.fox.attackTarget = owner.getRevengeTarget();
            if (this.fox.attackTarget == null) {
                // 否则攻击主人攻击的实体
                this.fox.attackTarget = ((EntityPlayer) owner).getLastAttackedEntity();
            }
        }
    }
}