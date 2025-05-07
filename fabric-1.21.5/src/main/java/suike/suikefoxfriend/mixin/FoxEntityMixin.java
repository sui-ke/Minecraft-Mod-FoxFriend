package suike.suikefoxfriend.mixin;

import java.util.UUID;
import java.util.Random;
import java.util.Optional;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.api.*;
import suike.suikefoxfriend.entity.ai.FoxWaitingGoal;
import suike.suikefoxfriend.entity.ai.FoxFollowOwnerGoal;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
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
    private static Random random = new Random();
    private FoxFollowOwnerGoal foxFollowOwnerGoal;
    private boolean isSleeping = false;
    public boolean getIsSleeping() {return this.isSleeping;}

    @Shadow abstract void stopActions();
    @Shadow abstract void setSleeping(boolean sleeping);
    @Shadow abstract void setAggressive(boolean aggressive);
    public void mixinStopActions() {this.stopActions();}
    public void mixinSetSleeping(boolean sleeping) {this.setSleeping(sleeping);}
    public void mixinSetAggressive(boolean aggressive) {this.setAggressive(aggressive);}

    private static final TrackedData<Byte> TAMEABLE_FLAGS = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<String> OWNER_UUID = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> WAITING_FLAG = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public void playerSetWaiting(PlayerEntity player) { // 玩家修改等待状态
        if (this.foxCooldownTicks <= 0) {
            if (player.getUuid().equals(this.getOwnerUuid())) {
                if (!this.isWaiting()) {
                    this.isSleeping = random.nextBoolean(); // 随机睡觉或坐下
                }
                this.setWaiting(!this.isWaiting(), "isPlayer"); // 修改等待状态
                this.tameAnimalCriteria(player);
                this.foxCooldownTicks = 3; // 修改冷却(tick)
            }
        }
    }

    public void playerTamedFox(PlayerEntity player) {
        if (player != null) {
            FoxEntity foxEntity = (FoxEntity) (Object) this;
            this.setOwner(player);
            this.setWaiting(true);
            this.mixinStopActions();
            foxEntity.setPersistent();
            this.foxFollowOwnerGoal = new FoxFollowOwnerGoal(foxEntity);
            ((MobEntityAccessor) this).getGoalSelector().add(5, this.foxFollowOwnerGoal);
            ((MobEntityAccessor) this).getGoalSelector().add(-1, new FoxWaitingGoal(foxEntity));
        }
    }

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    private void onInitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(OWNER_UUID, "");
        builder.add(TAMEABLE_FLAGS, (byte) 0);
        builder.add(WAITING_FLAG, this.isWaiting());
    }

//驯服部分
    public void setOwner(PlayerEntity player) { // 设置主人
        this.setTamed(true);
        this.setOwnerUuid(player.getUuid());
        this.tameAnimalCriteria(player);
    }

    public void setOwnerUuid(UUID uuid) { // 设置主人UUID
        String uuidString = (uuid != null) ? uuid.toString() : "";
        ((FoxEntity) (Object) this).getDataTracker().set(OWNER_UUID, uuidString);
    }

    public void setTamed(boolean tamed) { // 设置驯服
        byte b = ((FoxEntity) (Object) this).getDataTracker().get(TAMEABLE_FLAGS);
        if (tamed) {
            ((FoxEntity) (Object) this).getDataTracker().set(TAMEABLE_FLAGS, (byte) (b | 4));
        } else {
            ((FoxEntity) (Object) this).getDataTracker().set(TAMEABLE_FLAGS, (byte) (b & -5));
        }
    }

    public boolean isTamed() { // 是否驯服
        FoxEntity foxEntity = (FoxEntity) (Object) this;
        return foxEntity.getDataTracker() != null && (foxEntity.getDataTracker().get(TAMEABLE_FLAGS) & 4) != 0;
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
        String uuidString = ((FoxEntity) (Object) this).getDataTracker().get(OWNER_UUID);
        if (uuidString != null && !uuidString.isEmpty()) {
            try {
                return UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {}
        }
        return null;
    }

    public void tameAnimalCriteria(PlayerEntity player) { // 成就
        if (player instanceof ServerPlayerEntity) {
            Criteria.TAME_ANIMAL.trigger((ServerPlayerEntity) player, (FoxEntity) (Object) this);
        }
    }

//等待部分
    public void setWaiting(boolean waiting) {
        this.setWaiting(waiting, "notIsPlayer");
    }
    public void setWaiting(boolean waiting, String isPlayerSetWaiting) { // 设置等待状态
        if (this.isTamed()) {
            FoxEntity foxEntity = (FoxEntity) (Object) this;

            foxEntity.setInvulnerable(waiting); // 设置无敌状态
            foxEntity.getDataTracker().set(WAITING_FLAG, waiting);

            if (isPlayerSetWaiting.equals("isPlayer")) {
                this.setFoxName(foxEntity, waiting);
            } else {
                this.setFoxName(foxEntity, false); // 清除等待字段
            }
        }
    }

    public boolean isWaiting() { // 获取等待状态
        return this.isTamed() && ((FoxEntity) (Object) this).getDataTracker().get(WAITING_FLAG);
    }

    public void setFoxName(FoxEntity foxEntity, boolean waiting) {
        String foxName = foxEntity.getName().getString(); // 获取当前名称
        String waitingString = Text.translatable("foxfriend.waiting").getString(); // 获取等待字段

        if (waiting) {
            if (!foxName.contains(waitingString)) { // 如果名称中不包含等待字段
                foxEntity.setCustomName(Text.literal(foxName + waitingString)); // 添加等待字段
            }
            this.foxNameWaitingTicks = 50; // 显示等待状态
        } else {
            String defaultFoxName = Text.translatable("entity.minecraft.fox").getString(); // 获取狐狸默认名称

            String oldFoxName = foxName.replace(waitingString, ""); // 去除等待字段
            foxEntity.setCustomName(Text.literal(oldFoxName)); // 恢复原始名称

            if (oldFoxName.equals(defaultFoxName)) { // 如果玩家未使用命名牌
                foxEntity.setCustomName(null); // 清除名字
            }
            this.foxNameWaitingTicks = 0;
        }
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
        if (this.foxNameWaitingTicks > 0) {
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
        if (this.isTamed()) {
            nbt.putBoolean("Tamed", this.isTamed());
            nbt.putBoolean("Waiting", this.isWaiting());
            nbt.putString("Owner", this.getOwnerUuid().toString()); // 将 UUID 转换为字符串存储
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("Owner")) {
            UUID ownerUuid = tryReadOwnerUuid(nbt);

            if (ownerUuid != null) {
                this.setOwnerUuid(ownerUuid);
                this.setTamed(nbt.getBoolean("Tamed").orElse(false));
                this.isSleeping = nbt.getBoolean("Sleeping").orElse(false);
                this.setWaiting(nbt.getBoolean("Waiting").orElse(false));

                FoxEntity foxEntity = (FoxEntity) (Object) this;
                this.mixinStopActions();
                foxEntity.setPersistent();
                this.foxFollowOwnerGoal = new FoxFollowOwnerGoal(foxEntity);
                ((MobEntityAccessor) this).getGoalSelector().add(5, this.foxFollowOwnerGoal);
                ((MobEntityAccessor) this).getGoalSelector().add(-1, new FoxWaitingGoal(foxEntity));
            }
        }
    }

    public UUID tryReadOwnerUuid(NbtCompound nbt) {
        NbtElement element = nbt.get("Owner");
        if (element == null) {
            return null;
        }

        // 判断类型并读取
        if (element instanceof NbtIntArray) {
            // 读取旧版模组保存的 UUID
            int[] uuidInts = ((NbtIntArray) element).getIntArray();
            if (uuidInts.length == 4) {
                return new UUID((long) uuidInts[0] << 32 | uuidInts[1] & 0xFFFFFFFFL,
                                (long) uuidInts[2] << 32 | uuidInts[3] & 0xFFFFFFFFL);
            }
        } else if (element instanceof NbtString) {
            String ownerUuidString = ((NbtString) element).toString().replace("\"", "");
            if (ownerUuidString != null) {
                try {
                    return UUID.fromString(ownerUuidString);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

//尝试捡起物品
    @Inject(method = "canPickupItem", at = @At("HEAD"), cancellable = true)
    private void onCanPickupItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.isTamed()) { // 是否驯服
            boolean canPickupItem = false;
            ItemStack handItem = ((FoxEntity) (Object) this).getEquippedStack(EquipmentSlot.MAINHAND);

            if (handItem.isEmpty() || !this.canPickup(handItem)) {
                canPickupItem = this.canPickup(stack);
            }

            cir.setReturnValue(canPickupItem); // 设置返回值
        }
    }

    public boolean canPickup(ItemStack stack) {
        Item dropItem = stack.getItem();
        if (SuiKe.foxCanPickupItemList.contains(dropItem)) {
            return true;
        } else if (SuiKe.foxNotPickupItemList.contains(dropItem)) {
            return false;
        } else if ((FoodComponent) stack.get(DataComponentTypes.FOOD) != null) {
            return true;
        }
        return false;
    }

//禁止等待时停止睡觉
    @Inject(method = "stopSleeping", at = @At("HEAD"), cancellable = true)
    private void onStopSleeping(CallbackInfo ci) {
        if (this.isWaiting()) { // 等待状态
            ci.cancel(); // 取消方法执行
        }
    }
}

@Mixin(Entity.class)
interface EntityAccessor {
    @Accessor("world")
    World getWorld();
}

@Mixin(MobEntity.class)
interface MobEntityAccessor {
    @Accessor("navigation")
    EntityNavigation getNavigation();
    @Accessor("goalSelector")
    GoalSelector getGoalSelector();
}