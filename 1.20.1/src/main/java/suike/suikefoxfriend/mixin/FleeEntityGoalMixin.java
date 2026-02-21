package suike.suikefoxfriend.mixin;

import java.util.function.Predicate;

import suike.suikefoxfriend.api.*;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FleeEntityGoal.class)
public abstract class FleeEntityGoalMixin<T extends LivingEntity> implements IFleeEntityGoal {
    @Shadow
    private PathAwareEntity mob;

    @Shadow
    @Mutable
    private Class<T> classToFleeFrom;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void onCanStart(CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof FoxEntity fox) {
            if (((IFoxTamed) fox).isTamed()) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Override
    public boolean isPlayer() {
        return PlayerEntity.class.equals(classToFleeFrom);
    }
}