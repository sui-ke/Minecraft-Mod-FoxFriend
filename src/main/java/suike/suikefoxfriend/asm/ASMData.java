package suike.suikefoxfriend.asm;

import java.util.Map;
import java.util.HashMap;
import java.util.function.*;

import suike.suikefoxfriend.asm.*;
import suike.suikefoxfriend.asm.advice.*;
import suike.suikefoxfriend.inter.IMethod;
import suike.suikefoxfriend.expand.Examine;

import org.objectweb.asm.*;

public class ASMData {
    private static Map<String, ASMData> ASM_DATA;

    public final String transformed;
    public final int classWriterType;
    public final int classReaderType;
    public final Function<AdviceAdapterData, IMethod> createMethod;

    public ASMData(String transformed, int classWriterType, int classReaderType, Function<AdviceAdapterData, IMethod> createMethod) {
        this.transformed = transformed;
        this.classWriterType = classWriterType;
        this.classReaderType = classReaderType;
        this.createMethod = createMethod;
    }

    public String getName() {
        for (int i = this.transformed.length() - 1; i >= 0; i--) {
            if (this.transformed.charAt(i) == '.') {
                return this.transformed.substring(i + 1);
            }
        }
        return this.transformed;
    }

    public static ASMData getDataByName(String transformedName) {
        return ASM_DATA.get(normalizeClassName(transformedName));
    }
    public static String normalizeClassName(String transformedName) {
        // 从末尾反向查找 '@' 或 '.'
        for (int i = transformedName.length() - 1; i >= 0; i--) {
            char c = transformedName.charAt(i);
            if (c == '@') {
                return transformedName.substring(0, i); // 去除内存地址
            }
            if (c == '.') {
                return transformedName; // 无内存地址
            }
        }
        return transformedName; // 无内存地址
    }

    private static void addMap(ASMData data, boolean shouldAdd, Map<String, ASMData> map) {
        if (shouldAdd) map.put(data.transformed, data);
    }
    private static void addMap(ASMData data, Supplier<Boolean> shouldAdd, Map<String, ASMData> map) {
        if (shouldAdd.get()) map.put(data.transformed, data);
    }

    public static void initUnconditionalMap() {
        Map<String, ASMData> map = new HashMap<>();

        addMap(
            new ASMData(
                "net.minecraft.entity.passive.EntityWolf",
                ClassWriter.COMPUTE_FRAMES, ClassReader.EXPAND_FRAMES,
                data -> new EntityAttackableFoxMethod(data)
            ),
            true, map
        );
        addMap(
            new ASMData(
                "net.minecraft.entity.monster.EntityPolarBear",
                ClassWriter.COMPUTE_FRAMES, ClassReader.EXPAND_FRAMES,
                data -> new EntityAttackableFoxMethod(data)
            ),
            true, map
        );

        ASM_DATA = map;
        ClassTransformer.initCompleted = true;
    }

    public static void initConditionMap() {
        Map<String, ASMData> map = new HashMap<>();

        addMap(
            new ASMData(
                "thedarkcolour.futuremc.block.villagepillage.SweetBerryBushBlock",
                ClassWriter.COMPUTE_FRAMES, ClassReader.EXPAND_FRAMES,
                data -> new SweetBerryBushBlockMethod(data)
            ),
            Examine.FuturemcID, map
        );

        if (!map.isEmpty()) {
            ASM_DATA = map;
            ClassTransformer.initCompleted = true;
        }
    }
}