package com.example.whatsapp6;

import android.content.Context;
import android.content.SharedPreferences;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class AESUtils {

    // SharedPreferences name to store the AES key
    private static final String PREF_NAME = "AES_PREF";
    private static final String KEY_ALIAS = "aes_key";

    // Method to generate and store a secret key for AES encryption
    public static SecretKey generateKey(Context context) throws Exception {
        SecretKey secretKey = null;

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String storedKey = sharedPreferences.getString(KEY_ALIAS, null);

        if (storedKey != null) {
            // If the key is already stored, retrieve it
            byte[] decodedKey = Base64.getDecoder().decode(storedKey);
            secretKey = new javax.crypto.spec.SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        } else {
            // Otherwise, generate a new key
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);  // 128-bit AES key
            secretKey = keyGenerator.generateKey();

            // Store the new key in SharedPreferences
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            sharedPreferences.edit().putString(KEY_ALIAS, encodedKey).apply();
        }

        return secretKey;
    }

    // Method to encrypt a message using AES
    public static String encrypt(String message, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);  // Convert to base64 for easy display
    }

    // Method to decrypt a message using AES
    public static String decrypt(String encryptedMessage, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage); // Decode from base64
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);  // Convert bytes back to a string
    }
}

