package com.eclipseop.osrs.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Revision {

  private static final Logger LOGGER = Logger.getLogger(Revision.class.getName());

  public static int getRevision(ClassNode classNode) {
    AbstractInsnNode abstractInsnNode = classNode.methods.stream().filter(mn -> mn.name.equals("init")).flatMap((Function<MethodNode, Stream<AbstractInsnNode>>) mn -> Arrays.stream(mn.instructions.toArray())).filter(ain -> ain instanceof IntInsnNode && ((IntInsnNode) ain).operand == 503).findFirst().orElseThrow();

    return ((IntInsnNode) abstractInsnNode.getNext()).operand;
  }

  private static final byte REV_OPERAND = 15;
  private static final String HOST = "oldschool78.runescape.com";
  private static final int PORT = 43594;
  private static final byte RESPONSE_OUTDATED = 6;
  private static final byte RESPONSE_CURRENT = 0;

  private static boolean checkResponse(byte code) {
    if (code == RESPONSE_OUTDATED) {
      return false;
    } else if (code == RESPONSE_CURRENT) {
      return true;
    } else {
      throw new IllegalArgumentException("Unknown response code " + code);
    }
  }

  public static boolean isCurrentRevision(int revision) {
    try (Socket socket = new Socket(HOST, PORT)) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(baos);
      out.writeInt(REV_OPERAND);
      out.writeInt(revision);

      byte[] data = new byte[5];
      System.arraycopy(baos.toByteArray(), 3, data, 0, 5);

      socket.getOutputStream().write(data);
      socket.getOutputStream().flush();

      DataInputStream input = new DataInputStream(socket.getInputStream());
      byte response = input.readByte();
      return checkResponse(response);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  public static void main(String[] args) {
    Optional<Integer> max = Stream.of("15", "54", "13").map(Integer::valueOf).max(Integer::compare);
    System.out.println(max);

    for (int i = 207; i < 207 + 3; i++) {
      System.out.println(i);
    }
  }
}
