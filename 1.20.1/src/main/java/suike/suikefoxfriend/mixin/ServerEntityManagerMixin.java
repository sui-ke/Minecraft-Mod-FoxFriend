package suike.suikefoxfriend.mixin;

import suike.suikefoxfriend.api.IFoxTamed;

import net.minecraft.text.*;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerEntityManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntityManager.class)
public abstract class ServerEntityManagerMixin {
    private static final Text LOSE = Text.of("§cFox Lose!!!");

    @Inject(method = "unload(Lnet/minecraft/world/entity/EntityLike;)V", at = @At("HEAD"), cancellable = true)
    private void onUnload(EntityLike entity, CallbackInfo ci) {
        if (entity instanceof FoxEntity) {
            IFoxTamed ifox = (IFoxTamed) entity;
            if (ifox.isTamed() && !ifox.isWaiting()) {
                PlayerEntity player = (PlayerEntity) ifox.getOwner();
                if (player != null) {
                    if (ifox.getFollowGoal().teleport()) {
                        // 传送成功取消卸载
                        ci.cancel();
                    } else {
                        // 传送失败发送消息
                        BlockPos pos = entity.getBlockPos();
                        player.sendMessage(LOSE, false);
                        player.sendMessage(Text.of("§eFox Pos: " + pos.getX() + ", "+ pos.getY() + ", "+ pos.getZ()), false);
                    }
                }
            }
        }
    }
}