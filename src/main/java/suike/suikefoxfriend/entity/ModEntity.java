package suike.suikefoxfriend.entity;

import java.util.Objects;
import java.util.stream.Stream;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.entity.fox.FoxEntity;
import suike.suikefoxfriend.client.render.entity.fox.FoxRenderFactory;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;

@Mod.EventBusSubscriber
public class ModEntity {

    public static final Class<FoxEntity> entityFox = FoxEntity.class;

    // 注册实体
    @SubscribeEvent
    public static void onRegisterEntity(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(EntityEntryBuilder.create().entity(entityFox).id(new ResourceLocation(SuiKe.MODID, "fox"), 0).name("fox").tracker(80, 3, true).build());

        Biome[] taigaBiomes = Stream.of(
            Biomes.TAIGA,
            Biomes.TAIGA_HILLS,
            Biomes.COLD_TAIGA,
            Biomes.COLD_TAIGA_HILLS,
            Biomes.MUTATED_TAIGA,
            Biomes.MUTATED_TAIGA_COLD
        ).filter(Objects::nonNull).toArray(Biome[]::new);

        EntityRegistry.addSpawn(
            entityFox,
            40, // 生成权重
            2, // 最小生成数量
            5, // 最大生成数量
            EnumCreatureType.CREATURE, // 生物类型
            taigaBiomes // 目标生物群系
        );
    }

    // 注册实体渲染
    @SubscribeEvent
    public static void registryModel(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(entityFox, new FoxRenderFactory());
    }
}