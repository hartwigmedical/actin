package com.hartwig.actin.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ReportApplication {

    private static final Logger LOGGER = LogManager.getLogger(ReportApplication.class);

    public static final String VERSION = ReportApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) {
        LOGGER.info("Running ACTIN Report v{}", VERSION);
    }
}
