package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import net.minecraft.entity.ai.EntityAIPanic;

public class FoxPanicAI extends EntityAIPanic {
    private final FoxEntity fox;

    public FoxPanicAI(FoxEntity fox) {
        super(fox, 0.8D);
        this.fox = fox;
    }

    @Override
    public boolean shouldExecute() {
        if (this.fox.canAttacking()) {
            return false;
        }
        return super.shouldExecute();
    }
}