package com.payvia.service;

import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class SignatureVerificationService {

    public void validatePublicKeyFormat(String publicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("EC");
            PublicKey publicKey = kf.generatePublic(spec);
            
            if (!publicKey.getAlgorithm().equals("EC") && !publicKey.getAlgorithm().equals("ECDSA")) {
                throw new IllegalArgumentException("Unsupported key algorithm");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 public key");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid X.509 SPKI public key format or unsupported algorithm");
        }
    }

    public boolean verifySignature(String publicKeyBase64, String payload, String signatureBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("EC");
            PublicKey publicKey = kf.generatePublic(spec);

            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(payload.getBytes("UTF-8"));

            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return ecdsaVerify.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }
}