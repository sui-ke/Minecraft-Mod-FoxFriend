package suike.suikefoxfriend.packet.packets;

import suike.suikefoxfriend.particle.ModParticle;

import net.minecraft.world.World;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import io.netty.buffer.ByteBuf;

public class SpawnFoodParticlesPacket implements IMessage {
    private int entityID;
    private String foodName;

    public SpawnFoodParticlesPacket() {}

    public SpawnFoodParticlesPacket(int entityID, String foodName) {
        this.entityID = entityID;
        this.foodName = foodName;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityID);
        ByteBufUtils.writeUTF8String(buf, foodName);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
        foodName = ByteBufUtils.readUTF8String(buf);
    }

    // 客户端处理类: 处理服务器发来的包
    @SideOnly(Side.CLIENT)
    public static class ClientHandler implements IMessageHandler<SpawnFoodParticlesPacket, IMessage> {
        @Override
        public IMessage onMessage(SpawnFoodParticlesPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                ModParticle.spawnParticlesEatFood(world, message.entityID, message.foodName);
            });
            return null;
        }
    }

    // 服务端处理类: 处理客户端发来的包
    @SideOnly(Side.SERVER)
    public static class ServerHandler implements IMessageHandler<SpawnFoodParticlesPacket, IMessage> {
        @Override
        public IMessage onMessage(SpawnFoodParticlesPacket message, MessageContext ctx) {
            return null;
        }
    }
}