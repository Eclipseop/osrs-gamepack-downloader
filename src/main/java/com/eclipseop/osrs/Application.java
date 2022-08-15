package com.eclipseop.osrs;

import com.eclipseop.osrs.util.Gamepack;
import com.eclipseop.osrs.util.Revision;
import com.google.api.gax.paging.Page;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.*;
import com.google.events.cloud.pubsub.v1.Message;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Application implements BackgroundFunction<Message> {

  public static final String GCS_BUCKET = "gamepacks";

  private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

  private static boolean alreadyHave(List<String> blobs, int revision) {
    String objectName = "osrs-" + revision + ".jar";
    return blobs.contains(objectName);
  }

  private static void downloadAndInsert(Storage storage, int revision) {
    BlobId blobId = BlobId.of(GCS_BUCKET, String.format("osrs-%d.jar", revision));
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    storage.create(blobInfo, Gamepack.getJarBytes());
  }

  @Override
  public void accept(Message message, Context context) throws Exception {
    Storage storage = StorageOptions.getDefaultInstance().getService();
    Page<Blob> blobPages = storage.list(GCS_BUCKET);

    List<Blob> blobs =
        StreamSupport.stream(blobPages.iterateAll().spliterator(), false)
            .collect(Collectors.toList());

    Optional<Blob> lastRevisionBlob =
        blobs.stream().max(Comparator.comparingLong(BlobInfo::getCreateTime));

    if (lastRevisionBlob.isEmpty()) {
      LOGGER.warning("Unable to detect last revision!");
      return;
    }

    int lastRevision = Integer.parseInt(lastRevisionBlob.get().getName().replaceAll("\\D", ""));
    LOGGER.info("Last revision: " + lastRevision);

    List<String> blobNames = blobs.stream().map(Blob::getName).collect(Collectors.toList());
    for (int i = lastRevision - 1; i < lastRevision + 3; i++) {
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
}
