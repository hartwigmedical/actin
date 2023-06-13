package com.hartwig.actin.treatment.ctc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CohortStatusInterpreter {

    private static final Logger LOGGER = LogManager.getLogger(CohortStatusInterpreter.class);

    private static final String NOT_AVAILABLE = "NA";
    private static final String NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY = "not_in_ctc_overview_unknown_why";
    private static final String WONT_BE_MAPPED_BECAUSE_CLOSED = "wont_be_mapped_because_closed";
    private static final String WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE = "wont_be_mapped_because_not_available";

    @Nullable
    public static InterpretedCohortStatus interpret(@NotNull List<CTCDatabaseEntry> entries, @NotNull CohortDefinitionConfig cohortConfig) {
        Set<String> ctcCohortIds = cohortConfig.ctcCohortIds();
        if (isNotAvailable(ctcCohortIds)) {
            LOGGER.debug("CTC entry for cohort '{}' of trial '{}' explicitly configured to be unavailable",
                    cohortConfig.cohortId(),
                    cohortConfig.trialId());
            return null;
        } else if (isMissingEntry(ctcCohortIds)) {
            LOGGER.info("CTC entry missing for unknown reason for cohort '{}' of trial '{}'! Setting cohort to closed without slots",
                    cohortConfig.cohortId(),
                    cohortConfig.trialId());
            return closedWithoutSlots();
        } else if (isMissingBecauseClosedOrUnavailable(ctcCohortIds)) {
            LOGGER.debug("CTC entry missing for cohort '{}' of trial '{}' because it's assumed closed or not available. "
                    + "Setting cohort to closed without slots", cohortConfig.cohortId(), cohortConfig.trialId());
            return closedWithoutSlots();
        }

        Set<Integer> configuredCohortIds = ctcCohortIds.stream().map(Integer::parseInt).collect(Collectors.toSet());
        return consolidatedCohortStatus(entries, configuredCohortIds);
    }

    @NotNull
    private static InterpretedCohortStatus consolidatedCohortStatus(@NotNull List<CTCDatabaseEntry> entries,
            @NotNull Set<Integer> configuredCohortIds) {
        boolean hasAtLeastOneOpen = false;
        boolean hasAtLeastOneSlotsAvailable = false;
        for (CTCDatabaseEntry entry : entries) {
            if (configuredCohortIds.contains(entry.cohortId())) {
                String cohortStatus = entry.cohortStatus();
                if (cohortStatus == null) {
                    LOGGER.warn("Cohort status missing for CTC entry with cohort ID: {}: {}", entry.cohortId(), entry);
                } else if (CTCStatus.fromStatusString(entry.cohortStatus()) == CTCStatus.OPEN) {
                    hasAtLeastOneOpen = true;
                }

                Integer cohortSlotsAvailable = entry.cohortSlotsNumberAvailable();
                if (cohortSlotsAvailable != null && cohortSlotsAvailable > 0) {
                    hasAtLeastOneSlotsAvailable = true;
                }
            }
        }

        return ImmutableInterpretedCohortStatus.builder().open(hasAtLeastOneOpen).slotsAvailable(hasAtLeastOneSlotsAvailable).build();
    }

    private static boolean isNotAvailable(@NotNull Set<String> ctcCohortIds) {
        return isSpecificValue(ctcCohortIds, NOT_AVAILABLE);
    }

    private static boolean isMissingEntry(@NotNull Set<String> ctcCohortIds) {
        return isSpecificValue(ctcCohortIds, NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY);
    }

    private static boolean isMissingBecauseClosedOrUnavailable(@NotNull Set<String> ctcCohortIds) {
        return isSpecificValue(ctcCohortIds, WONT_BE_MAPPED_BECAUSE_CLOSED) || isSpecificValue(ctcCohortIds,
                WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE);
    }

    private static boolean isSpecificValue(@NotNull Set<String> ctcCohortIds, @NotNull String valueToFind) {
        return ctcCohortIds.size() == 1 && ctcCohortIds.stream().allMatch(cohortId -> cohortId.equals(valueToFind));
    }

    @NotNull
    private static InterpretedCohortStatus closedWithoutSlots() {
        return ImmutableInterpretedCohortStatus.builder().open(false).slotsAvailable(false).build();
    }
}
