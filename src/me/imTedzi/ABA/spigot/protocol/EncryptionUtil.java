package me.imTedzi.ABA.spigot.protocol;

import com.google.common.base.Charsets;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.stream.Stream;

/**
 * Encryption and decryption minecraft util for connection between servers
 * and paid minecraft account clients.
 *
 * Source:
 * https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/MinecraftEncryption.java
 *
 * Remapped by:
 * https://github.com/Techcable/MinecraftMappings/tree/master/1.8
 */
public class EncryptionUtil {

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

            keyPairGenerator.initialize(1_024);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException nosuchalgorithmexception) {
            //Should be existing in every vm
            throw new ExceptionInInitializerError(nosuchalgorithmexception);
        }
    }

    public static byte[] getServerIdHash(String serverId, PublicKey publicKey, SecretKey secretKey) {
        return digestOperation("SHA-1"
                , new byte[][]{serverId.getBytes(Charsets.ISO_8859_1), secretKey.getEncoded(), publicKey.getEncoded()});
    }

    private static byte[] digestOperation(String algo, byte[]... content) {
        try {
            MessageDigest messagedigest = MessageDigest.getInstance(algo);
            Stream.of(content).forEach(messagedigest::update);

            return messagedigest.digest();
        } catch (NoSuchAlgorithmException nosuchalgorithmexception) {
            nosuchalgorithmexception.printStackTrace();
            return null;
        }
    }

    public static SecretKey decryptSharedKey(PrivateKey privateKey, byte[] encryptedSharedKey) {
        return new SecretKeySpec(decryptData(privateKey, encryptedSharedKey), "AES");
    }

    public static byte[] decryptData(Key key, byte[] data) {
        return cipherOperation(Cipher.DECRYPT_MODE, key, data);
    }

    private static byte[] cipherOperation(int operationMode, Key key, byte[] data) {
        try {
            return createCipherInstance(operationMode, key.getAlgorithm(), key).doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException illegalblocksizeexception) {
            illegalblocksizeexception.printStackTrace();
        }

        System.err.println("Cipher data failed!");
        return null;
    }

    private static Cipher createCipherInstance(int operationMode, String cipherName, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(cipherName);

            cipher.init(operationMode, key);
            return cipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException invalidkeyexception) {
            invalidkeyexception.printStackTrace();
        }

        System.err.println("Cipher creation failed!");
        return null;
    }

    private EncryptionUtil() {
        //utility
    }
}
