package suike.suikefoxfriend.sound;

import suike.suikefoxfriend.SuiKe;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;

@Mod.EventBusSubscriber
public class ModSoundEvent {

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        // 狐狸音效
        registerSound(event, "entity.fox.ambient");  // 空闲
        registerSound(event, "entity.fox.aggro");    // 攻击生物
        registerSound(event, "entity.fox.bite");     // 咬住物品
        registerSound(event, "entity.fox.death");    // 死亡
        registerSound(event, "entity.fox.eat");      // 进食
        registerSound(event, "entity.fox.hurt");     // 受伤
        registerSound(event, "entity.fox.screech");  // 尖声叫
        registerSound(event, "entity.fox.sleep");    // 睡觉
        registerSound(event, "entity.fox.sniff");    // 检查甜浆果丛或洞穴藤蔓
        registerSound(event, "entity.fox.spit");     // 吐出物品
        registerSound(event, "entity.fox.teleport"); // 传送
    }

    private static void registerSound(RegistryEvent.Register<SoundEvent> event, String soundName) {
        ResourceLocation location = new ResourceLocation(SuiKe.MODID, soundName);
        SoundEvent soundEvent = new SoundEvent(location);

        soundEvent.setRegistryName(location);
        event.getRegistry().register(soundEvent);
    }
}