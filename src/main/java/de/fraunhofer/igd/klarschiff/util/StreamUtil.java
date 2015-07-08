package de.fraunhofer.igd.klarschiff.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Die Klasse bietet verschiedene Funktionen zum Arbeiten mit Streams.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class StreamUtil {

  static final int BUFFER_SIZE = 65536;

  /**
   * Kopiert einen Inputstream in einen Outputstream.
   *
   * @param is Inputstream
   * @param os Outputstream
   * @param closeIs Inputstream nach dem Kopieren schließen?
   * @param closeOs Outputstream nach dem Kopieren schließen?
   * @param encoding verwendet Encoding zum Lesen des Inputstreams
   * @throws Exception
   */
  public static void copyStreamContent(InputStream is, OutputStream os, boolean closeIs, boolean closeOs, String encoding) throws Exception {
    InputStreamReader isr = new InputStreamReader(is, encoding);
    PrintWriter pw = new PrintWriter(os, true);
    char[] buffer = new char[BUFFER_SIZE];
    int length;
    while ((length = isr.read(buffer, 0, BUFFER_SIZE)) != -1) {
      pw.write(buffer, 0, length);
    }
    if (closeIs) {
      is.close();
    }
    if (closeOs) {
      os.close();
    }

  }

  /**
   * Kopiert einen Inputstream in einen Outputstream.
   *
   * @param is Inputstream
   * @param os Outputstream
   * @param closeIs Inputstream nach dem Kopieren schließen?
   * @param closeOs Outputstream nach dem Kopieren schließen?
   * @throws Exception
   */
  public static void copyStreamContent(InputStream is, OutputStream os, boolean closeIs, boolean closeOs) throws Exception {
    byte[] buffer = new byte[BUFFER_SIZE];
    int length;
    while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
      os.write(buffer, 0, length);
    }
    if (closeIs) {
      is.close();
    }
    if (closeOs) {
      os.close();
    }

  }

  /**
   * Kopiert einen Inputstream in einen Outputstream.
   *
   * @param is Inputstream
   * @param os Outputstream
   * @param closeIs Inputstream nach dem Kopieren schließen?
   * @param closeOs Outputstream nach dem Kopieren schließen?
   * @param autoFlush flush nach jedem Lesen eines Blockes auf dem Outputstream ausführen?
   * @throws Exception
   */
  public static void copyStreamContent(InputStream is, OutputStream os, boolean closeIs, boolean closeOs, boolean autoFlush) throws Exception {
    byte[] buffer = new byte[BUFFER_SIZE];
    int length;
    while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
      os.write(buffer, 0, length);
      if (autoFlush) {
        os.flush();
      }
    }
    if (closeIs) {
      is.close();
    }
    if (closeOs) {
      os.close();
    }

  }

  /**
   * Liest einen Inputstream in ein Bytearray
   *
   * @param is Inputstream
   * @return Daten des Inputstream als Bytearray
   * @throws Exception
   */
  public static byte[] readInputStream(InputStream is) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    copyStreamContent(is, bos, true, true);
    return bos.toByteArray();
  }

  /**
   * Liest einen Inputstream in ein Bytearray
   *
   * @param is Inputstream
   * @param encoding verwendet Encoding zum Lesen des Inputstreams
   * @return Daten des Inputstream als Bytearray
   * @throws Exception
   */
  public static String readInputStreamToString(InputStream is, String encoding) throws Exception {
    StringBuilder sb = new StringBuilder();
    InputStreamReader isr = new InputStreamReader(is, encoding);
    byte[] buffer = new byte[BUFFER_SIZE];
    int length;
    while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
      sb.append(new String(buffer, 0, length, encoding));
    }
    isr.close();
    return sb.toString();
  }
}
