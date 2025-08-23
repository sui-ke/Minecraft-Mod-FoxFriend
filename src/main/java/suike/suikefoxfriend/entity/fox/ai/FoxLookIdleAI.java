package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.entity.ai.EntityAILookIdle;

public class FoxLookIdleAI extends EntityAILookIdle {
    private final FoxEntity fox;

    public FoxLookIdleAI(FoxEntity fox) {
        super(fox);
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