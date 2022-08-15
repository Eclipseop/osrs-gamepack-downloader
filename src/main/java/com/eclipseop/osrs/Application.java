package com.eclipseop.osrs;

import com.eclipseop.osrs.util.Gamepack;
import com.eclipseop.osrs.util.Revision;
import com.google.api.gax.paging.Page;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.*;
import com.google.events.cloud.pubsub.v1.Message;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public class Application implements BackgroundFunction<Message> {

  public static final String GCS_BUCKET = "gamepacks";

  private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

  @Override
  public void accept(Message message, Context context) throws Exception {
    //    List<ClassNode> gamepackClassNodes = Gamepack.parse();
    //    int revision =
    //        Revision.getRevision(
    //            gamepackClassNodes.stream()
    //                .filter(cn -> cn.name.equals("client"))
    //                .findFirst()
    //                .orElseThrow());
    //    LOGGER.info("Detected Revision: " + revision);
    //
    //    Storage storage = StorageOptions.getDefaultInstance().getService();
    //    Page<Blob> blobs = storage.list(GCS_BUCKET);
    //
    //    String objectName = "osrs-" + revision + ".jar";
    //
    //    boolean alreadyHaveGamepack =
    //        StreamSupport.stream(blobs.iterateAll().spliterator(), false)
    //            .anyMatch(blob -> blob.getName().equals(objectName));
    //    if (alreadyHaveGamepack) {
    //      LOGGER.info("Already have " + objectName);
    //      return;
    //    }

    Storage storage = StorageOptions.getDefaultInstance().getService();
    Page<Blob> blobs = storage.list(GCS_BUCKET);
    Optional<Integer> lastRevision =
        StreamSupport.stream(blobs.iterateAll().spliterator(), false)
            .map(Blob::getName)
            .map(str -> str.replaceAll("\\D", ""))
            .map(Integer::valueOf)
            .max(Integer::compare);

    if (lastRevision.isEmpty()) {
      LOGGER.warning("Unable to detect last revision!");
      return;
    }
    LOGGER.info("Last revision: " + lastRevision);

    for (int i = lastRevision.get() - 1; i < lastRevision.get() + 3; i++) {
      if (!Revision.isCurrentRevision(i)) continue;

      LOGGER.info("Detected current revision as: " + i);
      if (alreadyHave(blobs, i)) {
        LOGGER.info("We already have! Skipping download");
        return;
      }

      LOGGER.info("New revision! Downloading...");
      //downloadAndInsert(storage, i);
      break;
    }
  }

  private static boolean alreadyHave(Page<Blob> blobs, int revision) {
    String objectName = "osrs-" + revision + ".jar";

    return
        StreamSupport.stream(blobs.iterateAll().spliterator(), false)
            .anyMatch(blob -> blob.getName().equals(objectName));
  }

  private static void downloadAndInsert(Storage storage, int revision) {
    BlobId blobId = BlobId.of(GCS_BUCKET, String.format("osrs-%d.jar", revision));
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    //storage.create(blobInfo, Gamepack.getJarBytes());
  }
}
