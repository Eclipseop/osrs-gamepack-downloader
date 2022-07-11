package com.eclipseop.osrs.util;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class RevisionTest {

  @Test
  public void testFindingRevision() {
    List<ClassNode> parsedGamepack = Gamepack.parse();
    ClassNode clientClass = parsedGamepack.stream().filter(cn -> cn.name.equals("client")).findFirst().orElseThrow();

    int revision = Revision.getRevision(clientClass);
    assertTrue(revision > 0);
  }
}
