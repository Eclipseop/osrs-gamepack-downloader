package com.eclipseop.osrs.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class Revision {

  private static final Logger LOGGER = Logger.getLogger(Revision.class.getName());

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
}
