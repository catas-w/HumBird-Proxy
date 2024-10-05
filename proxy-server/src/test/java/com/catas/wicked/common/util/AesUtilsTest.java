package com.catas.wicked.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

public class AesUtilsTest {

    @Test
    public void testGenerateKey() throws Exception {
        SecretKey secretKey = AesUtils.generateSecretKey();

        String keyToString = AesUtils.secretKeyToString(secretKey);
        System.out.println(keyToString);

        SecretKey key = AesUtils.stringToSecretKey(keyToString);
        Assertions.assertEquals(secretKey, key);
    }

    @Test
    public void testAes() throws Exception {
        SecretKey secretKey = AesUtils.generateSecretKey();

        {
            String plainText = "Hello, this is a secret message.";

            // Encrypt the text
            String encryptedText = AesUtils.encrypt(plainText, secretKey);
            System.out.println("Encrypted Text: " + encryptedText);

            // Decrypt the text
            String decryptedText = AesUtils.decrypt(encryptedText, secretKey);
            Assertions.assertEquals(plainText, decryptedText);
        }

        {
            String plainText = "123456";

            // Encrypt the text
            String encryptedText = AesUtils.encrypt(plainText, secretKey);
            System.out.println("Encrypted Text: " + encryptedText);

            // Decrypt the text
            String decryptedText = AesUtils.decrypt(encryptedText, secretKey);
            Assertions.assertEquals(plainText, decryptedText);
        }
    }
}
