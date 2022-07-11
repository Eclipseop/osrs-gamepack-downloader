package com.eclipseop.osrs.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.stream.Collectors;

public class GamepackTest {

  @Test
  public void testGettingJarBytes() {
    byte[] jarBytes = Gamepack.getJarBytes();
    assertNotNull(jarBytes);
  }

  @Test
  public void testParsingGamepack() {
    List<ClassNode> parsed = Gamepack.parse();
    List<String> classNames = parsed.stream().map(cn -> cn.name).collect(Collectors.toList());

    assertNotNull(parsed);
    assertTrue(classNames.stream().anyMatch(name -> name.equals("client")));
  }
}
