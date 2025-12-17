package suike.suikefoxfriend.entity.ai;

import java.util.Random;
import java.util.EnumSet;

import suike.suikefoxfriend.api.IOwnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

public class FoxFollowOwnerGoal extends Goal {
    private final FoxEntity fox;
    private final World world;
    private final double speed = 1.0D;
    private final float minDistance = 10.0F;
    private final float maxDistance = 20.0F;
    private LivingEntity owner;
    private int updateCountdownTicks;

    public FoxFollowOwnerGoal(FoxEntity fox) {
        this.fox = fox;
        this.world = fox.getEntityWorld();
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity owner = ((IOwnable) this.fox).getOwner();
        if (owner == null || owner.isSpectator() || this.cannotFollow()) {
            return false;
        } else if (this.fox.squaredDistanceTo(owner) < (double) (this.minDistance * this.minDistance)) {
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
        } else if (this.fox.squaredDistanceTo(((IOwnable) this.fox).getOwner()) <= (double) (this.maxDistance * this.maxDistance)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean cannotFollow() {
        return ((IOwnable) this.fox).isWaiting(); 
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
        if (this.owner == null) {
            return;
        }
        // 看向主人
        this.fox.getLookControl().lookAt(this.owner, 10.0F, (float) this.fox.getMaxLookPitchChange());

        // 是否传送到主人
        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = 10;
            if (this.fox.squaredDistanceTo(this.owner) >= 144.0D) {
                this.tryTeleport();
            } else {
                this.fox.getNavigation().startMovingTo(this.owner, this.speed);
            }
        }
    }

    public boolean teleport() {
        this.owner = ((IOwnable) this.fox).getOwner();;
        if (owner != null && this.fox.squaredDistanceTo(this.owner) >= 144.0D) {
            this.tryTeleport();
            this.owner = null;
            return true;
        }
        this.owner = null;
        return false;
    }

    private void tryTeleport() {
        BlockPos ownerPos = this.owner.getBlockPos();
        for (int i = 0; i < 10; ++i) {
            int x = ownerPos.getX() + this.getRandomInt(-3, 3);
            int y = ownerPos.getY() + this.getRandomInt(-1, 1);
            int z = ownerPos.getZ() + this.getRandomInt(-3, 3);
            if (this.tryTeleportTo(x, y, z)) {
                return;
            }
        }
    }

    private boolean tryTeleportTo(int x, int y, int z) {
        if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.fox.refreshPositionAndAngles((double) x + 0.5D, (double) y, (double) z + 0.5D, this.fox.getYaw(), this.fox.getPitch());
            this.fox.getNavigation().stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this.fox, pos.mutableCopy());
        if (pathNodeType != PathNodeType.WALKABLE) {
            return false;
        } else {
            BlockState blockState = this.world.getBlockState(pos.down());
            if (blockState.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockPos = pos.subtract(this.fox.getBlockPos());
                return this.world.isSpaceEmpty(this.fox, this.fox.getBoundingBox().offset(blockPos));
            }
        }
    }

    private static Random random = new Random();
    private int getRandomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}