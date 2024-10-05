package com.catas.wicked.common.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesUtils {

    public static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        return encrypt(plainText.getBytes(), secretKey);
    }

    public static String encrypt(byte[] data, SecretKey secretKey) throws Exception{
        // Create AES Cipher instance
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        byte[] decryptedBytes = decrypt(encryptedText.getBytes(), secretKey);
        return new String(decryptedBytes);
    }

    public static byte[] decrypt(byte[] data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(data);

        return cipher.doFinal(encryptedBytes);
    }

    public static String secretKeyToString(SecretKey secretKey) {
        byte[] encodedKey = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public static SecretKey stringToSecretKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public static SecretKey generateSecretKey() throws Exception {
        // Generate AES Key using KeyGenerator
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

        // 128, 192, or 256 bits
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

}
