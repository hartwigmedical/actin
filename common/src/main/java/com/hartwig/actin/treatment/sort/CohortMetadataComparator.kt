package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.CohortMetadata
import java.lang.Boolean
import kotlin.Comparator
import kotlin.Int

class CohortMetadataComparator() : Comparator<CohortMetadata> {
    public override fun compare(metadata1: CohortMetadata, metadata2: CohortMetadata): Int {
        val idCompare: Int = metadata1.cohortId().compareTo(metadata2.cohortId())
        if (idCompare != 0) {
            return idCompare
        }
        val descriptionCompare: Int = metadata1.description().compareTo(metadata2.description())
        if (descriptionCompare != 0) {
            return descriptionCompare
        }
        val openCompare: Int = Boolean.compare(metadata2.open(), metadata1.open())
        if (openCompare != 0) {
            return openCompare
        }
        val slotsAvailableCompare: Int = Boolean.compare(metadata2.slotsAvailable(), metadata1.slotsAvailable())
        if (slotsAvailableCompare != 0) {
            return slotsAvailableCompare
        }
        return Boolean.compare(metadata1.blacklist(), metadata2.blacklist())
    }
}
