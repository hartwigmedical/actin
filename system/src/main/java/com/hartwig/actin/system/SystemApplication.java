package com.hartwig.actin.system;

import com.hartwig.actin.algo.TreatmentMatcherApplication;
import com.hartwig.actin.clinical.ClinicalIngestionApplication;
import com.hartwig.actin.database.ClinicalLoaderApplication;
import com.hartwig.actin.report.ReporterApplication;
import com.hartwig.actin.treatment.TreatmentCreatorApplication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class SystemApplication {

    private static final Logger LOGGER = LogManager.getLogger(SystemApplication.class);

    private static final String VERSION = SystemApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) {
        LOGGER.info("The following applications are available through ACTIN v{}", VERSION);
        LOGGER.info(" {}", ClinicalIngestionApplication.class);
        LOGGER.info(" {}", TreatmentCreatorApplication.class);
        LOGGER.info(" {}", TreatmentMatcherApplication.class);
        LOGGER.info(" {}", ClinicalLoaderApplication.class);
        LOGGER.info(" {}", ReporterApplication.class);
    }
}
