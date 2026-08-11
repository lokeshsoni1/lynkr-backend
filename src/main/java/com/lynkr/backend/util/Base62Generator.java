package com.lynkr.backend.util;

import java.security.SecureRandom;

public class Base62Generator {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 7;

    public static String generateShortCode() {
        return generateShortCode(DEFAULT_LENGTH);
    }

    public static String generateShortCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62_CHARS.charAt(RANDOM.nextInt(BASE62_CHARS.length())));
        }
        return sb.toString();
    }
}
