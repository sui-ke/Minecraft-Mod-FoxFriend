package suike.suikefoxfriend.asm.advice;

import java.util.Map;

import suike.suikefoxfriend.inter.IMethod;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import com.google.common.collect.ImmutableMap;

public class SweetBerryBushBlockMethod extends AdviceAdapter implements IMethod {
    private String type = "0"; // 无类型

    public SweetBerryBushBlockMethod(AdviceAdapterData data) {
        super(Opcodes.ASM5, data.mv, data.access, data.name, data.desc);
    }

// 需要修改的方法
    @Override
    public Map<String, String[]> getMethods() {
        return ImmutableMap.<String, String[]>builder()
            .put("func_180634_a", new String[] {"(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V", "entityCollided"})
            .build();
    }

    @Override
    public MethodVisitor setMethodType(String type) {
        this.type = type;
        return this;
    }

// 在方法头部注入
    @Override
    public void onMethodEnter() {
        if ("entityCollided".equals(this.type)) {
            this.modifyEntityCollided();
        }
    }

    public void modifyEntityCollided() {
        mv.visitVarInsn(ALOAD, 4);

        // 检查是否是 FoxEntity
        Label skipReturn = new Label();
        visitTypeInsn(INSTANCEOF, "suike/suikefoxfriend/entity/fox/FoxEntity");
        visitJumpInsn(IFEQ, skipReturn);

        visitInsn(RETURN);
        visitLabel(skipReturn);
    }
}