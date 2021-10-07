package com.hartwig.actin.clinical;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ClinicalApplication {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalApplication.class);

    public static void main(@NotNull String... args) {
        LOGGER.info("Hello world");
    }
}
