package suike.suikefoxfriend.asm.advice;

import java.util.Map;

import suike.suikefoxfriend.inter.IMethod;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import com.google.common.collect.ImmutableMap;

public class EntityAttackableFoxMethod extends AdviceAdapter implements IMethod {
    private String type = "0"; // 无类型

    public EntityAttackableFoxMethod(AdviceAdapterData data) {
        super(Opcodes.ASM5, data.mv, data.access, data.name, data.desc);
    }

// 需要修改的方法
    @Override
    public Map<String, String[]> getMethods() {
        return ImmutableMap.<String, String[]>builder()
            .put("r", new String[] {"()V", "initAI"})
            .build();
    }

    @Override
    public MethodVisitor setMethodType(String type) {
        this.type = type;
        return this;
    }

// 在方法返回前注入
    @Override
    public void visitInsn(int opcode) {
        if (this.type.equals("initAI") && opcode == Opcodes.RETURN) {
            this.addAI();
        }
        super.visitInsn(opcode);
    }

    private void addAI() {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "suike/suikefoxfriend/entity/ai/EntityAttackableFoxAI",
            "initEntityAI",
            "(Lnet/minecraft/entity/EntityCreature;)V",
            false
        );
    }
}