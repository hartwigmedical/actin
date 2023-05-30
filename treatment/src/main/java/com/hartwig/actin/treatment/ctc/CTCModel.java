package com.hartwig.actin.treatment.ctc;

import java.io.IOException;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.treatment.ctc.config.CTCDatabase;
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseReader;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class CTCModel {

    private static final Logger LOGGER = LogManager.getLogger(CTCModel.class);

    @NotNull
    private final CTCDatabase ctcDatabase;

    @NotNull
    public static CTCModel createFromCTCConfigDirectory(@NotNull String ctcConfigDirectory) throws IOException {
        return new CTCModel(CTCDatabaseReader.read(ctcConfigDirectory));
    }

    @VisibleForTesting
    CTCModel(@NotNull CTCDatabase ctcDatabase) {
        this.ctcDatabase = ctcDatabase;
    }

    @NotNull
    public CohortMetadata resolveForCohortConfig(@NotNull CohortDefinitionConfig cohortConfig) {
        InterpretedCohortStatus interpretedCohortStatus = CohortStatusInterpreter.interpret(ctcDatabase.entries(), cohortConfig);

        if (interpretedCohortStatus == null) {
            interpretedCohortStatus = fromCohortConfig(cohortConfig);
        }

        return ImmutableCohortMetadata.builder()
                .cohortId(cohortConfig.cohortId())
                .evaluable(cohortConfig.evaluable())
                .open(interpretedCohortStatus.open())
                .slotsAvailable(interpretedCohortStatus.slotsAvailable())
                .blacklist(cohortConfig.blacklist())
                .description(cohortConfig.description())
                .build();
    }

    @NotNull
    private static InterpretedCohortStatus fromCohortConfig(@NotNull CohortDefinitionConfig cohortConfig) {
        Boolean open = cohortConfig.open();
        Boolean slotsAvailable = cohortConfig.slotsAvailable();

        if (open == null || slotsAvailable == null) {
            LOGGER.warn("Missing open and slots available data for cohort '{}' of trial '{}'. "
                    + "Assuming cohort is closed with no slots available", cohortConfig.cohortId(), cohortConfig.trialId());
            open = false;
            slotsAvailable = false;
        }

        return ImmutableInterpretedCohortStatus.builder().open(open).slotsAvailable(slotsAvailable).build();
    }
}
