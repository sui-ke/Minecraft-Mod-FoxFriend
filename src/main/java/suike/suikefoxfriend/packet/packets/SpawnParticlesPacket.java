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

public class SpawnParticlesPacket implements IMessage {
    private int entityID;
    private int particleType;

    public SpawnParticlesPacket() {}

    public SpawnParticlesPacket(int entityID, int particleType) {
        this.entityID = entityID;
        this.particleType = particleType;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityID);
        buf.writeInt(particleType);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
        particleType = buf.readInt();
    }

    // 客户端处理类: 处理服务器发来的包
    @SideOnly(Side.CLIENT)
    public static class ClientHandler implements IMessageHandler<SpawnParticlesPacket, IMessage> {
        @Override
        public IMessage onMessage(SpawnParticlesPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                switch (message.particleType) {
                case 0:
                    ModParticle.spawnParticlesLave(world, message.entityID); break;
                case 1:
                    ModParticle.spawnParticlesTeleport(world, message.entityID); break;
                default:
                    break;
                }
            });
            return null;
        }
    }

    // 服务端处理类: 处理客户端发来的包
    @SideOnly(Side.SERVER)
    public static class ServerHandler implements IMessageHandler<SpawnParticlesPacket, IMessage> {
        @Override
        public IMessage onMessage(SpawnParticlesPacket message, MessageContext ctx) {
            return null;
        }
    }
}