package com.hartwig.actin.report.interpretation;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class EvaluatedTrialComparator implements Comparator<EvaluatedTrial> {

    @Override
    public int compare(@NotNull EvaluatedTrial trial1, @NotNull EvaluatedTrial trial2) {
        int hasMolecularCompare = compareSets(trial1.molecularEvents(), trial2.molecularEvents());
        if (hasMolecularCompare != 0) {
            return hasMolecularCompare;
        }

        int trialCompare = trial1.trialId().compareTo(trial2.trialId());
        if (trialCompare != 0) {
            return trialCompare;
        }

        if (trial1.cohort() == null) {
            return trial2.cohort() != null ? -1 : 0;
        } else {
            return trial1.cohort().compareTo(trial2.cohort());
        }
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
