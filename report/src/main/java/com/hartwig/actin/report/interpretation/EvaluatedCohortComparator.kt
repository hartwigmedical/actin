package com.hartwig.actin.report.interpretation;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvaluatedCohortComparator implements Comparator<EvaluatedCohort> {

    private static final String COMBINATION_COHORT_IDENTIFIER = "+";

    @Override
    public int compare(@NotNull EvaluatedCohort evaluatedCohort1, @NotNull EvaluatedCohort evaluatedCohort2) {
        int hasSlotsAvailableCompare = Boolean.compare(!evaluatedCohort1.hasSlotsAvailable(), !evaluatedCohort2.hasSlotsAvailable());
        if (hasSlotsAvailableCompare != 0) {
            return hasSlotsAvailableCompare;
        }

        int hasMolecularCompare =
                Boolean.compare(evaluatedCohort1.molecularEvents().isEmpty(), evaluatedCohort2.molecularEvents().isEmpty());
        if (hasMolecularCompare != 0) {
            return hasMolecularCompare;
        }

        int trialCompare = evaluatedCohort1.trialId().compareTo(evaluatedCohort2.trialId());
        if (trialCompare != 0) {
            return trialCompare;
        }

        int cohortCompare =
                compareCohorts(!evaluatedCohort1.molecularEvents().isEmpty(), evaluatedCohort1.cohort(), evaluatedCohort2.cohort());
        if (cohortCompare != 0) {
            return cohortCompare;
        }

        return compareSets(evaluatedCohort1.molecularEvents(), evaluatedCohort2.molecularEvents());
    }

    private static int compareCohorts(boolean hasMolecular, @Nullable String cohort1, @Nullable String cohort2) {
        if (cohort1 == null) {
            return cohort2 != null ? -1 : 0;
        } else if (cohort2 == null) {
            return 1;
        }

        if (hasMolecular) {
            int hasCombinationCohort =
                    Boolean.compare(cohort2.contains(COMBINATION_COHORT_IDENTIFIER), cohort1.contains(COMBINATION_COHORT_IDENTIFIER));
            if (hasCombinationCohort != 0) {
                return hasCombinationCohort;
            }
        }

        return cohort1.compareTo(cohort2);
    }

    private static int compareSets(@NotNull Set<String> set1, @NotNull Set<String> set2) {
        int countCompare = set2.size() - set1.size();
        if (countCompare != 0) {
            return countCompare > 0 ? 1 : -1;
        }

        Iterator<String> iterator1 = set1.iterator();
        Iterator<String> iterator2 = set2.iterator();
        while (iterator1.hasNext()) {
            int valueCompare = iterator1.next().compareTo(iterator2.next());
            if (valueCompare != 0) {
                return valueCompare;
            }
        }
        return 0;
    }
}
