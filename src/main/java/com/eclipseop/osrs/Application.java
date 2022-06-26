package com.eclipseop.osrs;

import com.eclipseop.osrs.util.Gamepack;
import com.eclipseop.osrs.util.Revision;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.logging.Logger;

public class Application implements BackgroundFunction<Message> {

  private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

  @Override
  public void accept(Message message, Context context) throws Exception {
    LOGGER.info("Test with java Logger");

    List<ClassNode> parse = Gamepack.parse();
    int revision =
        Revision.getRevision(
            parse.stream().filter(cn -> cn.name.equals("client")).findFirst().orElseThrow());
    LOGGER.info("Detected Revision: " + revision);
  }
}
