package suike.suikefoxfriend.client.render.entity.fox;

import suike.suikefoxfriend.entity.fox.FoxEntity;
import suike.suikefoxfriend.entity.fox.FoxEntity.State;
import suike.suikefoxfriend.client.render.entity.fox.animation.*;

import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelBase;

public class FoxModelBase extends ModelBase {

    private static final FoxModel ADULT_MODEL_IDLING = new Idling(0);
    private static final FoxModel ADULT_MODEL_SITTING = new Sitting(0);
    private static final FoxModel ADULT_MODEL_SLEEPING = new Sleeping(0);
    // private static final FoxModel ADULT_MODEL_ATTACKING = new Attacking();

    private static final FoxModel CHILD_MODEL_IDLING = new Idling(true);
    private static final FoxModel CHILD_MODEL_SITTING = new Sitting(true);
    private static final FoxModel CHILD_MODEL_SLEEPING = new Sleeping(true);

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        FoxEntity fox = (FoxEntity) entity;
        State state = fox.getState();

        FoxModel model = fox.isChild()
            ? this.getChildModel(state)
            : /*fox.isJumpAttack() ? ADULT_MODEL_ATTACKING : */this.getAdultModel(state);

        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    private FoxModel getAdultModel(State state) {
        switch (state) {
            case SITTING:
                return ADULT_MODEL_SITTING; 
            case SLEEPING:
                return ADULT_MODEL_SLEEPING;
            default:
                return ADULT_MODEL_IDLING;
        }
    }

    private FoxModel getChildModel(State state) {
        switch (state) {
            case SITTING:
                return CHILD_MODEL_SITTING;
            case SLEEPING:
                return CHILD_MODEL_SLEEPING;
            default:
                return CHILD_MODEL_IDLING;
        }
    }
}