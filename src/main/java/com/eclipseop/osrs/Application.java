package com.eclipseop.osrs;

import com.eclipseop.osrs.util.Gamepack;
import com.eclipseop.osrs.util.Revision;
import com.google.api.gax.paging.Page;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.*;
import com.google.events.cloud.pubsub.v1.Message;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Application implements BackgroundFunction<Message> {

  public static final String GCS_BUCKET = "gamepacks";

  private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

  @Override
  public void accept(Message message, Context context) throws Exception {
    Storage storage = StorageOptions.getDefaultInstance().getService();
    Page<Blob> blobs = storage.list(GCS_BUCKET);

    List<String> blobNames =
        StreamSupport.stream(blobs.iterateAll().spliterator(), false)
            .map(Blob::getName)
            .collect(Collectors.toList());

    Optional<Integer> lastRevision =
        blobNames.stream()
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
      if (alreadyHave(blobNames, i)) {
        LOGGER.info("We already have! Skipping download");
        return;
      }

      LOGGER.info("New revision! Downloading...");
      downloadAndInsert(storage, i);
      break;
    }
  }

  private static boolean alreadyHave(List<String> blobs, int revision) {
    String objectName = "osrs-" + revision + ".jar";
    return blobs.contains(objectName);
  }

  private static void downloadAndInsert(Storage storage, int revision) {
    BlobId blobId = BlobId.of(GCS_BUCKET, String.format("osrs-%d.jar", revision));
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    storage.create(blobInfo, Gamepack.getJarBytes());
  }
}
