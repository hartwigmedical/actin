package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.CohortMetadata

class CohortMetadataComparator : Comparator<CohortMetadata> {

    private val comparator = Comparator.comparing(CohortMetadata::cohortId)
        .thenComparing(CohortMetadata::description)
        .thenComparing(CohortMetadata::open, reverseOrder())
        .thenComparing(CohortMetadata::slotsAvailable, reverseOrder())
        .thenComparing(CohortMetadata::blacklist)

    override fun compare(metadata1: CohortMetadata, metadata2: CohortMetadata): Int {
        return comparator.compare(metadata1, metadata2)
    }
}
