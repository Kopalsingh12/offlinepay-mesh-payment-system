package OfflinePay.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    private static final String RSA_TRANSFORMATION =
            "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private static final String AES_TRANSFORMATION =
            "AES/GCM/NoPadding";

    private static final int AES_KEY_BITS = 256;
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int RSA_KEY_BITS = 2048;
    private static final int RSA_ENCRYPTED_KEY_BYTES = 256;

    private final SecureRandom secureRandom =
            new SecureRandom();

    private final ServerKeyHolder serverKeyHolder;

    public CryptoService(ServerKeyHolder serverKeyHolder) {
        this.serverKeyHolder = serverKeyHolder;
    }

    public String encrypt(
            String plainText,
            PublicKey serverPublicKey) throws Exception {

        byte[] plaintext =
                plainText.getBytes(StandardCharsets.UTF_8);

        KeyGenerator keyGenerator =
                KeyGenerator.getInstance("AES");

        keyGenerator.init(AES_KEY_BITS);

        SecretKey aesKey =
                keyGenerator.generateKey();

        byte[] iv = new byte[GCM_IV_BYTES];
        secureRandom.nextBytes(iv);

        Cipher aesCipher =
                Cipher.getInstance(AES_TRANSFORMATION);

        aesCipher.init(
                Cipher.ENCRYPT_MODE,
                aesKey,
                new GCMParameterSpec(
                        GCM_TAG_BITS,
                        iv
                )
        );

        byte[] ciphertext =
                aesCipher.doFinal(plaintext);

        Cipher rsaCipher =
                Cipher.getInstance(RSA_TRANSFORMATION);

        OAEPParameterSpec oaepSpec =
                new OAEPParameterSpec(
                        "SHA-256",
                        "MGF1",
                        MGF1ParameterSpec.SHA256,
                        PSource.PSpecified.DEFAULT
                );

        rsaCipher.init(
                Cipher.ENCRYPT_MODE,
                serverPublicKey,
                oaepSpec
        );

        byte[] encryptedAesKey =
                rsaCipher.doFinal(
                        aesKey.getEncoded()
                );

        ByteBuffer buffer =
                ByteBuffer.allocate(
                        encryptedAesKey.length
                                + iv.length
                                + ciphertext.length
                );

        buffer.put(encryptedAesKey);
        buffer.put(iv);
        buffer.put(ciphertext);

        return Base64.getEncoder()
                .encodeToString(buffer.array());
    }

    public String decrypt(
            String base64Ciphertext) throws Exception {

        byte[] all =
                Base64.getDecoder()
                        .decode(base64Ciphertext);

        int minimumLength =
                RSA_ENCRYPTED_KEY_BYTES
                        + GCM_IV_BYTES
                        + (GCM_TAG_BITS / 8);

        if (all.length < minimumLength) {
            throw new IllegalArgumentException(
                    "Ciphertext too short"
            );
        }

        byte[] encryptedAesKey =
                new byte[RSA_ENCRYPTED_KEY_BYTES];

        byte[] iv =
                new byte[GCM_IV_BYTES];

        byte[] ciphertext =
                new byte[
                        all.length
                                - RSA_ENCRYPTED_KEY_BYTES
                                - GCM_IV_BYTES
                ];

        ByteBuffer buffer =
                ByteBuffer.wrap(all);

        buffer.get(encryptedAesKey);
        buffer.get(iv);
        buffer.get(ciphertext);

        Cipher rsaCipher =
                Cipher.getInstance(RSA_TRANSFORMATION);

        OAEPParameterSpec oaepSpec =
                new OAEPParameterSpec(
                        "SHA-256",
                        "MGF1",
                        MGF1ParameterSpec.SHA256,
                        PSource.PSpecified.DEFAULT
                );

        rsaCipher.init(
                Cipher.DECRYPT_MODE,
                serverKeyHolder.getPrivateKey(),
                oaepSpec
        );

        byte[] aesKeyBytes =
                rsaCipher.doFinal(encryptedAesKey);

        SecretKey aesKey =
                new SecretKeySpec(
                        aesKeyBytes,
                        "AES"
                );

        Cipher aesCipher =
                Cipher.getInstance(AES_TRANSFORMATION);

        aesCipher.init(
                Cipher.DECRYPT_MODE,
                aesKey,
                new GCMParameterSpec(
                        GCM_TAG_BITS,
                        iv
                )
        );

        byte[] plaintext =
                aesCipher.doFinal(ciphertext);

        return new String(
                plaintext,
                StandardCharsets.UTF_8
        );
    }

    public String hashCiphertext(
            String base64Ciphertext) throws Exception {

        MessageDigest sha256 =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                sha256.digest(
                        base64Ciphertext
                                .getBytes(StandardCharsets.UTF_8)
                );

        StringBuilder result =
                new StringBuilder();

        for (byte b : hash) {
            result.append(
                    String.format("%02x", b)
            );
        }

        return result.toString();
    }

    public String getServerPublicKey() {

        return serverKeyHolder
                .getPublicKeyBase64();
    }
}