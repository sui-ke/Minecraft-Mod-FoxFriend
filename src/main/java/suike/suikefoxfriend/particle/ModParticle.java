package suike.suikefoxfriend.particle;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumParticleTypes;

public class ModParticle {
    public static void spawnParticlesLave(World world, int entityID) {
        BlockPos pos = ((FoxEntity) world.getEntityByID(entityID)).getPos();

        int a = world.rand.nextInt(4) + 4;
        for (int i = 0; i < a; ++i) {
            double x = (double) ((float) pos.getX() + world.rand.nextFloat());
            double y = (double) ((float) pos.getY() + world.rand.nextFloat());
            double z = (double) ((float) pos.getZ() + world.rand.nextFloat());

            double d0 = world.rand.nextGaussian() * 0.02D;
            double d1 = world.rand.nextGaussian() * 0.02D;
            double d2 = world.rand.nextGaussian() * 0.02D;

            world.spawnParticle(
                EnumParticleTypes.HEART,
                x, y, z,
                d0, d1, d2
            );
        }
    }

    public static void spawnParticlesVillagerHappy(World world, BlockPos pos) {
        int a = world.rand.nextInt(4) + 6;
        for (int i = 0; i < a; ++i) {
            double x = (double) ((float) pos.getX() + world.rand.nextFloat());
            double y = (double) ((float) pos.getY() + world.rand.nextFloat());
            double z = (double) ((float) pos.getZ() + world.rand.nextFloat());

            double d0 = world.rand.nextGaussian() * 0.02D;
            double d1 = world.rand.nextGaussian() * 0.02D;
            double d2 = world.rand.nextGaussian() * 0.02D;

            world.spawnParticle(
                EnumParticleTypes.VILLAGER_HAPPY,
                x, y, z,
                d0, d1, d2
            );
        }
    }

    public static void spawnParticlesEatFood(World world, int entityID, String foodName) {
        Entity entity = world.getEntityByID(entityID);
        if (!(entity instanceof FoxEntity)) return;

        Item item = Item.getByNameOrId(foodName);
        if (item == null)  return;

        Vec3d nosePos = ((FoxEntity) entity).getNosePosition();

        for (int i = 0; i < 12; i++) {
            double ox = world.rand.nextGaussian() * 0.06D;
            double oy = world.rand.nextGaussian() * 0.06D;
            double oz = world.rand.nextGaussian() * 0.06D;

            world.spawnParticle(
                EnumParticleTypes.ITEM_CRACK,
                nosePos.x, nosePos.y, nosePos.z,
                ox, oy, oz,
                Item.getIdFromItem(item)
            );
        }
    }

    public static void spawnParticlesTeleport(World world, int entityID) {
        Entity entity = world.getEntityByID(entityID);
        if (!(entity instanceof FoxEntity)) return;

        BlockPos pos = ((FoxEntity) entity).getPos();

        final double radius = 0.45;
        final int particlesPerCircle = 18;

        // 生成5圈粒子
        for (int circle = 0; circle < 5; circle++) {
            for (int i = 0; i < particlesPerCircle; i++) {
                double angle = 2 * Math.PI * i / particlesPerCircle;
                double x = pos.getX() + 0.5 + radius * Math.cos(angle);
                double z = pos.getZ() + 0.5 + radius * Math.sin(angle);
                double y = pos.getY() + 0.8;

                TeleportParticle.spawnParticle(world, x, y, z, circle);
            }
        }
    }
}