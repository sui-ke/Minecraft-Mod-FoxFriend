package suike.suikefoxfriend.inter;

import java.util.Map;

import org.objectweb.asm.*;

public interface IMethod {

    Map<String, String[]> getMethods();

    MethodVisitor setMethodType(String type);

    default String[] getInterfaces(String[] interfaces) {
        return interfaces;
    }

    default void addValue(ClassVisitor visitor) {}
}