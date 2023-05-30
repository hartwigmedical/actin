package com.hartwig.actin.treatment.ctc;

import java.io.IOException;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.treatment.ctc.config.CTCDatabase;
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseReader;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;

import org.jetbrains.annotations.NotNull;

public class CTCModel {

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
        /*
        not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
not_in_ctc_overview_unknown_why
wont_be_mapped_because_closed
wont_be_mapped_because_closed
wont_be_mapped_because_closed
wont_be_mapped_because_not_available
         */
        return ImmutableCohortMetadata.builder()
                .cohortId(cohortConfig.cohortId())
                .evaluable(cohortConfig.evaluable())
                .open(cohortConfig.open())
                .slotsAvailable(cohortConfig.slotsAvailable())
                .blacklist(cohortConfig.blacklist())
                .description(cohortConfig.description())
                .build();
    }
}
