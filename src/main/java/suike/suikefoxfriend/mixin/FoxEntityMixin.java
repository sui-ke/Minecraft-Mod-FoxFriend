package suike.suikefoxfriend.mixin;

import java.util.UUID;
import java.util.Random;
import java.util.Optional;
import java.lang.reflect.Constructor;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.api.IOwnable;
import suike.suikefoxfriend.entity.ai.FoxWaitingGoal;
import suike.suikefoxfriend.entity.ai.FoxFollowOwnerGoal;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.world.World;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.fluid.FluidState;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoxEntity.class)
public abstract class FoxEntityMixin implements IOwnable {//, Tameable {

    private int foxCooldownTicks = 0;
    private int foxNameWaitingTicks = 0;
    private boolean isSleeping = false;
    private static Random random = new Random();
    private FoxFollowOwnerGoal foxFollowOwnerGoal;

    @Shadow abstract void stopActions();
    @Shadow abstract void setSleeping(boolean aggressive);
    @Shadow abstract void setAggressive(boolean aggressive);
    public void mixinStopActions() {this.stopActions();}
    public void mixinSetSleeping(boolean sleeping) {this.setSleeping(sleeping);}
    public void mixinSetAggressive(boolean aggressive) {this.setAggressive(aggressive);}

    private static final TrackedData<Byte> TAMEABLE_FLAGS = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> WAITING_FLAG = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    public boolean getIsSleeping() {
        return this.isSleeping;
    }
    public void playerSetWaiting(PlayerEntity player) { // 玩家修改等待状态
        if (this.foxCooldownTicks <= 0) {
            if (player.getUuid().equals(this.getOwnerUuid())) {
                if (!this.isWaiting()) {
                    this.isSleeping = random.nextBoolean(); // 随机睡觉或坐下
                }
                this.setWaiting(!this.isWaiting()); // 修改等待状态
                this.tameAnimalCriteria(player);
                this.foxCooldownTicks = 3; // 修改冷却(tick)
            }
        }
    }

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    private void onInitDataTracker(CallbackInfo ci) {
        ((EntityAccessor) this).getDataTracker().startTracking(TAMEABLE_FLAGS, (byte) 0);
        ((EntityAccessor) this).getDataTracker().startTracking(OWNER_UUID, Optional.empty());
        ((FoxEntity) (Object) this).getDataTracker().startTracking(WAITING_FLAG, this.isWaiting());
    }

    @Inject(method = "eat", at = @At("HEAD"), cancellable = true)
    private void onEat(PlayerEntity player, Hand hand, ItemStack itemStack, CallbackInfo ci) {
        if (player != null && itemStack.isOf(Items.SWEET_BERRIES) && !this.isTamed()) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }

            FoxEntity foxEntity = (FoxEntity) (Object) this;
            this.setOwner(player);
            this.setWaiting(true);
            this.mixinStopActions();
            foxEntity.setPersistent();
            this.foxFollowOwnerGoal = new FoxFollowOwnerGoal(foxEntity);
            ((MobEntityAccessor) this).getGoalSelector().add(5, this.foxFollowOwnerGoal);
            ((MobEntityAccessor) this).getGoalSelector().add(-1, new FoxWaitingGoal(foxEntity));
            ci.cancel();
        }
    }

//驯服部分
    public void setOwner(PlayerEntity player) { // 设置主人
        this.setTamed(true);
        this.setOwnerUuid(player.getUuid());
        this.tameAnimalCriteria(player);
    }

    public void setOwnerUuid(UUID uuid) { // 设置主人UUID
        ((EntityAccessor) this).getDataTracker().set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public void setTamed(boolean tamed) { // 设置驯服
        byte b = ((EntityAccessor) this).getDataTracker().get(TAMEABLE_FLAGS);
        if (tamed) {
            ((EntityAccessor) this).getDataTracker().set(TAMEABLE_FLAGS, (byte) (b | 4));
        } else {
            ((EntityAccessor) this).getDataTracker().set(TAMEABLE_FLAGS, (byte) (b & -5));
        }
    }

    public boolean isTamed() { // 是否驯服
        return (((EntityAccessor) this).getDataTracker().get(TAMEABLE_FLAGS) & 4) != 0;
    }

    public LivingEntity getOwner() { // 获取主人
        UUID ownerUuid = this.getOwnerUuid();
        if (ownerUuid == null) {
            return null;
        }
        LivingEntity owner = ((EntityAccessor) this).getWorld().getPlayerByUuid(ownerUuid);
        return (owner instanceof PlayerEntity) ? owner : null;
    }

    public UUID getOwnerUuid() { // 获取主人UUID
        return ((EntityAccessor) this).getDataTracker().get(OWNER_UUID).orElse(null);
    }

    public void tameAnimalCriteria(PlayerEntity player) { // 成就
        if (player instanceof ServerPlayerEntity) {
            Criteria.TAME_ANIMAL.trigger((ServerPlayerEntity) player, (FoxEntity) (Object) this);
        }
    }

//等待部分
    public void setWaiting(boolean waiting) { // 设置等待状态
        if (this.isTamed()) {
            FoxEntity foxEntity = (FoxEntity) (Object) this;

            foxEntity.setInvulnerable(waiting); // 设置无敌状态
            foxEntity.getDataTracker().set(WAITING_FLAG, waiting);

            if (SuiKe.MCVersion < 1190) {
                return;
            }

            String foxName = foxEntity.getName().getString(); // 获取当前名称
            String waitingString = Text.translatable("foxfriend.waiting").getString(); // 获取等待字段

            if (waiting) {
                if (!foxName.contains(waitingString)) { // 如果名称中不包含等待字段
                    foxEntity.setCustomName(Text.literal(foxName + waitingString)); // 添加等待字段
                }
                this.foxNameWaitingTicks = 50; // 显示等待状态
            } else {
                String defaultFoxName = Text.translatable("entity.minecraft.fox").getString(); // 获取狐狸默认名称

                foxName = foxName.replace(waitingString, ""); // 去除等待字段
                foxEntity.setCustomName(Text.literal(foxName)); // 恢复原始名称

                if (foxName.equals(defaultFoxName)) { // 如果玩家未使用命名牌
                    foxEntity.setCustomName(null); // 清除名字
                }
                this.foxNameWaitingTicks = 0;
            }
        }
    }

    public boolean isWaiting() { // 获取等待状态
        return this.isTamed() && ((FoxEntity) (Object) this).getDataTracker().get(WAITING_FLAG);
    }
//tick方法
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!this.isTamed()) {
            return;
        }

        if (this.foxCooldownTicks > 0) {
            this.foxCooldownTicks--; // 减少点击冷却
        }

        FoxEntity foxEntity = (FoxEntity) (Object) this;
        if (SuiKe.MCVersion >= 1190 && this.foxNameWaitingTicks > 0) {
            this.foxNameWaitingTicks--; // 减少显示时间
            if (foxNameWaitingTicks == 0) {
                String foxName = foxEntity.getName().getString(); // 获取当前名称
                String waitingString = Text.translatable("foxfriend.waiting").getString(); // 获取等待字段
                String defaultFoxName = Text.translatable("entity.minecraft.fox").getString(); // 获取狐狸默认名称

                foxName = foxName.replace(waitingString, ""); // 去除等待字段
                foxEntity.setCustomName(Text.literal(foxName)); // 恢复原始名称

                if (foxName.equals(defaultFoxName)) { // 如果玩家未使用命名牌
                    foxEntity.setCustomName(null); // 清除名字
                }
            }
        }

        if (!isWaiting() && this.foxFollowOwnerGoal != null) {
            if (this.foxFollowOwnerGoal.teleport()) { // 尝试传送到主人
                return;
            }
        }

        World world = foxEntity.getWorld();
        BlockPos pos = foxEntity.getBlockPos();
        FluidState fluidState = world.getFluidState(pos);
        // 检查是否在液体里
        if (fluidState != null && !fluidState.isEmpty()) {
            this.setWaiting(false);
        } else if (this.getOwner() == null) { // 找不到主人设为等待状态
            this.setWaiting(true);
        }
    }

//存储读取
    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.getOwnerUuid() != null) {
            nbt.putUuid("Owner", this.getOwnerUuid());
        }
        nbt.putBoolean("Tamed", this.isTamed());
        nbt.putBoolean("Waiting", this.isWaiting());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.containsUuid("Owner")) {
            this.setTamed(nbt.getBoolean("Tamed"));
            this.setOwnerUuid(nbt.getUuid("Owner"));

            if (this.isTamed()) {
                FoxEntity foxEntity = (FoxEntity) (Object) this;
                this.mixinStopActions();
                foxEntity.setPersistent();
                this.foxFollowOwnerGoal = new FoxFollowOwnerGoal(foxEntity);
                ((MobEntityAccessor) this).getGoalSelector().add(5, this.foxFollowOwnerGoal);
                ((MobEntityAccessor) this).getGoalSelector().add(-1, new FoxWaitingGoal(foxEntity));

                if (nbt.contains("Waiting")) {
                    this.setWaiting(nbt.getBoolean("Waiting"));
                }
            }
        }
    }

//
    @Inject(method = "stopSleeping", at = @At("HEAD"), cancellable = true)
    private void onStopSleeping(CallbackInfo ci) {
        if (this.isWaiting()) { // 等待状态
            ci.cancel(); // 取消方法执行
        }
    }

    @Inject(method = "canPickupItem", at = @At("HEAD"), cancellable = true)
    private void onCanPickupItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.isTamed()) { // 是否驯服
            Item dropItem = stack.getItem();
            boolean canPickupItem = !SuiKe.foxNotPickupItemList.contains(dropItem) && (dropItem.isFood() || SuiKe.foxCanPickupItemList.contains(dropItem));
            cir.setReturnValue(canPickupItem); // 设置返回值
        }
    }
}

@Mixin(Entity.class)
interface EntityAccessor {
    @Accessor("world")
    World getWorld();
    @Accessor("dataTracker")
    DataTracker getDataTracker();
}

@Mixin(MobEntity.class)
interface MobEntityAccessor {
    @Accessor("navigation")
    EntityNavigation getNavigation();
    @Accessor("goalSelector")
    GoalSelector getGoalSelector();
}