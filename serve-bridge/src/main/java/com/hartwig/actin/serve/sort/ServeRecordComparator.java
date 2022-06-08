package com.hartwig.actin.serve.sort;

import java.util.Comparator;

import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServeRecordComparator implements Comparator<ServeRecord> {

    @Override
    public int compare(@NotNull ServeRecord record1, @NotNull ServeRecord record2) {
        int trialCompare = record1.trial().compareTo(record2.trial());
        if (trialCompare != 0) {
            return trialCompare;
        }

        int cohortCompare = compareStrings(record1.cohort(), record2.cohort());
        if (cohortCompare != 0) {
            return cohortCompare;
        }

        int ruleCompare = record1.rule().compareTo(record2.rule());
        if (ruleCompare != 0) {
            return ruleCompare;
        }

        int geneCompare = compareStrings(record1.gene(), record2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        int mutationCompare = compareStrings(record1.mutation(), record2.mutation());
        if (mutationCompare != 0) {
            return mutationCompare;
        }

        return Boolean.compare(record1.isUsedAsInclusion(), record2.isUsedAsInclusion());
    }

    private static int compareStrings(@Nullable String string1, @Nullable String string2) {
        if (string1 == null && string2 == null) {
            return 0;
        } else if (string1 == null) {
            return 1;
        } else if (string2 == null) {
            return -1;
        } else {
            return string1.compareTo(string2);
        }
    }
}
