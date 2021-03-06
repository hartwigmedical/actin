package com.hartwig.actin.report.interpretation;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvaluatedTrialComparator implements Comparator<EvaluatedTrial> {

    private static final String COMBINATION_COHORT_IDENTIFIER = "+";

    @Override
    public int compare(@NotNull EvaluatedTrial trial1, @NotNull EvaluatedTrial trial2) {
        int hasMolecularCompare = Boolean.compare(trial1.molecularEvents().isEmpty(), trial2.molecularEvents().isEmpty());
        if (hasMolecularCompare != 0) {
            return hasMolecularCompare;
        }

        int trialCompare = trial1.trialId().compareTo(trial2.trialId());
        if (trialCompare != 0) {
            return trialCompare;
        }

        int cohortCompare = compareCohorts(!trial1.molecularEvents().isEmpty(), trial1.cohort(), trial2.cohort());
        if (cohortCompare != 0) {
            return cohortCompare;
        }

        return compareSets(trial1.molecularEvents(), trial2.molecularEvents());
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
