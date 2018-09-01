package com.cyclist.Logic.Common;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    private static final String key = "E546C8D2W4R56M9V";
    private static Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
    private static Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(String toEncrypt) throws Exception{
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(toEncrypt.getBytes());
    }

    public static String decrypt(byte[] toDecrypt) throws Exception{
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return new String(cipher.doFinal(toDecrypt));
    }
}
