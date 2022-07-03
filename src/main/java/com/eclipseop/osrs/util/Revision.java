package com.eclipseop.osrs.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public class Revision {

  public static int getRevision(ClassNode classNode) {
    AbstractInsnNode abstractInsnNode =
        classNode.methods.stream()
            .filter(mn -> mn.name.equals("init"))
            .flatMap(
                (Function<MethodNode, Stream<AbstractInsnNode>>)
                    mn -> Arrays.stream(mn.instructions.toArray()))
            .filter(ain -> ain instanceof IntInsnNode && ((IntInsnNode) ain).operand == 503)
            .findFirst()
            .orElseThrow();

    return ((IntInsnNode) abstractInsnNode.getNext()).operand;
  }
}
