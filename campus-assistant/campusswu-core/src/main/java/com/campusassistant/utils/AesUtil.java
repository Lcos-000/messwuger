package com.campusassistant.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AesUtil {

    @Value("${aes.secret-key}")
    private String secretKey;

    // 算法
    private static final String ALGORITHM_NAME = "AES";
    // 指定算法，初始化加密模式
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    // 密钥偏移量
    private static final int KEY_OFFSET = 0;
    // 密钥长度 (AES-128 对应 16字节)
    private static final int KEY_LENGTH = 16;

    /**
     * AES加密（CBC模式，PKCS5填充）
     */
    public String encrypt(String plainText) throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, KEY_OFFSET, KEY_LENGTH, ALGORITHM_NAME);
        IvParameterSpec iv = new IvParameterSpec(
                secretKey.getBytes(StandardCharsets.UTF_8), KEY_OFFSET, KEY_LENGTH
        );

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES解密
     */
    public String decrypt(String encryptedText) throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, KEY_OFFSET, KEY_LENGTH, ALGORITHM_NAME);
        IvParameterSpec iv = new IvParameterSpec(
                secretKey.getBytes(StandardCharsets.UTF_8), KEY_OFFSET, KEY_LENGTH
        );

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        byte[] encrypted = Base64.getDecoder().decode(encryptedText);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}