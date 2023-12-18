package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.CohortMetadata

class CohortMetadataComparator : Comparator<CohortMetadata> {
    private val comparator = Comparator.comparing(CohortMetadata::cohortId)
        .thenComparing(CohortMetadata::description)
        .thenComparing(CohortMetadata::open)
        .thenComparing(CohortMetadata::slotsAvailable)
        .thenComparing(CohortMetadata::blacklist)

    override fun compare(metadata1: CohortMetadata, metadata2: CohortMetadata): Int {
        return comparator.compare(metadata1, metadata2)
    }
}
