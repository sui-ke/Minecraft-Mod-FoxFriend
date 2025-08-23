package suike.suikefoxfriend.entity.fox.ai;

import java.util.List;
import java.util.ArrayList;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;

public class FoxFollowOwnerAI extends EntityAIBase {
    private static final float minDist = 10.0F;
    private static final float maxDist = 2.0F;
    private static final int searchRadius = 2;
    private static final int verticalSearch = 1;

    private final FoxEntity fox;
    private final double followSpeed; // 跟随速度
    private final PathNavigate petPathfinder; // 路径导航器
    private int timeToRecalcPath; // 路径重新计算计时器
    private float oldWaterCost;   // 原始的水中移动成本

    public FoxFollowOwnerAI(FoxEntity fox, double followSpeed) {
        this.fox = fox;
        this.followSpeed = followSpeed;
        this.petPathfinder = fox.getNavigator();
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        // 如果没有主人, 正在等待 或 距离太近
        if (this.fox.getOwner() == null || this.fox.isWaiting() || this.fox.getDistanceSq(this.fox.getOwner()) < (minDist * minDist)) {
            return false;
        }
        this.fox.setIdling();
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.fox.getOwner() == null) {
            this.fox.setWaiting(true);
            return false;
        }

        // 如果路径未完成 && 距离超过最大距离 && 不在等待状态, 则继续
        return !this.petPathfinder.noPath()
            && !this.fox.isWaiting();
    }

    @Override
    public void startExecuting() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.fox.getPathPriority(PathNodeType.WATER);
        this.fox.setPathPriority(PathNodeType.WATER, 0.0F); // 降低水中移动成本
    }

    @Override
    public void resetTask() {
        this.petPathfinder.clearPath();
        this.fox.setPathPriority(PathNodeType.WATER, this.oldWaterCost); // 恢复原始水中移动成本
    }

// 更新
    @Override
    public void updateTask() {
        EntityPlayer owner = (EntityPlayer) this.fox.getOwner();
        if (owner == null) return;

        // 让狐狸看向主人
        this.fox.getLookHelper().setLookPositionWithEntity(owner, 10.0F, (float) this.fox.getVerticalFaceSpeed());

        // 如果狐狸不在等待状态, 则处理移动逻辑
        if (!this.fox.isWaiting()) {
            // 定期重新计算路径
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                // 尝试移动到主人位置
                if (!this.petPathfinder.tryMoveToEntityLiving(owner, this.followSpeed)) {
                    // 如果路径计算失败, 尝试传送
                    if (!this.fox.getLeashed() && !this.fox.isRiding() && 
                        this.fox.getDistanceSq(owner) >= 144.0D) {
                        tryTeleportToOwner();
                    }
                }
            }
        }
    }

// 尝试传送到主人附近
    public void tryTeleportToOwner() {
        EntityPlayer owner = (EntityPlayer) this.fox.getOwner();
        if (owner == null) return;

        BlockPos ownerPos = new BlockPos(owner);
        List<BlockPos> safePositions = new ArrayList<>();

        // 遍历主人附近获取安全的位置
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int dy = -verticalSearch; dy <= verticalSearch; dy++) {
                    BlockPos candidate = ownerPos.add(dx, dy, dz);
                    if (canTeleportTo(candidate)) {
                        safePositions.add(candidate);
                    }
                }
            }
        }

        // 如果没有安全的地方, 尝试传送到主人坐标最上方可站立的方块
        AAA:
        if (safePositions.isEmpty()) {
            int highestY = this.fox.world.getHeight(ownerPos.getX(), ownerPos.getZ());
            BlockPos highestPos = new BlockPos(ownerPos.getX(), highestY, ownerPos.getZ());

            // 遍历此范围获取安全的位置
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    for (int dy = -verticalSearch; dy <= verticalSearch; dy++) {
                        BlockPos candidate = highestPos.add(dx, dy, dz);
                        if (canTeleportTo(candidate)) {
                            safePositions.add(candidate);
                            break AAA; // 找到安全位置
                        }
                    }
                }
            }
        }

        // 如果有安全位置则随机选取
        if (!safePositions.isEmpty()) {
            BlockPos targetPos = safePositions.get(this.fox.getRandom().nextInt(safePositions.size()));
            if (this.fox.getDistanceSq(targetPos) > (minDist * minDist)) {
                this.fox.attemptTeleport(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
                this.petPathfinder.clearPath();
            }
        }
    }

// 检查是否可以传送到指定位置
    private boolean canTeleportTo(BlockPos pos) {
        // 检查底部方块是否可站立
        IBlockState downBlockState = this.fox.world.getBlockState(pos.down());
        AxisAlignedBB box = downBlockState.getCollisionBoundingBox(this.fox.world, pos.down());
        if (box == null 
            || (box.maxX - box.minX) != 1.0
            || (box.maxZ - box.minZ) != 1.0
            || box.maxY != 1.0) {
            return false;
        }

        // 检查是否是树叶且可能枯萎
        if (downBlockState.getBlock() instanceof BlockLeaves) {
            // 如果是原版树叶, 检查 CHECK_DECAY 和 DECAYABLE
            if (downBlockState.getProperties().containsKey(BlockLeaves.CHECK_DECAY)) {
                if (!downBlockState.getValue(BlockLeaves.CHECK_DECAY) && downBlockState.getValue(BlockLeaves.DECAYABLE)) {
                    return false; // 正在枯萎, 不能传送
                }
            }
            return false;
        }

        // 检查上方是否有火
        if (this.fox.world.getBlockState(pos.up()).getBlock() == Blocks.FIRE) {
            return false;
        }

        // 检查是否有足够的空间
        return this.fox.world.isAirBlock(pos) || this.fox.world.getBlockState(pos).getBlock() instanceof BlockSnow;
    }
}