package com.hartwig.actin.database.dao;

final class DatabaseUtil {

    private DatabaseUtil() {
    }

    public static byte toByte(boolean bool) {
        return (byte) (bool ? 1 : 0);
    }
}

