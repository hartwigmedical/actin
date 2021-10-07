package com.hartwig.actin.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class SystemApplication {

    private static final Logger LOGGER = LogManager.getLogger(SystemApplication.class);

    public static void main(@NotNull String... args) {
        LOGGER.info("Hello world");
    }
}
