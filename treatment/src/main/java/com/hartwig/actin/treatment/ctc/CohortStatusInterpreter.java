package com.hartwig.actin.treatment.ctc;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CohortStatusInterpreter {

    private static final Logger LOGGER = LogManager.getLogger(CohortStatusInterpreter.class);

    static final String NOT_AVAILABLE = "NA";
    static final String NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY = "not_in_ctc_overview_unknown_why";
    static final String WONT_BE_MAPPED_BECAUSE_CLOSED = "wont_be_mapped_because_closed";
    static final String WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE = "wont_be_mapped_because_not_available";

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
        List<CTCDatabaseEntry> matches = findEntriesByCohortIds(entries, configuredCohortIds);

        if (!hasValidCTCDatabaseMatches(matches)) {
            LOGGER.warn("Invalid cohort IDs configured for cohort '{}' of trial '{}': '{}'. Assuming cohort is closed without slots",
                    cohortConfig.cohortId(),
                    cohortConfig.trialId(),
                    configuredCohortIds);
            return closedWithoutSlots();
        }

        return consolidatedCohortStatus(entries, matches);
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

    @VisibleForTesting
    static boolean hasValidCTCDatabaseMatches(@NotNull List<CTCDatabaseEntry> matches) {
        if (matches.stream().anyMatch(Objects::isNull)) {
            return false;
        }

        return isSingleParent(matches) || isListOfChildren(matches);
    }

    @NotNull
    private static InterpretedCohortStatus consolidatedCohortStatus(@NotNull List<CTCDatabaseEntry> allEntries,
            @NotNull List<CTCDatabaseEntry> matches) {
        if (isSingleParent(matches)) {
            return fromEntry(matches.get(0));
        } else if (isListOfChildren(matches)) {
            InterpretedCohortStatus best =
                    matches.stream().map(CohortStatusInterpreter::fromEntry).max(new InterpretedCohortStatusComparator()).orElseThrow();

            Integer firstParentId = matches.get(0).cohortParentId();
            if (matches.size() > 1) {
                if (matches.stream().anyMatch(entry -> !Objects.equals(entry.cohortParentId(), firstParentId))) {
                    LOGGER.warn("Multiple parents found for single set of children: {}", matches);
                }
            }

            InterpretedCohortStatus firstParentStatus = fromEntry(findEntryByCohortId(allEntries, firstParentId));
            if (!best.equals(firstParentStatus)) {
                LOGGER.warn("Inconsistent status between best child and parent cohort in CTC for cohort with parent ID '{}'",
                        matches.get(0).cohortParentId());
            }

            return best;
        }

        throw new IllegalStateException("Unexpected set of CTC database matches: " + matches);
    }

    private static boolean isSingleParent(@NotNull List<CTCDatabaseEntry> matches) {
        return matches.size() == 1 && !isChild(matches.get(0));
    }

    private static boolean isListOfChildren(@NotNull List<CTCDatabaseEntry> matches) {
        if (matches.size() < 1) {
            return false;
        }

        return matches.stream().allMatch(CohortStatusInterpreter::isChild);
    }

    private static boolean isChild(@NotNull CTCDatabaseEntry entry) {
        return entry.cohortParentId() != null;
    }

    @NotNull
    private static List<CTCDatabaseEntry> findEntriesByCohortIds(@NotNull List<CTCDatabaseEntry> entries,
            @NotNull Set<Integer> configuredCohortIds) {
        return configuredCohortIds.stream().map(cohortId -> {
            CTCDatabaseEntry entry = findEntryByCohortId(entries, cohortId);
            if (entry == null) {
                LOGGER.warn("Could not find CTC database entry with cohort ID '{}'", cohortId);
            }
            return entry;
        }).collect(Collectors.toList());
    }

    @Nullable
    private static CTCDatabaseEntry findEntryByCohortId(@NotNull List<CTCDatabaseEntry> entries, int cohortIdToFind) {
        return entries.stream().filter(entry -> {
            Integer cohortId = entry.cohortId();
            return cohortId != null && cohortId == cohortIdToFind;
        }).findFirst().orElse(null);
    }

    @NotNull
    private static InterpretedCohortStatus closedWithoutSlots() {
        return create(false, false);
    }

    @NotNull
    private static InterpretedCohortStatus fromEntry(@NotNull CTCDatabaseEntry entry) {
        String cohortStatus = entry.cohortStatus();
        if (cohortStatus == null) {
            LOGGER.warn("No cohort status available in CTC for cohort with ID '{}'. Assuming cohort is closed without slots",
                    entry.cohortId());
            return closedWithoutSlots();
        }
        Integer numberSlotsAvailable = entry.cohortSlotsNumberAvailable();
        CTCStatus status = CTCStatus.fromStatusString(cohortStatus);
        boolean slotsAvailable;
        if (numberSlotsAvailable == null && status == CTCStatus.OPEN) {
            LOGGER.warn("No data available on number of slots for open cohort with ID '{}'. Assuming no slots available", entry.cohortId());
            slotsAvailable = false;
        } else {
            slotsAvailable = numberSlotsAvailable != null && numberSlotsAvailable > 0;
        }

        return create(status == CTCStatus.OPEN, slotsAvailable);
    }

    @NotNull
    private static InterpretedCohortStatus create(boolean open, boolean slotsAvailable) {
        return ImmutableInterpretedCohortStatus.builder().open(open).slotsAvailable(slotsAvailable).build();
    }
}
