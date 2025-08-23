package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import suike.suikefoxfriend.sound.Sound;

import thedarkcolour.futuremc.block.villagepillage.SweetBerryBushBlock;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;

public class FoxMoveToBushAI extends EntityAIBase {
    private final FoxEntity fox;
    private final double speed;
    private final int range;
    private BlockPos targetBushPos;
    private int delayCounter;
    private int sniffTimer = -1;
    private static final int SNIFF_DELAY = 20;

    public FoxMoveToBushAI(FoxEntity fox, double speed, int range) {
        this.fox = fox;
        this.speed = speed;
        this.range = range;
        this.setMutexBits(1);
    }

    // 是否开始执行
    @Override
    public boolean shouldExecute() {
        if (this.fox.handItemIsFood()) return false;

        this.targetBushPos = this.findNearestSweetBerryBush();
        return targetBushPos != null;
    }

    private BlockPos findNearestSweetBerryBush() {
        World world = this.fox.world;
        BlockPos foxPos = this.fox.getPos();

        List<BlockPos> potentialBushes = StreamSupport.stream(
                BlockPos.getAllInBox(
                    foxPos.add(-this.range, -2, -this.range),
                    foxPos.add(this.range, 2, this.range)).spliterator(), 
                false)
            .filter(pos -> isSweetBerryBush(world.getBlockState(pos)))
            .collect(Collectors.toList());

        return potentialBushes.stream()
            .filter(pos -> world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) >= 2)
            .min(Comparator.comparingDouble(pos -> this.fox.getDistanceSq(pos.getX(), pos.getY(), pos.getZ())))
            .orElse(null);
    }

    private boolean isSweetBerryBush(IBlockState state) {
        return state.getBlock() instanceof SweetBerryBushBlock;
    }

    // 是否继续执行
    @Override
    public boolean shouldContinueExecuting() {
        return this.targetBushPos != null && 
               isSweetBerryBush(this.fox.world.getBlockState(this.targetBushPos)) &&
               this.fox.world.getBlockState(this.targetBushPos).getBlock().getMetaFromState(this.fox.world.getBlockState(this.targetBushPos)) >= 2;
    }

    @Override
    public void startExecuting() {
        this.delayCounter = 0;
    }

    @Override
    public void updateTask() {
        if (this.targetBushPos == null) return;

        if (--this.delayCounter <= 0) {
            this.delayCounter = 10;
            this.fox.getNavigator().tryMoveToXYZ(
                this.targetBushPos.getX() + 0.5,
                this.targetBushPos.getY(),
                this.targetBushPos.getZ() + 0.5,
                speed
            );
        }

        if (this.fox.getDistanceSq(this.targetBushPos.getX(), this.targetBushPos.getY(), this.targetBushPos.getZ()) < 2.25) {
            if (sniffTimer < 0) {
                // 第一次接近，播放嗅探声音并开始计时
                Sound.playSound(this.fox.world, this.fox.getPos(), "entity.fox.sniff");
                sniffTimer = SNIFF_DELAY;
            } else if (sniffTimer == 0) {
                // 计时结束，采摘浆果
                this.pickBerries(this.fox.world, this.targetBushPos);
                this.resetTask();
            } else {
                // 等待期间减少计时器
                sniffTimer--;

                // 让狐狸面朝浆果丛
                this.fox.getLookHelper().setLookPosition(
                    this.targetBushPos.getX() + 0.5,
                    this.targetBushPos.getY(),
                    this.targetBushPos.getZ() + 0.5,
                    10.0F,
                    this.fox.getVerticalFaceSpeed()
                );
            }
        } else {
            // 如果离开浆果丛，重置计时器
            sniffTimer = -1;
        }
    }

    public static void pickBerries(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!(block instanceof SweetBerryBushBlock)) return;
        int age = block.getMetaFromState(state);

        if (age >= 2) {
            int berryCount = age == 3 ? 2 + world.rand.nextInt(2) : 1 + world.rand.nextInt(2);

            world.setBlockState(pos, block.getStateFromMeta(1));
            Block.spawnAsEntity(world, pos, new ItemStack(Item.getByNameOrId("futuremc:sweet_berries"), berryCount));
            Sound.playSound(world, pos, "futuremc", "item.sweet_berries.pick_from_bush");
        }
    }

    @Override
    public void resetTask() {
        this.targetBushPos = null;
        this.sniffTimer = -1;
        this.fox.getNavigator().clearPath();
    }
}