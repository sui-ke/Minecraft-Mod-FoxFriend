package suike.suikefoxfriend.entity.ai;

import java.util.*;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.api.IFoxTamed;
import suike.suikefoxfriend.crosshelper.CrossHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap;
import net.minecraft.util.math.BlockPos;

public class FoxFollowOwnerGoal extends Goal {

    private static final double MIN_DISTANCE = 10.0D * 10.0D; // 最小距离
    private static final double MAX_DISTANCE = 12.0D * 12.0D; // 最大距离
    private static final double TP_DISTANCE = 14.0D * 14.0D;  // 传送距离阈值
    private static final int TP_TARGET_RADIUS = 2;            // 传送搜索半径

    private final World world;
    private final FoxEntity fox;
    private final IFoxTamed ifox;
    private LivingEntity owner;
    private int updateCountdownTicks;

    public FoxFollowOwnerGoal(FoxEntity fox) {
        this.fox = fox;
        this.ifox = (IFoxTamed) fox;
        this.world = CrossHelper.getWorld(fox);
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!this.ifox.isTamed()) return false;

        LivingEntity owner = this.ifox.getOwner();
        if (owner == null || owner.isSpectator() || this.cannotFollow()) {
            return false;
        } else if (this.fox.squaredDistanceTo(owner) < MIN_DISTANCE) {
            return false;
        } else {
            this.owner = owner;
            return true;
        }
    }

    @Override
    public boolean shouldContinue() {
        if (this.cannotFollow()) {
            return false;
        } else if (this.fox.squaredDistanceTo(this.ifox.getOwner()) <= MAX_DISTANCE) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.fox.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.owner == null) return;

        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = 10;
            // 看向主人
            this.fox.getLookControl().lookAt(this.owner, 10.0F, (float) this.fox.getMaxLookPitchChange());
            // 传送或移动到主人
            if (this.fox.squaredDistanceTo(this.owner) > TP_DISTANCE) {
                this.tryTeleport();
            } else {
                this.fox.getNavigation().startMovingTo(this.owner, 1.0D);
            }
        }
    }

    // 外部调用
    public boolean teleport() {
        this.owner = this.ifox.getOwner();
        if (owner != null && this.fox.squaredDistanceTo(this.owner) > TP_DISTANCE) {
            boolean success = this.tryTeleport();
            this.owner = null;
            return success;
        }
        this.owner = null;
        return false;
    }

    // 尝试传送到主人
    private boolean tryTeleport() {
        BlockPos pos = this.owner.getBlockPos();
        List<BlockPos> list = this.getTpList(pos);
        if (list.isEmpty()) {
            int x = pos.getX();
            int z = pos.getZ();
            list = this.getTpList(new BlockPos(x, CrossHelper.getWorld(this.owner).getTopY(Heightmap.Type.WORLD_SURFACE, x, z), z));
        }
        if (list.isEmpty()) return false;

        pos = list.get(SuiKe.random.nextInt(list.size()));
        this.teleportTo(pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    // 获取传送位置列表
    private List<BlockPos> getTpList(BlockPos pos) {
        List<BlockPos> list = new ArrayList<>();
        for (int x = -TP_TARGET_RADIUS; x <= TP_TARGET_RADIUS; x++) {
            int targetX = pos.getX() + x;
            for (int y = -TP_TARGET_RADIUS; y <= TP_TARGET_RADIUS; y++) {
                int targetY = pos.getY() + y;
                for (int z = -TP_TARGET_RADIUS; z <= TP_TARGET_RADIUS; z++) {
                    int targetZ = pos.getZ() + z;
                    if (this.canTeleportTo(targetX, targetY, targetZ)) {
                        list.add(new BlockPos(targetX, targetY, targetZ));
                    }
                }
            }
        }
        return list;
    }

    // 执行传送
    private void teleportTo(int x, int y, int z) {
        this.fox.refreshPositionAndAngles((double) x + 0.5D, (double) y, (double) z + 0.5D, this.fox.getYaw(), this.fox.getPitch());
        this.fox.getNavigation().stop();
    }

    // 检查位置能否传送
    private boolean canTeleportTo(int x, int y, int z) {
        if (this.closeDistance(this.fox.getX(), this.fox.getY(), this.fox.getZ(), this.owner)) {
            return false;
        } else if (this.closeDistance(x, y, z, this.fox)) {
            return false;
        }
        return this.canTeleportTo(new BlockPos(x, y, z));
    }

    // 检查位置与实体距离, 过小则无效
    private boolean closeDistance(double x, double y, double z, LivingEntity entity) {
        return Math.abs(x - entity.getX()) < 5.0D
            && Math.abs(y - entity.getY()) < 5.0D
            && Math.abs(z - entity.getZ()) < 5.0D;
    }

    // 检查位置是否安全
    private boolean canTeleportTo(BlockPos pos) {
        BlockPos downPos = pos.down();
        BlockState blockState = this.world.getBlockState(downPos);
        if (blockState.getCollisionShape(this.world, downPos).isEmpty()) {
            return false;
        } else {
            return this.world.isSpaceEmpty(this.fox, this.fox.getBoundingBox().offset(pos.subtract(this.fox.getBlockPos())));
        }
    }

    private boolean cannotFollow() {
        return this.ifox.isWaiting();
    }
}