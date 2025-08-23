package suike.suikefoxfriend.entity.fox.ai;

import suike.suikefoxfriend.entity.fox.FoxEntity;

import java.util.List;
import java.util.Comparator;

import net.minecraft.item.ItemFood;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;

public class FoxSurveyItemAI extends EntityAIBase {
    private final FoxEntity fox;
    private final float searchRange;

    public FoxSurveyItemAI(FoxEntity fox, float range) {
        this.fox = fox;
        this.searchRange = range;
    }

    @Override
    public boolean shouldExecute() {
        if (this.fox.isSleeping() || this.fox.hasOwnerGiveItem()) return false;

        // 手中没有物品时执行 || 手中物品不为食物
        if (this.fox.getHandItem().isEmpty() || !this.fox.handItemIsFood()) {
            // 没有目标物品 || 目标物品无效时执行
            if (this.fox.targetItem == null || !this.fox.targetItem.isEntityAlive()) {
                EntityItem targetItem = this.findNearbyItem();
                if (this.fox.isValidTargetItem(targetItem)) {
                    this.fox.targetItem = targetItem;
                    this.fox.setIdling();
                }
            }
        }
        return false;
    }

    private EntityItem findNearbyItem() {
        List<EntityItem> items = this.fox.world.getEntitiesWithinAABB(
            EntityItem.class,
            this.fox.getEntityBoundingBox().grow(this.searchRange),
            item -> item != null && !item.getItem().isEmpty() && this.fox.isValidTargetItem(item)
        );

        return items.stream()
            .min(Comparator.comparingDouble(item -> this.fox.getDistanceSq(item)))
            .orElse(null);
    }
}