package suike.suikefoxfriend.entity.fox;

import java.util.UUID;
import java.util.List;
import java.util.Random;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.sound.Sound;
import suike.suikefoxfriend.item.ItemBase;
import suike.suikefoxfriend.expand.Examine;
import suike.suikefoxfriend.entity.fox.ai.*;
import suike.suikefoxfriend.packet.PacketHandler;
import suike.suikefoxfriend.packet.packets.*;
import suike.suikefoxfriend.particle.ModParticle;

import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemToolAssist;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.EnumHand;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import com.google.common.collect.Lists;

public class FoxEntity extends EntityTameable {

    public static List<ItemStack> chanceHasItem = Lists.newArrayList(
        // 5% 概率 (1/20)
        new ItemStack(Items.EMERALD),     // 绿宝石
        // 10% 概率 (2/20) x2
        new ItemStack(Items.RABBIT_FOOT), // 兔子脚
        new ItemStack(Items.RABBIT_FOOT),
        new ItemStack(Items.RABBIT_HIDE), // 兔子皮
        new ItemStack(Items.RABBIT_HIDE),
        // 15% 概率 (3/20)
        new ItemStack(Items.EGG),         // 鸡蛋
        new ItemStack(Items.EGG),
        new ItemStack(Items.EGG),
        // 20% 概率 (4/20) x3
        new ItemStack(Items.WHEAT),       // 小麦
        new ItemStack(Items.WHEAT),
        new ItemStack(Items.WHEAT),
        new ItemStack(Items.WHEAT),
        new ItemStack(Items.LEATHER),     // 皮革
        new ItemStack(Items.LEATHER),
        new ItemStack(Items.LEATHER),
        new ItemStack(Items.LEATHER),
        new ItemStack(Items.FEATHER),     // 羽毛
        new ItemStack(Items.FEATHER),
        new ItemStack(Items.FEATHER),
        new ItemStack(Items.FEATHER)
    );

    private static final DataParameter<Byte> STATE = EntityDataManager.createKey(FoxEntity.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> IS_SNOW = EntityDataManager.createKey(FoxEntity.class, DataSerializers.BOOLEAN);

    private FoxFollowOwnerAI foxFollowOwnerAI;

    public FoxEntity(World world) {
        super(world);
        this.setFoxSize();
        this.setHealth(20.0F);
        this.setCanPickUpLoot(true);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0F);
    }

// 初始化
    @Override
    public void entityInit() {
        super.entityInit();
        this.dataManager.register(IS_SNOW, false);
        this.dataManager.register(STATE, (byte) State.IDLING.ordinal());
    }

    @Override
    protected void initEntityAI() {
        /*等待*/this.tasks.addTask(-1, new FoxWaitingAI(this));
        /*游泳*/this.tasks.addTask(0, new EntityAISwimming(this));
        /*攻击*/this.tasks.addTask(0, new FoxAttackingTargetGetAI(this));
        /*攻击*/this.targetTasks.addTask(0, new FoxAttackingTargetAI(this));
        /*跟随玩家*/this.tasks.addTask(1, this.getFollowAI());
        /*避开实体*/this.tasks.addTask(2, new FoxAvoidEntityAI(this, EntityWolf.class));
        /*避开实体*/this.tasks.addTask(2, new FoxAvoidEntityAI(this, EntityPolarBear.class));
        /*逃跑*/this.tasks.addTask(2, new FoxPanicAI(this));
        /*繁殖*/this.tasks.addTask(2, new EntityAIMate(this, 0.6D));
        /*拾物*/this.tasks.addTask(2, new FoxSurveyItemAI(this, 10.0F));
        /*拾物*/this.tasks.addTask(2, new FoxMoveToItemAI(this, 0.8D));
        /*坐下*/this.tasks.addTask(3, new FoxSittingAI(this));
        /*睡觉*/this.tasks.addTask(3, new FoxSleepingAI(this));

        // 添加追击 鸡和兔子 AI
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(
            this, EntityChicken.class, true));
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(
            this, EntityRabbit.class, true));
        this.tasks.addTask(4, new EntityAIAttackMelee(this, 0.8D, true));

        /*观察玩家*/this.tasks.addTask(5, new FoxWatchPlayerAI(this));
        /*随机漫步*/this.tasks.addTask(5, new EntityAIWander(this, 0.4D));
        /*随机漫步*/this.tasks.addTask(6, new EntityAIWander(this, 0.6D));
        /*随机漫步*/this.tasks.addTask(7, new EntityAIWander(this, 0.8D));
        /*空闲时环顾*/this.tasks.addTask(5, new FoxLookIdleAI(this));
        /*空闲时环顾*/this.tasks.addTask(6, new FoxLookIdleAI(this));
        /*空闲时环顾*/this.tasks.addTask(7, new FoxLookIdleAI(this));
        if (Examine.FuturemcID) {
            /*采摘浆果*/this.tasks.addTask(5, new FoxMoveToBushAI(this, 0.7D, 8));
        }
    }

    public FoxFollowOwnerAI getFollowAI() {
        this.foxFollowOwnerAI = new FoxFollowOwnerAI(this, 0.6D);
        return this.foxFollowOwnerAI;
    }

    public void setFoxSize() {
        if (this.isChild()) {
            this.setSize(0.3F, 0.35F);
        } else {
            this.setSize(0.6F, 0.7F);
        }
    }

// 更新
    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) return;

        if (this.lastSqueakTime > 0) this.lastSqueakTime--;

        this.squeak();

        if (!this.isTamed()) return;

        // 未找到主人, 设为等待状态
        EntityPlayer owner = (EntityPlayer) this.getOwner();
        if (owner == null && !this.isWaiting()) {
            this.setWaiting(true);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        // 每tick增长年龄
        if (!this.world.isRemote) {
            int age = this.getGrowingAge();
            if (age < 0) {
                this.setGrowingAge(age + 1);
            }
            this.handleEatingBehavior();
        }
    }

// 交互
    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack heldStack = player.getHeldItem(hand);

        // 获取玩家手中物品
        if (player.isSneaking()) {
            return this.tryGiveItem(player, heldStack);
        }

        Item heldItem = heldStack.getItem();

        // 喂食苹果
        if (heldItem == Items.APPLE) {
            if (!this.isTamed()) {
                this.setTamedBy(player);
                heldStack.shrink(1);
                return true;
            }
            // 增加成长年龄
            else if (this.isChild()) {
                if (this.world.isRemote) {
                    ModParticle.spawnParticlesVillagerHappy(
                        this.world,
                        new BlockPos(this.posX, this.posY + 0.5D, this.posZ)
                    );
                }
                else {
                    this.setGrowingAge(Math.min(this.growingAge + 1200, 0)); //不能超过 0
                    heldStack.shrink(1);
                }
                return true;
            }
        }
        // 修等待状态
        else if (heldItem != Items.APPLE && this.isOwner(player)) {
            this.setWaiting(!this.isWaiting);
            return true;
        }

        return super.processInteract(player, hand);
    }

// 驯服部分
    @Override
    public void setTamedBy(EntityPlayer player) {
        if (!this.isTamed()) {
            super.setTamedBy(player);
            this.isWaiting = true;
            this.isSleepingWaiting = false;
            this.stabilizePosition();
            this.spawnLoveParticles();
        }
    }

    public boolean isOwner(EntityPlayer player) {
        if (this.isTamed()) {
            UUID ownerUuid = this.getOwnerId();
            if (ownerUuid != null) {
                return this.isTamed() && player.getUniqueID().equals(ownerUuid);
            }
        }
        return false;
    }

    private void spawnLoveParticles() {
        PacketHandler.INSTANCE.sendToAllAround(
            new SpawnParticlesPacket(getEntityId(), 0),
            new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 64)
        );
    }

// 等待部分
    private boolean isWaiting;
    public boolean isWaiting() {
        return this.isWaiting;
    }

    public void setWaiting(boolean waiting) {
        if (waiting) {
            this.isSleepingWaiting = this.rand.nextBoolean();
            this.stabilizePosition();
        } else {
            this.setIdling();
        }
        this.isWaiting = waiting;
    }

    private boolean isSleepingWaiting;
    public boolean isSleepingWaiting() {
        return this.isSleepingWaiting;
    }

// 状态
    public enum State {
        IDLING, SITTING, SLEEPING//, ATTACK // 空闲, 睡觉, 坐下, 扑击
    }

    public State getState() {
        return State.values()[this.dataManager.get(STATE)];
    }
    private void setState(State state) {
        if (!this.world.isRemote) {
            this.dataManager.set(STATE, (byte) state.ordinal());
        }
    }

    public boolean isIdling() {
        return this.getState() == State.IDLING;
    }
    public boolean isSitting() {
        return this.isWaiting || this.getState() == State.SITTING;
    }
    public boolean isSleeping() {
        return this.getState() == State.SLEEPING;
    }

    public void setIdling() {
        this.setState(State.IDLING);
    }
    public void setSitting() {
        this.setState(State.SITTING);
    }
    public void setSleeping() {
        this.setState(State.SLEEPING);
    }

    /*public boolean isJumpAttack() {
        if (this.getState() == State.ATTACK) {
            this.startJumpAttackTick = this.ticksExisted;
            return true;
        }
        return false;
    }
    public void setJumpAttack() {
        this.setState(State.ATTACK);
    }

// 攻击部分
    private int startJumpAttackTick = 0; // 扑击开始时间
    public int getJumpAttackSchedule() {
        return this.ticksExisted - this.startJumpAttackTick;
    }*/
    public EntityLivingBase attackTarget;

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
        return EntityChicken.class.isAssignableFrom(cls)
            || EntityRabbit.class.isAssignableFrom(cls)
            || this.canAttacking();
    }

    @Override
    public boolean shouldAttackEntity(EntityLivingBase attackTarget, EntityLivingBase owner) {
        if (attackTarget == null || attackTarget instanceof EntityCreeper || attackTarget instanceof EntityFlying) {
            return false;
        }

        if (attackTarget instanceof EntityPlayer) {
            return false;
        }
        else if (attackTarget instanceof EntityTameable) {
            EntityTameable tameable = (EntityTameable) attackTarget;
            if (tameable.isTamed() && tameable.getOwner() == owner) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean attackEntityAsMob(Entity attackEntity) {
        ItemStack heldItem = this.getHandItem();
        if (!heldItem.isEmpty()) {
            int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, heldItem);
            if (fireAspectLevel > 0) {
                attackEntity.setFire(fireAspectLevel * 4);
            }
        }

        float damage = this.getAttackDamage(attackEntity);
        attackEntity.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        attackEntity.motionY += 0.1D;

        this.squeak("entity.fox.aggro");

        return true;
    }

    // 获取攻击力
    private float getAttackDamage(Entity attackEntity) {
        float damage = 2.0F;
        ItemStack heldItem = this.getHandItem();

        if (!heldItem.isEmpty()) {
            if (heldItem.getItem() instanceof ItemSword) {
                damage += ((ItemSword) heldItem.getItem()).getAttackDamage();
            }
            else if (heldItem.getItem() instanceof ItemTool) {
                damage += ItemToolAssist.getAttackDamage((ItemTool) heldItem.getItem());
            }
            damage += EnchantmentHelper.getModifierForCreature(
                heldItem, 
                ((EntityLivingBase) attackEntity).getCreatureAttribute()
            );
        }

        return damage;
    }

    public boolean canAttacking() {
        Item heldItem = this.getHandItem().getItem();
        return heldItem instanceof ItemSword || heldItem instanceof ItemAxe;
    }

    public boolean isAttacking() {
        return this.getAttackTarget() != null;
    }

// 携带物品部分
    public EntityItem targetItem;
    private boolean hasOwnerGiveItem;

    public boolean hasOwnerGiveItem() {
        return this.hasOwnerGiveItem;
    }

    public boolean tryGiveItem(EntityPlayer player, ItemStack stack) {
        if (!this.isOwner(player) || this.eating) return false;

        // 空手则获取物品
        if (stack.isEmpty()) {
            ItemStack handItem = this.getHandItem().copy();
            player.field_71071_by.setInventorySlotContents(
                player.field_71071_by.currentItem,
                handItem
            );
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.hasOwnerGiveItem = false;
        }
        // 手持物品则给予物品
        else {
            this.setHeldItem(stack);
            this.hasOwnerGiveItem = true;
        }
        return true;
    }

    @Override
    public void updateEquipmentIfNeeded(EntityItem itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (this.canEquipItem(stack)) {
            this.setHeldItem(stack);
        }
    }

    @Override
    public boolean canEquipItem(ItemStack stack) {
        ItemStack handItem = this.getHandItem();
        boolean isEmpty = handItem.isEmpty();

        // 有物品时则检查是否是玩家给的
        if (!isEmpty && this.hasOwnerGiveItem) {
            return false;
        }

        // 没有物品时随意拾取
        if (isEmpty) return true;

        // 如果已拾取物品, 则优先食物
        return !(handItem.getItem() instanceof ItemFood) && stack.getItem() instanceof ItemFood;
    }

    public void setHeldItem(ItemStack stack) {
        // 如果已有物品则丢掉
        ItemStack handItem = this.getHandItem();
        if (!handItem.isEmpty() && !this.world.isRemote) {
            this.dropItem();
        }

        // 只保留一个物品
        ItemStack copy = stack.copy();
        copy.setCount(1);

        // 修改物品
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, copy);
        Sound.playSound(this.world, this.getPos(), "entity.fox.bite");

        // 更新食物冷却
        this.eatingTimer = 0;

        stack.shrink(1);
    }

    public ItemStack getHandItem() {
        return this.getHeldItemMainhand();
    }

    public boolean isValidTargetItem() {
        return this.isValidTargetItem(this.targetItem);
    }
    public boolean isValidTargetItem(EntityItem item) {
        if (item == null || !item.isEntityAlive()) {
            return false;
        }

        // 如果手上没有物品，接受任何物品
        if (this.getHandItem().isEmpty()) {
            return true;
        }
        // 如果手上有非食物物品，只接受食物
        else if (!this.handItemIsFood()) {
            return item.getItem().getItem() instanceof ItemFood;
        }
        return false;
    }

    public void dropItem() {
        this.hasOwnerGiveItem = false;
        this.entityDropItem(this.getHandItem(), 0);
        Sound.playSound(this.world, this.getPos(), "entity.fox.spit");
    }

    private void initHandItem() {
        if (rand.nextFloat() < 0.2F) {
            this.setHeldItem(chanceHasItem.get(this.rand.nextInt(chanceHasItem.size())).copy());
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

// 进食部分
    private boolean eating;
    private int eatingTimer = 0;

    private void handleEatingBehavior() {
        if (this.eatingTimer <= 0 && this.handItemIsFood()) {
            this.eatingTimer = 520 + this.rand.nextInt(40); // 进食倒计时
        }
        // 进食计时器处理
        else if (!this.isSleeping() && this.eatingTimer > 0) {
            this.eatingTimer--;

            // 开始粒子效果阶段 (560-520刻)
            if (this.eatingTimer <= 40 && this.eatingTimer >= 5) {
                this.eating = true;
                if (this.eatingTimer % 10 == 0 || this.eatingTimer == 5) {
                    this.spawnEatingParticles();
                    Sound.playSound(this.world, this.getPos(), "entity.fox.eat", (this.rand.nextFloat() * 0.2F) + 0.8F, 1.0F);
                }
            }
            // 吃掉食物阶段 (520刻完成)
            else if (this.eatingTimer == 0) {
                this.spawnEatingParticles();
                Sound.playSound(this.world, this.getPos(), "entity.fox.eat", 1.2F, 1.0F);
                this.consumeFoodItem();
                this.eating = false;
            }
        }
    }

    private void consumeFoodItem() {
        ItemStack handStack = this.getHandItem().copy();
        Item item = handStack.getItem();

        // 确保物品依然是食物
        if (!handStack.isEmpty() && item instanceof ItemFood) {
            // 恢复生命值
            float healAmount = ((ItemFood) item).getHealAmount(handStack);
            this.heal(healAmount);

            if (item instanceof ItemAppleGold) {
                this.getGoldAppleEffect(handStack);
            } else {
                ((ItemFood) item).onItemUseFinish(handStack, this.world, this);
            }

            ItemStack container = getFoodContainer(handStack);
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, container);

            // 已将玩家给的食物吃掉
            this.hasOwnerGiveItem = false;
        }
    }

    private ItemStack getFoodContainer(ItemStack foodStack) {
        Item item = foodStack.getItem();

        if (item == Items.MUSHROOM_STEW || item == Items.RABBIT_STEW || item == Items.BEETROOT_SOUP) {
            return new ItemStack(Items.BOWL);
        }

        String registryName = item.getRegistryName().toString();

        // 处理汤类食物 -> 碗
        if (registryName.contains("stew") || registryName.contains("soup")) {
            return new ItemStack(Items.BOWL);
        }
        // 处理瓶装食物 -> 玻璃瓶
        if (registryName.contains("bottle")) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }

        return ItemStack.EMPTY;
    }

    private void getGoldAppleEffect(ItemStack stack) {
        if (!this.world.isRemote) {
            if (stack.getMetadata() > 0) {
                this.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 400, 1));
                this.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 6000, 0));
                this.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 6000, 0));
                this.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2400, 3));
            }
            else {
                this.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 1));
                this.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 2400, 0));
            }
        }
    }

    public boolean handItemIsFood() {
        ItemStack handItem = this.getHandItem();
        return !handItem.isEmpty() && handItem.getItem() instanceof ItemFood;
    }

    private void spawnEatingParticles() {
        PacketHandler.INSTANCE.sendToAllAround(
            new SpawnFoodParticlesPacket(getEntityId(), this.getHandItem().getItem().getRegistryName().toString()),
            new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 64)
        );
    }

    public Vec3d getNosePosition() {
        // 基础位置
        Vec3d basePos = new Vec3d(posX, posY + (this.isChild() ? 0.2 : 0.5), posZ);
        // 正前方偏移
        float yawRad = rotationYawHead * (float)Math.PI / 180F; // 转为弧度
        Vec3d forward = new Vec3d(
            -Math.sin(yawRad), // X轴分量
            0,                 // Y轴分量 (水平方向无变化)
            Math.cos(yawRad)   // Z轴分量
        ).scale(this.isChild() ? 0.1 : 0.5);  

        return basePos.add(forward);
    }

// 狐狸类型
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance instance, IEntityLivingData data) {
        // 检查是否在雪地生物群系生成
        Biome biome = this.world.getBiome(this.getPos());
        if (biome.getTemperature(this.getPos()) < 0.15F) {
            this.setFoxType(true); // 设置为雪狐
        }
        if (this.rand.nextFloat() < 0.08F) {
            this.setGrowingAge(-24000);
        }

        this.initHandItem();

        if (instance == null && data == null) return null;

        return super.onInitialSpawn(instance, data);
    }

    public boolean isSnowType() {
        return this.dataManager.get(IS_SNOW);
    }
    public void setFoxType(boolean isSnow) {
        this.dataManager.set(IS_SNOW, isSnow);
    }

// 繁殖
    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.APPLE;
    }

    @Override
    public boolean canMateWith(EntityAnimal other) {
        return this.isTamed()
            && ((FoxEntity) other).isTamed()
            && super.canMateWith(other);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        FoxEntity child = new FoxEntity(this.world);
        child.setGrowingAge(-24000);
        child.setTamed(true);
        UUID ownerUuid = this.getOwnerId();
        if (ownerUuid != null) {
            child.setOwnerId(ownerUuid);
        }
        this.isSnowFoxBaby(child, ageable);

        this.setGrowingAge(6000);
        ((EntityAnimal)ageable).setGrowingAge(6000);  // 繁殖冷却
        return child;
    }

    private void isSnowFoxBaby(FoxEntity child, EntityAgeable ageable) {
        boolean parent1Snow = this.isSnowType();
        boolean parent2Snow = ((FoxEntity) ageable).isSnowType();

        if (parent1Snow && parent2Snow) {
            // 父母都是雪狐 -> 100%雪狐
            child.setFoxType(true);
        } else if (parent1Snow || parent2Snow) {
            // 只有一个是雪狐 -> 50%概率
            child.setFoxType(this.rand.nextBoolean());
        }
    }

// 狐狸宝宝
    @Override
    public boolean isChild() {
        return this.getGrowingAge() < 0;
    }

    @Override
    public void setGrowingAge(int age) {
        super.setGrowingAge(age);
        if (age >= 0) {
            this.setFoxSize();
        }
    }

// 移动
    public void stabilizePosition() {
        this.motionX = 0;
        this.motionZ = 0;
        this.setAttackTarget(null);
        this.getNavigator().clearPath();
        this.getMoveHelper().action = EntityMoveHelper.Action.WAIT;
    }

    @Override
    public int getMaxFallHeight() {
        return 10;
    }

    /*@Override
    public void fall(float distance, float damageMultiplier) {
        // 跳跃攻击中免疫摔落伤害
        if (this.getState() == State.ATTACK) {
            super.fall(distance, 0);
        } else {
            super.fall(distance, damageMultiplier);
        }
    }*/

// 声音
    private int lastSqueakTime;
    public void squeak() {
        this.squeak(this.isSleeping() ? "entity.fox.sleep" : "entity.fox.ambient");
    }
    public void squeak(String soundName) {
        if (this.lastSqueakTime != 0) return;
        if (this.rand.nextFloat() < 0.06F) {
            float a = this.isChild() ? 0.6f : 1.0f;
            Sound.playSound(this.world, this.getPos(), soundName, a, 1.0f);

            this.lastSqueakTime = 80;
        }
    }
    public void playSound(String soundName) {
        float a = this.isChild() ? 0.6f : 1.0f;
        Sound.playSound(this.world, this.getPos(), soundName, a, 1.0f);
    }

    @Override public void playStepSound(BlockPos pos, Block block) {}

// 受到攻击
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isWaiting) return false;

        if (source instanceof EntityDamageSource) {
            Entity entity = ((EntityDamageSource) source).getTrueSource();
            if (entity instanceof EntityLivingBase) {
                this.attackTarget = (EntityLivingBase) entity;
            }
            if (!(entity instanceof EntityPlayer)) {
                if (this.isTamed() && amount > 1) {
                    amount = 1;
                }
            }
        } else {
            if (this.isTamed() && amount > 1) {
                amount = 1;
            }
        }

        if (!super.attackEntityFrom(source, amount)) return false;

        this.setIdling();
        this.playSound("entity.fox.hurt");

        return true;
    }

    @Override
    public void setDead() {
        super.setDead();
        if (!this.world.isRemote && this.isDead) {
            this.playSound("entity.fox.death");
            ItemStack handItem = this.getHandItem();
            if (!handItem.isEmpty()) {
                this.entityDropItem(handItem, 0);
            }

            // this.tryTeleportToOwner();
        }
    }

// 存储 & 读取
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Type")) {
            String type = compound.getString("Type");
            if ("snow".equals(type) || "Snow".equals(type)) {
                this.setFoxType(true);
            }
        }
        if (compound.hasKey("Sitting")) {
            if (compound.getBoolean("Sitting")) {
                this.setSitting();
            }
        }
        if (compound.hasKey("Sleeping")) {
            if (compound.getBoolean("Sleeping")) {
                this.setSleeping();
            }
        }
        if (compound.hasKey("Waiting")) {
            this.setWaiting(compound.getBoolean("Waiting"));
            this.isSleepingWaiting = this.isSleeping();
        }
        if (compound.hasKey("HasOwnerGiveItem")) {
            this.hasOwnerGiveItem = compound.getBoolean("HasOwnerGiveItem");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setString("Type", this.isSnowType() ? "snow" : "red");
        compound.setBoolean("Sitting", (this.getState() == State.SITTING));
        compound.setBoolean("Sleeping", this.isSleeping());
        if (this.isWaiting()) {
            compound.setBoolean("Waiting", this.isWaiting());
        }
        if (!this.getHandItem().isEmpty() && this.hasOwnerGiveItem) {
            compound.setBoolean("HasOwnerGiveItem", true);
        }
        return compound;
    }

// 传送
    private void tryTeleportToOwner() {
        if (!this.world.isRemote && !this.world.isBlockLoaded(this.getPos())) {
            this.foxFollowOwnerAI.tryTeleportToOwner();
        }
    }

    @Override
    public boolean attemptTeleport(double x, double y, double z) {
        boolean a = super.attemptTeleport(x, y, z);
        if (a) {
            this.teleportEffect();
        }
        return a;
    }
 
    public void teleportEffect() {
        this.motionY = 0;
        this.onGround = true;
        this.setIdling();
        this.spawnTeleportParticles();
        Sound.playSound(this.world, this.getPos(), "entity.fox.teleport");
    }

    private void spawnTeleportParticles() {
        PacketHandler.INSTANCE.sendToAllAround(
            new SpawnParticlesPacket(getEntityId(), 1),
            new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 64)
        );
    }

// 动画
    // 眨眼动画
    private int lastBlinkTick = 0;         // 上次眨眼的时间刻
    private Integer nextBlinkTick = null;  // 下次眨眼的时间刻
    // 获取下次眨眼时间 (如果未初始化则生成)
    public int getNextBlinkTick() {
        if (this.nextBlinkTick == null) {
            // 随机生成 8~12 秒的间隔 (160~240 ticks)
            int randomInterval = 160 + this.rand.nextInt(80); 
            this.nextBlinkTick = this.lastBlinkTick + randomInterval;
        }
        return this.nextBlinkTick;
    }
    // 触发眨眼后重置周期
    public void resetBlinkTimer() {
        this.lastBlinkTick = this.ticksExisted;
        this.nextBlinkTick = null;
    }

    // 耳朵动画
    private int lastTwitchTick = 0;         // 上次耳朵颤动的时间刻
    private Integer nextTwitchTick = null;  // 下次耳朵颤动的时间刻
    // 获取下次颤动时间 (如果未初始化则生成)
    public int getNextTwitchTick() {
        if (this.nextTwitchTick == null) {
            // 随机生成 6~10 秒的间隔 (120~200 ticks) - 比眨眼频率稍高
            int randomInterval = 120 + this.rand.nextInt(80); 
            this.nextTwitchTick = this.lastTwitchTick + randomInterval;
        }
        return this.nextTwitchTick;
    }
    // 触发颤动后重置周期
    public void resetTwitchTimer() {
        this.lastTwitchTick = this.ticksExisted;
        this.nextTwitchTick = null;
    }

// 获取
    public BlockPos getPos() {
        return new BlockPos(this.posX, this.posY, this.posZ);
    }
    public Random getRandom() {
        return this.rand;
    }
    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(ItemBase.FOX_EGG);
    }
}