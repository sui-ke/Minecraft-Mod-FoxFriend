package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.ai.EntityAIWatchClosest;

public class FoxWatchPlayerAI extends EntityAIWatchClosest {
    private final FoxEntity fox;

    public FoxWatchPlayerAI(FoxEntity fox) {
        super(fox, EntityPlayer.class, 5.0F);
        this.fox = fox;
    }

    @Override
    public boolean shouldExecute() {
        if (this.fox.isSleeping()) {
            return false;
        }
        return super.shouldExecute();
    }
}