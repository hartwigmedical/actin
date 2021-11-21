package com.hartwig.actin.treatment.sort;

import java.util.Comparator;

import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.Eligibility;

import org.jetbrains.annotations.NotNull;

public class CohortComparator implements Comparator<Cohort> {

    private static final Comparator<CohortMetadata> METADATA_COMPARATOR = new CohortMetadataComparator();
    private static final Comparator<Eligibility> ELIGIBILITY_COMPARATOR = new EligibilityComparator();

    @Override
    public int compare(@NotNull Cohort cohort1, @NotNull Cohort cohort2) {
        int metadataCompare = METADATA_COMPARATOR.compare(cohort1.metadata(), cohort2.metadata());
        if (metadataCompare != 0) {
            return metadataCompare;
        }

        int sizeCompare = cohort1.eligibility().size() - cohort2.eligibility().size();
        if (sizeCompare != 0) {
            return sizeCompare > 0 ? 1 : -1;
        }

        int index = 0;
        while (index < cohort1.eligibility().size()) {
            int eligibilityCompare = ELIGIBILITY_COMPARATOR.compare(cohort1.eligibility().get(index), cohort2.eligibility().get(index));
            if (eligibilityCompare != 0) {
                return eligibilityCompare;
            }
        }

        return 0;
    }
}
