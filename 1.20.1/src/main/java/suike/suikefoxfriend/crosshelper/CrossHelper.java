package suike.suikefoxfriend.crosshelper;

import net.minecraft.world.World;
import net.minecraft.entity.Entity;

public class CrossHelper {
    public static final World getWorld(Entity entity) {
        return entity.getWorld();
    }
}