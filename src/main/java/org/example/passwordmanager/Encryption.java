package org.example.passwordmanager;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class Encryption {

    public static boolean isValidBase64(String text) {
        try {
            Base64.getDecoder().decode(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public static byte[] generateSalt()
    {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }
    public static String hashKey(String key) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(key.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hashedBytes);
    }
    public static byte[] getSalt() throws IOException {
        String saltString = JsonReadWrite.getSaltString();
        return Base64.getDecoder().decode(saltString);
    }
    public static SecretKeySpec getAESKeyFromPassword(String masterPassword, byte[] salt) throws Exception {
        int iterationCount = 100000;
        int keyLength = 256;

        PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    public static IvParameterSpec generateIV()
    {
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String encryptCBC(String plainText, SecretKeySpec key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    public static String decryptCBC(String cipherText, SecretKeySpec key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        byte[] encryptedBytes = Base64.getDecoder().decode(cipherText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }
}