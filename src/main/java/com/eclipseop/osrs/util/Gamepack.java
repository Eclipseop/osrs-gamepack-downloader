package com.eclipseop.osrs.util;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Gamepack {

  private static final Logger LOGGER = Logger.getLogger(Gamepack.class.getName());

  private static final Function<InputStream, byte[]> readInputStream =
      inputStream -> {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
          byte[] buffer = new byte[4096];
          int caret;

          while ((caret = inputStream.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, caret);
          }
          out.flush();
          return out.toByteArray();
        } catch (IOException e) {
          e.printStackTrace();
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

  public static byte[] getJarBytes() {
    try (InputStream inputStream =
             new BufferedInputStream(new URL(getGamepackDownloadUrl()).openStream())) {
      return readInputStream.apply(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
