package suike.suikefoxfriend;

import java.util.*;
import java.lang.reflect.*;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class SuiKe implements ModInitializer {
	// public static final String MOD_ID = "suikefoxfriend";
	// public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Random random = new Random();

	public static List<Item> foxNotPickupItemList = Lists.newArrayList(
        Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO, Items.SUSPICIOUS_STEW
    );

    @Override
    public void onInitialize() {

    }
}