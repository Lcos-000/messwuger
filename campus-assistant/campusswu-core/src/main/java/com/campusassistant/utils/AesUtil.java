package com.campusassistant.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesUtil {

    @Value("${aes.secret-key}")
    private String secretKey;

    // 算法
    private static final String ALGORITHM_NAME = "AES";
    // 指定算法，初始化加密模式
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    private static final byte[] FORMAT_PREFIX = new byte[]{'I', 'V'};
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * AES加密（CBC模式，PKCS5填充）
     */
    public String encrypt(String plainText) throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec keySpec = keySpec(keyBytes);
        byte[] ivBytes = new byte[IV_LENGTH];
        secureRandom.nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] result = new byte[FORMAT_PREFIX.length + IV_LENGTH + encrypted.length];
        System.arraycopy(FORMAT_PREFIX, 0, result, 0, FORMAT_PREFIX.length);
        System.arraycopy(ivBytes, 0, result, FORMAT_PREFIX.length, IV_LENGTH);
        System.arraycopy(encrypted, 0, result, FORMAT_PREFIX.length + IV_LENGTH, encrypted.length);
        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * AES解密
     */
    public String decrypt(String encryptedText) throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        byte[] encoded = Base64.getDecoder().decode(encryptedText);
        byte[] ivBytes;
        byte[] encrypted;
        if (encoded.length >= FORMAT_PREFIX.length + IV_LENGTH * 2
                && encoded[0] == FORMAT_PREFIX[0] && encoded[1] == FORMAT_PREFIX[1]) {
            SecretKeySpec keySpec = keySpec(keyBytes);
            ivBytes = java.util.Arrays.copyOfRange(encoded, FORMAT_PREFIX.length, FORMAT_PREFIX.length + IV_LENGTH);
            encrypted = java.util.Arrays.copyOfRange(encoded, FORMAT_PREFIX.length + IV_LENGTH, encoded.length);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
        } else {
            // Backward compatibility for tasks encrypted before random IVs were introduced.
            byte[] legacyKey = java.util.Arrays.copyOf(keyBytes, IV_LENGTH);
            SecretKeySpec keySpec = new SecretKeySpec(legacyKey, ALGORITHM_NAME);
            ivBytes = legacyKey;
            encrypted = encoded;
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
        }
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private SecretKeySpec keySpec(byte[] keyBytes) throws GeneralSecurityException {
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new GeneralSecurityException("AES_SECRET_KEY 必须为 16、24 或 32 字节");
        }
        return new SecretKeySpec(keyBytes, ALGORITHM_NAME);
    }
}
