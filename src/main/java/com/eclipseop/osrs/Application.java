package com.eclipseop.osrs;

import com.eclipseop.osrs.util.Gamepack;
import com.eclipseop.osrs.util.Revision;
import com.google.api.gax.paging.Page;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.*;
import com.google.events.cloud.pubsub.v1.Message;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public class Application implements BackgroundFunction<Message> {

  public static final String PROJECT_ID = "osrs-gamepack-archive";
  public static final String GCS_BUCKET = "gamepacks";

  private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

  @Override
  public void accept(Message message, Context context) throws Exception {
    List<ClassNode> gamepackClassNodes = Gamepack.parse();
    int revision =
        Revision.getRevision(
            gamepackClassNodes.stream()
                .filter(cn -> cn.name.equals("client"))
                .findFirst()
                .orElseThrow());
    LOGGER.info("Detected Revision: " + revision);

    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Page<Blob> blobs = storage.list(GCS_BUCKET);

    String objectName = "osrs-" + revision + ".jar";

    boolean alreadyHaveGamepack =
        StreamSupport.stream(blobs.iterateAll().spliterator(), false)
            .anyMatch(blob -> blob.getName().equals(objectName));
    if (alreadyHaveGamepack) {
      LOGGER.info("Already have " + objectName);
      return;
    }

    LOGGER.info("Attempting to insert " + objectName);
    BlobId blobId = BlobId.of(GCS_BUCKET, objectName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    storage.create(blobInfo, Gamepack.getJarBytes());
  }
}
