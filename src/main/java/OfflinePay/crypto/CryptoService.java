package OfflinePay.crypto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String plainText, String secretKey) throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        GCMParameterSpec gcmSpec =
                new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encrypted =
                cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] result = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(
                encrypted,
                0,
                result,
                iv.length,
                encrypted.length
        );

        return Base64.getEncoder().encodeToString(result);
    }

    public String decrypt(String encryptedText, String secretKey)
            throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        byte[] combined =
                Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted =
                new byte[combined.length - IV_LENGTH];

        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
        System.arraycopy(
                combined,
                IV_LENGTH,
                encrypted,
                0,
                encrypted.length
        );

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        GCMParameterSpec gcmSpec =
                new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(
                decrypted,
                StandardCharsets.UTF_8
        );
    }

    public String generateSecretKey() throws Exception {

        KeyGenerator keyGenerator =
                KeyGenerator.getInstance("AES");

        keyGenerator.init(AES_KEY_SIZE);

        SecretKey secretKey =
                keyGenerator.generateKey();

        return Base64.getEncoder()
                .encodeToString(secretKey.getEncoded());
    }
}
