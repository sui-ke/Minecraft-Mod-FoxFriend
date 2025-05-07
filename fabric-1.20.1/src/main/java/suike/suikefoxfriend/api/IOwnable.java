package suike.suikefoxfriend.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface IOwnable {
    void playerTamedFox(PlayerEntity player);

    boolean isTamed();

    boolean isWaiting();

    void playerSetWaiting(PlayerEntity player);

    void setWaiting(boolean waiting);

    boolean getIsSleeping();

    LivingEntity getOwner();

    void mixinStopActions();

    void mixinSetSleeping(boolean sleeping);

    void mixinSetAggressive(boolean aggressive);
}