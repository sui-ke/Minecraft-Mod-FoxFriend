package suike.suikefoxfriend.mixin;

import suike.suikefoxfriend.api.IOwnable;

import net.minecraft.world.GameRules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if ((Object) this instanceof FoxEntity) {
            FoxEntity fox = (FoxEntity) (Object) this;
            if (!fox.getWorld().isClient && fox.getWorld().getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
                LivingEntity owner = ((IOwnable) this).getOwner();
                if (owner instanceof ServerPlayerEntity) {
                    owner.sendMessage(((LivingEntityAccessor) this).getDamageTracker().getDeathMessage());
                }
            }
        }
    }
}

@Mixin(LivingEntity.class)
interface LivingEntityAccessor {
    @Accessor("damageTracker")
    DamageTracker getDamageTracker();
}