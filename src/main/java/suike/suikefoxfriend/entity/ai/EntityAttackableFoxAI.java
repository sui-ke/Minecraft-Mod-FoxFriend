package suike.suikefoxfriend.entity.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.util.EntitySelectors;

import com.google.common.base.Predicate;

public class EntityAttackableFoxAI extends EntityAINearestAttackableTarget<FoxEntity> {
    private boolean isWolf;

    public EntityAttackableFoxAI(EntityCreature entity) {
        super(
            entity, FoxEntity.class, 10, true, false,
            new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(EntityLivingBase target) {
                    return !((FoxEntity) target).isTamed();
                }
            }
        );
        this.isWolf = entity instanceof EntityWolf;
    }

    @Override
    public boolean shouldExecute() {
        return this.isWolf
            ? !((EntityWolf) this.taskOwner).isTamed() && super.shouldExecute()
            : super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase attackTarget = this.taskOwner.getAttackTarget();
        if (attackTarget instanceof FoxEntity && EntitySelectors.IS_ALIVE.apply(attackTarget) && ((FoxEntity) attackTarget).isTamed()) {
            return false;
        }

        return this.isWolf
            ? !((EntityWolf) this.taskOwner).isTamed() && super.shouldContinueExecuting()
            : super.shouldContinueExecuting();
    }

    public static void initEntityAI(EntityCreature entity) {
        if (entity instanceof EntityWolf) {
            entity.targetTasks.addTask(4, new EntityAttackableFoxAI(entity));
        } else {
            entity.targetTasks.addTask(1, new EntityAttackableFoxAI(entity));
        }
    }
}