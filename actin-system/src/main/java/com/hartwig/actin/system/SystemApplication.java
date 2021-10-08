package com.hartwig.actin.system;

import com.hartwig.actin.algo.AlgoRunner;
import com.hartwig.actin.clinical.ClinicalIngestionApplication;
import com.hartwig.actin.database.DatabaseLoaderApplication;
import com.hartwig.actin.report.ReportApplication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class SystemApplication {

    private static final Logger LOGGER = LogManager.getLogger(SystemApplication.class);

    public static void main(@NotNull String... args) {
        LOGGER.info("The following applications are available through ACTIN");
        LOGGER.info(" {}", ClinicalIngestionApplication.class);
        LOGGER.info(" {}", AlgoRunner.class);
        LOGGER.info(" {}", DatabaseLoaderApplication.class);
        LOGGER.info(" {}", ReportApplication.class);
    }
}
