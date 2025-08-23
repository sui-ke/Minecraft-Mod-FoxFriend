package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.ai.EntityAIAvoidEntity;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class FoxAvoidEntityAI<T extends Entity> extends EntityAIAvoidEntity {
    private FoxEntity fox;

    public FoxAvoidEntityAI(FoxEntity fox, Class<T> classToAvoid) {
        super(
            fox, classToAvoid,
            EntityWolf.class.isAssignableFrom(classToAvoid) ? getWolfPredicate() : Predicates.alwaysTrue(),
            8.0F, 0.6D, 0.8D
        );
        this.fox = fox;
        this.setMutexBits(0);
    }

    @Override
    public boolean shouldExecute() {
        return !this.fox.isTamed() && super.shouldExecute();
    }

    @Override
    public void startExecuting() {
        this.fox.setIdling();
        super.startExecuting();
        this.fox.playSound("entity.fox.screech");
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.fox.isTamed() && super.shouldContinueExecuting();
    }

    private static Predicate getWolfPredicate() {
        return new Predicate<EntityLivingBase>()
        {
            @Override
            public boolean apply(EntityLivingBase target) {
                return !((EntityWolf) target).isTamed();
            }
        };
    }
}