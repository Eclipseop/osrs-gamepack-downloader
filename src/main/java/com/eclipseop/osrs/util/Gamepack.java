package com.eclipseop.osrs.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Gamepack {

  private static final Logger LOGGER = Logger.getLogger(Gamepack.class.getName());
  private static final Function<JarInputStream, byte[]> jisToByteArray =
      jis -> {
        try {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          byte[] buffer = new byte[4096];
          int caret;

          while ((caret = jis.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, caret);
          }
          out.flush();
          return (byte[]) Function.identity().apply(out.toByteArray());
        } catch (IOException e) {
          return null;
        }
      };

  private static List<String> getConfig() {
    try (BufferedReader br =
        new BufferedReader(
            new InputStreamReader(
                new URL("http://oldschool.runescape.com/jav_config.ws").openStream()))) {
      return br.lines().collect(Collectors.toList());
    } catch (IOException e) {
      LOGGER.warning("Exception occurred while parsing jav_config.ws");
    }
    return Collections.emptyList();
  }

  private static String getGamepackName() {
    String gamepackLine =
        getConfig().stream().filter(s -> s.startsWith("initial_jar")).findFirst().orElseThrow();
    return gamepackLine.replace("initial_jar=", "");
  }

  private static String getGamepackDownloadUrl() {
    return "http://oldschool1.runescape.com/" + getGamepackName();
  }

  private static JarInputStream getJarInputStream() throws IOException {
    String gamepackDownloadUrl = getGamepackDownloadUrl();
    return new JarInputStream(new URL(gamepackDownloadUrl).openStream());
  }

  public static List<ClassNode> parse() {
    List<ClassNode> classNodes = new ArrayList<>();

    try {
      JarInputStream jis = getJarInputStream();

      JarEntry entry;
      while ((entry = jis.getNextJarEntry()) != null) {
        if (!entry.getName().endsWith(".class")) continue;

        ClassReader cr = new ClassReader(jisToByteArray.apply(jis));
        ClassNode classNode = new ClassNode();

        cr.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        classNodes.add(classNode);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return classNodes;
  }

  public static byte[] getJarBytes() {
    try {
      InputStream inputStream =
          new BufferedInputStream(new URL(getGamepackDownloadUrl()).openStream());
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      byte[] buffer = new byte[4096];
      int caret;

      while ((caret = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, caret);
      }

      inputStream.close();
      outputStream.close();
      return outputStream.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
