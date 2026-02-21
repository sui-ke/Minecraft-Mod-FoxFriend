package suike.suikefoxfriend.api;

import java.util.*;

import suike.suikefoxfriend.entity.ai.FoxFollowOwnerGoal;

import net.minecraft.item.ItemStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface IFoxTamed {
    FoxFollowOwnerGoal getFollowGoal();

    boolean hasOwnerGiveItem();
    void setOwnerGiveItem(boolean hasOwnerGiveItem);
    boolean handItemIsEmpty();
    ItemStack getHandItem();

    boolean isTamed();
    LivingEntity getOwner();
    void setOwner(PlayerEntity player);
    UUID getOwnerUuid();
    void setOwnerUuid(UUID uuid);
    boolean isOwner(PlayerEntity player);

    boolean isWaiting();
    void setWaiting(boolean waiting);
    boolean isSleepingWaiting();
    void setSleepingWaiting(boolean isSleeping);

    void mixinStopActions();
    void mixinSetSleeping(boolean sleeping);

    boolean mixinIsAggressive();
    void mixinSetAggressive(boolean aggressive);
}