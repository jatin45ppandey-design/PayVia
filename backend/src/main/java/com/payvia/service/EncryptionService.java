package com.payvia.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int AES_KEY_SIZE = 256; // bits

    public String generateAesKeyBase64() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(AES_KEY_SIZE, new SecureRandom());
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public String encrypt(String plaintext, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        // Output format: Base64(iv + cipherText)
        byte[] message = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, message, 0, iv.length);
        System.arraycopy(cipherText, 0, message, iv.length, cipherText.length);
        
        return Base64.getEncoder().encodeToString(message);
    }

    public String decrypt(String ciphertextBase64, String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        
        byte[] message = Base64.getDecoder().decode(ciphertextBase64);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(message, 0, iv, 0, iv.length);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        
        byte[] cipherText = new byte[message.length - GCM_IV_LENGTH];
        System.arraycopy(message, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
        
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    public String generateSha256Hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
