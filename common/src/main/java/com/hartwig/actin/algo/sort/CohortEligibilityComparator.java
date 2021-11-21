package com.hartwig.actin.algo.sort;

import java.util.Comparator;

import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.sort.CohortMetadataComparator;

import org.jetbrains.annotations.NotNull;

public class CohortEligibilityComparator implements Comparator<CohortEligibility> {

    private static final Comparator<CohortMetadata> METADATA_COMPARATOR = new CohortMetadataComparator();

    @Override
    public int compare(@NotNull CohortEligibility eligibility1, @NotNull CohortEligibility eligibility2) {
        int metadataCompare = METADATA_COMPARATOR.compare(eligibility1.metadata(), eligibility2.metadata());
        if (metadataCompare != 0) {
            return metadataCompare;
        }

        int overallEvaluationCompare = eligibility1.overallEvaluation().compareTo(eligibility2.overallEvaluation());
        if (overallEvaluationCompare != 0) {
            return overallEvaluationCompare;
        }

        return EvaluationMapCompareUtil.compareEvaluationMaps(eligibility1.evaluations(), eligibility2.evaluations());
    }
}
