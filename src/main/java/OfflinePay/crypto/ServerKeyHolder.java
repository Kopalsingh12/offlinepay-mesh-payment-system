package OfflinePay.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ServerKeyHolder {

    private KeyPair keyPair;

    @PostConstruct
    public void init() throws Exception {

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        keyPair = generator.generateKeyPair();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder()
                .encodeToString(
                        keyPair.getPublic().getEncoded()
                );
    }
}