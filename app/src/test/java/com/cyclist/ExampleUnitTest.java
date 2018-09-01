package com.cyclist;

import com.cyclist.Logic.Common.Utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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