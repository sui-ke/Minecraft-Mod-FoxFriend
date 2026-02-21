package suike.suikefoxfriend.mixin;

import java.util.*;

import suike.suikefoxfriend.api.IFoxTamed;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoxEntity.class)
public abstract class FoxEntityNbtMixin {
    @Inject(method = "writeCustomData", at = @At("RETURN"))
    private void onWriteCustomDataToNbt(WriteView nbt, CallbackInfo ci) {
        IFoxTamed ifox = (IFoxTamed) this;
        if (ifox.isTamed()) {
            nbt.putBoolean("Waiting", ifox.isWaiting());
            nbt.putString("Owner", ifox.getOwnerUuid().toString());
            if (ifox.hasOwnerGiveItem()) {
                nbt.putBoolean("HasOwnerGiveItem", true);
            }
        }
    }

    @Inject(method = "readCustomData", at = @At("RETURN"))
    private void onReadCustomDataFromNbt(ReadView nbt, CallbackInfo ci) {
        UUID ownerUuid = getUuidFromNbt(nbt, "Owner");
        if (ownerUuid != null) {
            IFoxTamed ifox = (IFoxTamed) this;
            ifox.setOwnerUuid(ownerUuid);

            ifox.mixinStopActions();
            this.getFox().setPersistent();
            ifox.setOwnerGiveItem(nbt.getBoolean("HasOwnerGiveItem", false));
            if (nbt.getBoolean("Waiting", false)) {
                ifox.setSleepingWaiting(nbt.getBoolean("Sleeping", false));
                ifox.setWaiting(true);
            }
        }
    }

    private static final UUID getUuidFromNbt(ReadView nbt, String name) {
        Optional<String> ownerUuidStringOpt = nbt.getOptionalString(name);
        if (ownerUuidStringOpt.isPresent()) {
            String ownerUuidString = ownerUuidStringOpt.get();
            try {
                return UUID.fromString(ownerUuidString);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        Optional<int[]> uuidIntsOpt = nbt.getOptionalIntArray(name);
        if (uuidIntsOpt.isPresent()) {
            int[] uuidInts = uuidIntsOpt.get();
            if (uuidInts.length == 4) {
                return new UUID((long) uuidInts[0] << 32 | uuidInts[1] & 0xFFFFFFFFL,
                                (long) uuidInts[2] << 32 | uuidInts[3] & 0xFFFFFFFFL);
            }
        }

        return null;
    }

    private FoxEntity getFox() {
        return (FoxEntity) (Object) this;
    }
}