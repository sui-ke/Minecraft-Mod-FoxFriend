package suike.suikefoxfriend.packet;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.packet.packets.*;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(SuiKe.MODID);

    public static void registerClientPackets() {
        // 服务器 → 客户端的包 // 客户端处理的包
        INSTANCE.registerMessage(SpawnFoodParticlesPacket.ClientHandler.class, SpawnFoodParticlesPacket.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(SpawnParticlesPacket.ClientHandler.class, SpawnParticlesPacket.class, 1, Side.CLIENT);
    }

    public static void registerServerPackets() {
        // 客户端 → 服务器的包 // 服务器处理的包
        INSTANCE.registerMessage(SpawnFoodParticlesPacket.ServerHandler.class, SpawnFoodParticlesPacket.class, 0, Side.SERVER);
        INSTANCE.registerMessage(SpawnParticlesPacket.ServerHandler.class, SpawnParticlesPacket.class, 1, Side.SERVER);
    }
}