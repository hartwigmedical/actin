package com.hartwig.actin.algo.sort;

import java.util.Comparator;

import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.sort.CohortMetadataComparator;

import org.jetbrains.annotations.NotNull;

public class CohortMatchComparator implements Comparator<CohortMatch> {

    private static final Comparator<CohortMetadata> METADATA_COMPARATOR = new CohortMetadataComparator();

    @Override
    public int compare(@NotNull CohortMatch eligibility1, @NotNull CohortMatch eligibility2) {
        int metadataCompare = METADATA_COMPARATOR.compare(eligibility1.metadata(), eligibility2.metadata());
        if (metadataCompare != 0) {
            return metadataCompare;
        }

        int isPotentiallyEligibleCompare = Boolean.compare(eligibility1.isPotentiallyEligible(), eligibility2.isPotentiallyEligible());
        if (isPotentiallyEligibleCompare != 0) {
            return isPotentiallyEligibleCompare;
        }

        return EvaluationMapCompare.compare(eligibility1.evaluations(), eligibility2.evaluations());
    }
}
