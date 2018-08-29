package com.cyclist;

import com.cyclist.logic.common.Utils;

import org.junit.Test;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.*;

public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception{
        String password = "1234abcd";
        byte[] encryptedPassword = Utils.encrypt(password);
        String decryptedPassword = Utils.decrypt(encryptedPassword);

        assertNotEquals(password ,new String(encryptedPassword));
        assertEquals(password ,decryptedPassword);
    }
}