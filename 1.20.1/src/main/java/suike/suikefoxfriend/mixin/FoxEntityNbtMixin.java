package suike.suikefoxfriend.mixin;

import java.util.*;

import suike.suikefoxfriend.api.IFoxTamed;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoxEntity.class)
public abstract class FoxEntityNbtMixin {
    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        IFoxTamed ifox = (IFoxTamed) this;
        if (ifox.isTamed()) {
            nbt.putBoolean("Waiting", ifox.isWaiting());
            nbt.putString("Owner", ifox.getOwnerUuid().toString());
            if (ifox.hasOwnerGiveItem()) {
                nbt.putBoolean("HasOwnerGiveItem", true);
            }
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        UUID ownerUuid = getUuidFromNbt(nbt, "Owner");
        if (ownerUuid != null) {
            IFoxTamed ifox = (IFoxTamed) this;
            ifox.setOwnerUuid(ownerUuid);

            ifox.mixinStopActions();
            this.getFox().setPersistent();
            ifox.setOwnerGiveItem(getBooleanFromNbt(nbt, "HasOwnerGiveItem"));
            if (getBooleanFromNbt(nbt, "Waiting")) {
                ifox.setSleepingWaiting(getBooleanFromNbt(nbt, "Sleeping"));
                ifox.setWaiting(true);
            }
        }
    }

    // 兼容新旧版本的 UUID
    private static final UUID getUuidFromNbt(NbtCompound nbt, String name) {
        NbtElement element = nbt.get(name);
        if (element == null) return null;

        if (element instanceof NbtString) {
            String ownerUuidString = ((NbtString) element).toString().replace("\"", "");
            if (ownerUuidString != null) {
                try {
                    return UUID.fromString(ownerUuidString);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        else if (element instanceof NbtIntArray) {
            int[] uuidInts = ((NbtIntArray) element).getIntArray();
            if (uuidInts.length == 4) {
                return new UUID((long) uuidInts[0] << 32 | uuidInts[1] & 0xFFFFFFFFL,
                                (long) uuidInts[2] << 32 | uuidInts[3] & 0xFFFFFFFFL);
            }
        }

        return null;
    }

    // 兼容不同版本的 getBoolean 返回值
    private static final boolean getBooleanFromNbt(NbtCompound nbt, String name) {
        Object element = nbt.getBoolean(name);
        if (element instanceof Boolean) {
            return (boolean) element;
        }
        else if (element instanceof Optional) {
            return ((Optional<Boolean>) element).orElse(false);
        }
        return false;
    }

    private FoxEntity getFox() {
        return (FoxEntity) (Object) this;
    }
}