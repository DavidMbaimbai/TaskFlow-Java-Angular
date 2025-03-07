package com.authorization.server.security;

import com.authorization.server.exception.ApiException;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

@Slf4j
@Component
public class KeyUtils {
    private static final String RSA = "RSA";
    @Value("${spring.profiles.active}")
    private String activeProfile;
    @Value("${keys.private}")
    private String privateKey;
    @Value("${keys.public}")
    private String publicKey;

    public RSAKey getRSAKeyPair() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        return generateRSAKeyPair(privateKey, publicKey);
    }

    private RSAKey generateRSAKeyPair(String privateKeyName, String publicKeyName) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        KeyPair keyPair;
        var keysDirectory = Paths.get("src", "main", "resources", "keys");
        verifyKeysDirectory(keysDirectory);
        if (Files.exists(keysDirectory.resolve(privateKey)) && Files.exists(keysDirectory.resolve(publicKey))) {
            log.info("RSA keys already exists. Loading keys from file paths: {}, {}", privateKey, publicKey);
            try {
                var privateKeyFile = keysDirectory.resolve(privateKeyName).toFile();
                var publicKeyFile = keysDirectory.resolve(publicKeyName).toFile();
                var keyFactory = KeyFactory.getInstance(RSA);
                byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
                byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
                var keyId = UUID.randomUUID().toString();
                log.info("Key Id: {}", keyId);
                return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyId).build();
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException(exception.getMessage());
            }
        }else {
            if (activeProfile.equalsIgnoreCase("prod")){
                throw new ApiException("Private and Public keys dont exist in prod environment");
            }
        }
        log.info("Generating new public and private keys: {}, {} ", privateKey, publicKey);
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
            try(var fos = new FileOutputStream(keysDirectory.resolve(publicKeyName).toFile())){
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
                fos.write(privateKeySpec.getEncoded());
            }
            try(var fos = new FileOutputStream(keysDirectory.resolve(publicKeyName).toFile())){
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
                fos.write(publicKeySpec.getEncoded());
                return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
            }
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        }

    }

    private void verifyKeysDirectory(Path keyDirectory) {
        if (!Files.exists(keyDirectory)) {
            try {
                Files.createDirectories(keyDirectory);
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException(exception.getMessage());
            }
            log.info("Create keys directory: {}", keyDirectory);
        }
    }


}
