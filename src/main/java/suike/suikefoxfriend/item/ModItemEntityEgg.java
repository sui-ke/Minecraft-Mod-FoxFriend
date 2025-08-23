package suike.suikefoxfriend.item;

import java.util.List;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityAnimal;

// 生物蛋
public class ModItemEntityEgg extends Item {
    public ModItemEntityEgg(String name, Class<FoxEntity> entityClass) {
        /*设置物品名*/this.setRegistryName(name);
        /*设置物品名key*/this.setUnlocalizedName(SuiKe.MODID + "." + name);
        /*设置创造模式物品栏*/this.setCreativeTab(CreativeTabs.MISC);
        this.setEntityClass(entityClass);
    }

    private Class<FoxEntity> entityClass;
    private void setEntityClass(Class<FoxEntity> entityClass) {
        this.entityClass = entityClass;
    }

// 使用物品
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);

        // 计算视线轨迹
        Vec3d eyePos = new Vec3d(
            player.posX, 
            player.posY + player.getEyeHeight(), 
            player.posZ
        );
        Vec3d lookVec = player.getLookVec();
        Vec3d endPos = eyePos.add(lookVec.scale(5.0D)); // 最大射程5格

        // 执行方块级射线追踪
        RayTraceResult rayTrace = world.rayTraceBlocks(eyePos, endPos, true);
        if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, itemStack);
        }

        // 计算生成位置 - 修改后的部分
        BlockPos blockPos = rayTrace.getBlockPos();
        Vec3d spawnPos;

        // 计算生成位置
        if (rayTrace.sideHit == EnumFacing.UP) {
            // 点击方块上面，在方块上方生成
            spawnPos = new Vec3d(
                blockPos.getX() + 0.5,
                blockPos.getY() + 1.0,
                blockPos.getZ() + 0.5
            );
        } else if (rayTrace.sideHit == EnumFacing.DOWN) {
            // 点击方块下面，在方块下方生成
            spawnPos = new Vec3d(
                blockPos.getX() + 0.5,
                blockPos.getY() - 1.0 + 1.0, // 减去1.0然后加上实体高度
                blockPos.getZ() + 0.5
            );
        } else {
            // 点击侧面，在相邻方块中心生成
            BlockPos adjacentPos = blockPos.offset(rayTrace.sideHit);
            spawnPos = new Vec3d(
                adjacentPos.getX() + 0.5,
                adjacentPos.getY() + 0.5,
                adjacentPos.getZ() + 0.5
            );
        }

        // 创建生物实体
        FoxEntity fox;
        try {
            fox = this.entityClass.getConstructor(World.class).newInstance(world);
        } catch (Exception e) {
            e.printStackTrace();
            return new ActionResult<>(EnumActionResult.FAIL, itemStack);
        }

        fox.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
        fox.onInitialSpawn(null, null);

        // 服务器端生成实体
        if (!world.isRemote) {
            world.spawnEntity(fox);
            if (!player.field_71075_bZ.isCreativeMode) {
                itemStack.shrink(1);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }
}