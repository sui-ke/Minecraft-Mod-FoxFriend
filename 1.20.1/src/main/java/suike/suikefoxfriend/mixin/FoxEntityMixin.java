package suike.suikefoxfriend.mixin;

import java.util.*;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.api.*;
import suike.suikefoxfriend.entity.ai.FoxWaitingGoal;
import suike.suikefoxfriend.entity.ai.FoxFollowOwnerGoal;
import suike.suikefoxfriend.crosshelper.CrossHelper;

import net.minecraft.item.*;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.fluid.FluidState;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoxEntity.class)
public abstract class FoxEntityMixin extends AnimalEntity implements IFoxTamed {//, Tameable {

    private FoxEntityMixin() { super(null, null); }

    private UUID OWNER_UUID = null;         // 主人 UUID
    private boolean FOX_WAITING = false;    // 等待
    private boolean HAS_OWNER_GIVE = false; // 手持玩家给予

    @Shadow @Mutable private int eatingTime;
    @Shadow abstract boolean canEat(ItemStack a);
    @Shadow abstract void stopActions();
    @Shadow abstract void setSleeping(boolean sleeping);
    @Shadow abstract boolean isAggressive();
    @Shadow abstract void setAggressive(boolean aggressive);
    @Override public void mixinStopActions() { this.stopActions(); }
    @Override public void mixinSetSleeping(boolean sleeping) { this.setSleeping(sleeping); }
    @Override public boolean mixinIsAggressive() { return this.isAggressive(); }
    @Override public void mixinSetAggressive(boolean aggressive) { this.setAggressive(aggressive); }

    // 冷却
    private int foxClickCooldownTicks = 0;
    private boolean tryInteractCooldown() {
        if (this.foxClickCooldownTicks == 0 ) {
            this.foxClickCooldownTicks = 3;
            return true;
        }
        return false;
    }
    private int teleportCooldownTicks = 0;
    private boolean tryTeleportCooldown() {
        if (this.teleportCooldownTicks == 0) {
            this.teleportCooldownTicks = 20;
            return true;
        }
        return false;
    }

    // 跟随主人AI
    private FoxFollowOwnerGoal foxFollowOwnerGoal;
    @Override
    public FoxFollowOwnerGoal getFollowGoal() {
        if (this.foxFollowOwnerGoal == null) {
            this.foxFollowOwnerGoal = new FoxFollowOwnerGoal(this.getFox());
        }
        return this.foxFollowOwnerGoal;
    }

    // 等待时睡觉
    private boolean isSleeping = false;
    @Override
    public boolean isSleepingWaiting() {
        return this.isSleeping;
    }
    @Override
    public void setSleepingWaiting(boolean isSleeping) {
        this.isSleeping = isSleeping;
    }

    // 初始化AI
    @Inject(method = "initGoals", at = @At("RETURN"))
    private void onInitGoals(CallbackInfo ci) {
        ((MobEntityAccessor) this).getGoalSelector().add(5, this.getFollowGoal());
        ((MobEntityAccessor) this).getGoalSelector().add(-1, new FoxWaitingGoal(this.getFox()));
    }

    @Redirect(
        method = "initGoals",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V"
        )
    )
    private void preventFleePlayerGoal(GoalSelector goalSelector, int priority, Goal goal) {
        if (goal instanceof FleeEntityGoal fleeGoal) {
            if (((IFleeEntityGoal) fleeGoal).isPlayer()) {
                return;
            }
        }
        goalSelector.add(priority, goal);
    }

// 交互方法
    @Unique // 无 interactMob 方法, 无法 Inject, 直接添加
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ActionResult action = player.isSneaking() ? ActionResult.PASS : super.interactMob(player, hand);
        if (!action.isAccepted() && !CrossHelper.getWorld(player).isClient() && this.tryInteractCooldown() && hand == Hand.MAIN_HAND) {
            ItemStack stack = player.getMainHandStack();
            Item handItem = stack.getItem();
            if (player.isSneaking() && this.isOwner(player) && this.modifyItem(player, stack, handItem)) {
                return ActionResult.SUCCESS;
            }
            else if (this.modifyState(player, stack, handItem)) {
                return ActionResult.SUCCESS;
            }
        }
        return action;
    }

    // 修改物品
    private boolean modifyItem(PlayerEntity player, ItemStack handStack, Item handItem) {
        ItemStack foxItem = this.getFox().getEquippedStack(EquipmentSlot.MAINHAND);
        // 设置物品
        if (!handStack.isEmpty() && foxItem.isEmpty()) {
            ItemStack handStackCopy = handStack.copy();
            handStackCopy.setCount(1);
            this.setEquippedStack(handStackCopy);
            if (!player.isCreative()) {
                handStack.decrement(1);
            }
            this.eatingTime = 0;
            return true;
        }
        // 取下物品
        else if (!foxItem.isEmpty()) {
            foxItem = foxItem.copy();
            this.setEquippedStack(ItemStack.EMPTY);
            if (!player.getInventory().insertStack(foxItem)) {
                this.spawnAsEntity(foxItem);
            }
            return true;
        }
        return false;
    }

    // 修改状态
    private boolean modifyState(PlayerEntity player, ItemStack stack, Item handItem) {
        if (this.isTamed()) {
            return this.playerSetWaiting(player);
        }
        // 尝试驯服
        else if (!this.isTamed() && handItem == Items.APPLE) {
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            // 设为驯服
            return this.playerTamedFox(player);
        }
        return false;
    }
    // 玩家驯服狐狸
    private boolean playerTamedFox(PlayerEntity player) {
        if (player != null && !this.isTamed()) {
            FoxEntity fox = this.getFox();
            this.setOwner(player);
            this.setWaiting(true);
            this.mixinStopActions();
            fox.setPersistent();
            CrossHelper.getWorld(fox).sendEntityStatus(fox, (byte) 18);
            return true;
        }
        return false;
    }
    // 玩家修改等待状态
    private boolean playerSetWaiting(PlayerEntity player) {
        if (this.isOwner(player) && !this.inFluidState()) {
            this.setWaiting(!this.isWaiting()); // 修改等待状态
            if (this.isWaiting()) {
                this.isSleeping = SuiKe.random.nextBoolean(); // 随机睡觉或坐下
            }
            return true;
        }
        return false;
    }

// 驯服部分
    // 设置主人
    @Override
    public void setOwner(PlayerEntity player) {
        if (player == null) return;
        this.setOwnerUuid(player.getUuid());
        this.tameAnimalCriteria(player);
    }
    // 获取主人
    @Override
    public LivingEntity getOwner() {
        UUID ownerUuid = this.getOwnerUuid();
        LivingEntity owner = ownerUuid != null ? CrossHelper.getWorld(this.getFox()).getPlayerByUuid(ownerUuid) : null;
        return (owner instanceof PlayerEntity) ? owner : null;
    }
    // 设置主人UUID
    @Override
    public void setOwnerUuid(UUID uuid) {
        this.OWNER_UUID = uuid;
    }
    // 获取主人UUID
    @Override
    public UUID getOwnerUuid() {
        return this.OWNER_UUID;
    }
    // 是否是主人
    @Override
    public boolean isOwner(PlayerEntity player) {
        return this.isTamed() && player != null && player.getUuid().equals(this.getOwnerUuid());
    }
    // 是否驯服
    @Override
    public boolean isTamed() {
        return this.OWNER_UUID != null;
    }

    // 成就
    private void tameAnimalCriteria(PlayerEntity player) {
        if (!CrossHelper.getWorld(player).isClient() && player instanceof ServerPlayerEntity) {
            Criteria.TAME_ANIMAL.trigger((ServerPlayerEntity) player, this.getFox());
        }
    }

// 等待部分
    // 设置等待状态
    @Override
    public void setWaiting(boolean waiting) {
        if (this.isTamed()) {
            this.getFox().setInvulnerable(waiting); // 设置无敌状态
            this.isSleeping = false;
            this.FOX_WAITING = waiting;
        }
    }
    // 是否处于等待状态
    @Override
    public boolean isWaiting() {
        return this.isTamed() && this.FOX_WAITING;
    }

// tick方法
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!this.isTamed() && CrossHelper.getWorld(this.getFox()).isClient()) return;

        if (this.foxClickCooldownTicks > 0) this.foxClickCooldownTicks--;
        if (this.teleportCooldownTicks > 0) this.teleportCooldownTicks--;

        if (!this.isWaiting() && this.getOwner() == null) {
            this.setWaiting(true); // 找不到主人设为等待状态
        }
        // 等待状态尝试传送
        else if (this.isWaiting() && this.getOwner() != null && this.tryTeleportCooldown() && this.inFluidState()) {
            this.setWaiting(false); // 取消等待尝试传送
            if (!this.getFollowGoal().teleport()) {
                this.setWaiting(true); // 传送失败重新打开等待
            }
        }
    }

    private boolean inFluidState() {
        FoxEntity fox = this.getFox();
        FluidState fluidState = CrossHelper.getWorld(fox).getFluidState(fox.getBlockPos());

        return fluidState != null && !fluidState.isEmpty();
    }

// 物品部分
    private void setEquippedStack(ItemStack stack) {
        this.getFox().equipStack(EquipmentSlot.MAINHAND, stack);
        this.setOwnerGiveItem(!stack.isEmpty());
    }

    private void spawnAsEntity(ItemStack stack) {
        ItemEntity item = new ItemEntity(CrossHelper.getWorld(this.getFox()), this.getFox().getX(), this.getFox().getY(), this.getFox().getZ(), stack);
        item.setToDefaultPickupDelay();
        CrossHelper.getWorld(this.getFox()).spawnEntity(item);
    }

    @Inject(method = "canPickupItem", at = @At("HEAD"), cancellable = true)
    private void onCanPickupItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && (this.hasOwnerGiveItem() || SuiKe.foxNotPickupItemList.contains(stack.getItem()))) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public void setOwnerGiveItem(boolean value) {
        if (value && !this.handItemIsEmpty()) {
            this.HAS_OWNER_GIVE = true;
        } else {
            this.HAS_OWNER_GIVE = false;
        }
    }
    @Override
    public boolean hasOwnerGiveItem() {
        return this.isTamed() && !this.handItemIsEmpty() && this.HAS_OWNER_GIVE;
    }
    @Override
    public boolean handItemIsEmpty() {
        return this.getHandItem().isEmpty();
    }
    @Override
    public ItemStack getHandItem() {
        return this.getFox().getEquippedStack(EquipmentSlot.MAINHAND);
    }

// 进食部分
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        FoxEntity fox = this.getFox();
        if (!CrossHelper.getWorld(fox).isClient() && fox.isAlive()) {
            ItemStack handItem = fox.getEquippedStack(EquipmentSlot.MAINHAND);
            // 检查是否完成了吃东西
            if (this.canEat(handItem) && this.eatingTime >= 600) {
                // 恢复生命值
                fox.heal(Math.min(3, fox.getMaxHealth() - fox.getHealth()));
            }
        }
    }

// 死亡
    @Unique
    @Override
    public void onDeath(DamageSource damageSource) {
        FoxEntity fox = this.getFox();
        if (!CrossHelper.getWorld(fox).isClient()) {
            LivingEntity owner = ((IFoxTamed) this).getOwner();
            if (owner instanceof ServerPlayerEntity player) {
                player.sendMessage(((LivingEntityAccessor) this).getDamageTracker().getDeathMessage());
            }
        }
        super.onDeath(damageSource);
    }

// 禁止等待时停止睡觉
    @Inject(method = "stopSleeping", at = @At("HEAD"), cancellable = true)
    private void onStopSleeping(CallbackInfo ci) {
        if (this.isWaiting()) { // 等待状态
            ci.cancel(); // 取消方法执行
        }
    }

    private FoxEntity getFox() {
        return (FoxEntity) (Object) this;
    }
}

@Mixin(MobEntity.class)
interface MobEntityAccessor {
    @Accessor("goalSelector")
    GoalSelector getGoalSelector();
}

@Mixin(LivingEntity.class)
interface LivingEntityAccessor {
    @Accessor("damageTracker")
    DamageTracker getDamageTracker();
}