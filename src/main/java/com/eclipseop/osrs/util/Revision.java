package com.eclipseop.osrs.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Revision {

  public static int getRevision(ClassNode classNode) {
    AbstractInsnNode abstractInsnNode =
        classNode.methods.stream()
            .filter(mn -> mn.name.equals("init"))
            .mapMulti(
                (BiConsumer<MethodNode, Consumer<AbstractInsnNode>>)
                    (mn, c) -> mn.instructions.forEach(c))
            .filter(ain -> ain instanceof IntInsnNode && ((IntInsnNode) ain).operand == 503)
            .findFirst()
            .orElseThrow();

    return ((IntInsnNode) abstractInsnNode.getNext()).operand;
  }
}
