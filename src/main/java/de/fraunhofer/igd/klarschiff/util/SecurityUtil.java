package de.fraunhofer.igd.klarschiff.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang.StringUtils;

/**
 * Die Klasse stellt Funktionen zum Ver- und Entschlüsseln bereit.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class SecurityUtil {

  static Cipher cipherEncrypt;
  static Cipher cipherDecrypt;

  static {
    SecretKeySpec key = new SecretKeySpec("1234567890".getBytes(),
      "Blowfish");
    try {
      cipherEncrypt = Cipher.getInstance("Blowfish");
      cipherEncrypt.init(Cipher.ENCRYPT_MODE, key);
      cipherDecrypt = Cipher.getInstance("Blowfish");
      cipherDecrypt.init(Cipher.DECRYPT_MODE, key);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Einfaches Verschlüsseln von Text.
   *
   * @param to_encrypt Text der verschlüsselt werden soll.
   * @return Verschlüsselter Text
   */
  public static String simpleEncrypt(String to_encrypt) {
    try {
      byte[] bytea;
      String str;
      bytea = to_encrypt.getBytes();
      bytea = cipherEncrypt.doFinal(bytea);
      //bytea = Base64.encodeBase64(bytea);
      //str = new String(bytea);
      //str = URLEncoder.encode(str, "UTF-8");
      str = encode(bytea);
      return str;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Einfaches Verschlüsseln von Bytes.
   *
   * @param bytes Bytes die verschlüsselt werden sollen.
   * @return Verschlüsselte Bytes als Text
   */
  private static String encode(byte[] bytes) {
    StringBuilder str = new StringBuilder();
    for (byte b : bytes) {
      if (b < 0) {
        str.append(1);
      } else {
        str.append(0);
      }
      str.append(StringUtils.leftPad(Math.abs(b) + "", 3, '0'));
    }
    return str.toString();
  }

  /**
   * Einfaches Entschlüsseln in Text.
   *
   * @param to_decrypt Verschlüsselter Text.
   * @return Entschlüsselter Text
   */
  public static String simpleDecrypt(String to_decrypt) {
    try {
      byte[] bytea;
      String str;
      bytea = decode(to_decrypt);
      bytea = cipherDecrypt.doFinal(bytea);
      str = new String(bytea);
      return str;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Einfaches Entschlüsseln in Bytes.
   *
   * @param str Verschlüsselter Text.
   * @return Entschlüsselte Bytes
   */
  private static byte[] decode(String str) {
    byte[] bytes = new byte[str.length() / 4];
    for (int i = 0; i < str.length() / 4; i++) {
      int b = (str.charAt(i * 4) == '1') ? -1 : 1;
      b = b * Integer.parseInt(str.substring(i * 4 + 1, i * 4 + 4));
      bytes[i] = (byte) b;
    }
    return bytes;
  }
}
