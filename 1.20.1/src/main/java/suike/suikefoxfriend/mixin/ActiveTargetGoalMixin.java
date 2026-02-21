package suike.suikefoxfriend.mixin;

import java.util.function.Predicate;

import suike.suikefoxfriend.api.IFoxTamed;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ActiveTargetGoal.class)
public abstract class ActiveTargetGoalMixin<T extends LivingEntity> {
    @ModifyArg(
        method = "findClosestTarget",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntitiesByClass(Ljava/lang/Class;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"
        ),
        index = 2
    )
    private Predicate<LivingEntity> modifyPredicate(Predicate<LivingEntity> original) {
        return entity -> {
            if (entity instanceof FoxEntity fox) {
                return !((IFoxTamed) fox).isTamed();
            }
            return original.test(entity);
        };
    }
}