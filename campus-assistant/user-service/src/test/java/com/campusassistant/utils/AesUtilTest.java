package com.campusassistant.utils;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

class AesUtilTest {

    private static final String KEY = "0123456789abcdefghijklmn";

    @Test
    void encryptUsesRandomIvAndRoundTrips() throws Exception {
        AesUtil aesUtil = configuredAesUtil();

        String first = aesUtil.encrypt("campus-password");
        String second = aesUtil.encrypt("campus-password");

        assertNotEquals(first, second);
        assertEquals("campus-password", aesUtil.decrypt(first));
        assertEquals("campus-password", aesUtil.decrypt(second));
    }

    @Test
    void legacyCiphertextBeginningWithIvStillDecrypts() throws Exception {
        AesUtil aesUtil = configuredAesUtil();
        byte[] base = "legacy-password-that-is-long-enough".getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < 1 << 16; i++) {
            byte[] plaintext = Arrays.copyOf(base, base.length);
            plaintext[0] = (byte) (i >>> 8);
            plaintext[1] = (byte) i;
            String encoded = legacyEncrypt(plaintext);
            byte[] decoded = Base64.getDecoder().decode(encoded);
            if (decoded.length >= 2 && decoded[0] == 'I' && decoded[1] == 'V') {
                assertEquals(new String(plaintext, StandardCharsets.UTF_8), aesUtil.decrypt(encoded));
                return;
            }
        }

        fail("could not construct a legacy ciphertext beginning with IV");
    }

    private static AesUtil configuredAesUtil() throws Exception {
        AesUtil aesUtil = new AesUtil();
        Field secretKey = AesUtil.class.getDeclaredField("secretKey");
        secretKey.setAccessible(true);
        secretKey.set(aesUtil, KEY);
        aesUtil.validateSecretKey();
        return aesUtil;
    }

    private static String legacyEncrypt(byte[] plaintext) throws Exception {
        byte[] legacyKey = Arrays.copyOf(KEY.getBytes(StandardCharsets.UTF_8), 16);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(legacyKey, "AES"),
                new IvParameterSpec(legacyKey));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext));
    }
}
