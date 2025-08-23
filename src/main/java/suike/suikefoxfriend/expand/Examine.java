package suike.suikefoxfriend.expand;

import net.minecraftforge.fml.common.Loader;

public class Examine {
    public static boolean FuturemcID;

    public static void examine() {
        FuturemcID = Loader.isModLoaded("futuremc");
    }
}