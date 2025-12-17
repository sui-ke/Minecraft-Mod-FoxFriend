package suike.suikefoxfriend;

import java.util.List;

import suike.suikefoxfriend.api.IOwnable;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class SuiKe implements ModInitializer {
	public static final String MOD_ID = "suikefoxfriend";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static List<Item> foxCanPickupItemList = Lists.newArrayList(
        Items.TOTEM_OF_UNDYING
    );
	public static List<Item> foxNotPickupItemList = Lists.newArrayList(
        Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO, Items.SUSPICIOUS_STEW
    );

    @Override
    public void onInitialize() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == Hand.MAIN_HAND && !world.isClient()) {
                // 检查是否是右键点击
				if (entity instanceof FoxEntity) {
                    IOwnable foxIOwnable = ((IOwnable) entity);
					Item handItem = player.getMainHandStack().getItem();
					if (foxIOwnable.isTamed() && handItem != Items.SWEET_BERRIES && handItem != Items.NAME_TAG) {
                        // 设置等待状态
						foxIOwnable.playerSetWaiting(player);
						return ActionResult.SUCCESS;

					} else if (!foxIOwnable.isTamed() && handItem == Items.SWEET_BERRIES) {
                        if (!player.getAbilities().creativeMode) {
                            player.getMainHandStack().decrement(1);
                        }

                        // 设为驯服
                        foxIOwnable.playerTamedFox(player);
						return ActionResult.SUCCESS;
                    }
				}
            }
            return ActionResult.PASS;
        });
    }
}