package com.bookstore.common.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {
    @Test void encode_NotEqualToRaw() {
        String encoded = PasswordUtil.encode("MyPassword123!");
        assertNotEquals("MyPassword123!", encoded);
        assertTrue(encoded.startsWith("$2a$"));
    }
    @Test void encode_SamePassword_DifferentSalt() {
        assertNotEquals(PasswordUtil.encode("SamePwd1!"), PasswordUtil.encode("SamePwd1!"));
    }
    @Test void matches_Correct() { assertTrue(PasswordUtil.matches("MyPassword123!", PasswordUtil.encode("MyPassword123!"))); }
    @Test void matches_Wrong() { assertFalse(PasswordUtil.matches("WrongPassword!", PasswordUtil.encode("MyPassword123!"))); }
    @Test void matches_Empty() { assertFalse(PasswordUtil.matches("", PasswordUtil.encode("RealPwd1!"))); }
}
