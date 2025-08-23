package suike.suikefoxfriend.item;

import java.util.List;
import java.util.ArrayList;

import suike.suikefoxfriend.SuiKe;
import suike.suikefoxfriend.entity.ModEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class ItemBase {

    public static final ModItemEntityEgg FOX_EGG = new ModItemEntityEgg("fox_spawn_egg", ModEntity.entityFox);

    // 注册物品
    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(FOX_EGG);
    }

    // 注册模型
    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(FOX_EGG, 0, new ModelResourceLocation(FOX_EGG.getRegistryName(), "inventory"));
    }
}