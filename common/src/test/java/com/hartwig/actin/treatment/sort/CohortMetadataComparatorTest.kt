package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata

class CohortMetadataComparatorTest {
    @org.junit.Test
    fun canSortCohortMetadata() {
        val builder: ImmutableCohortMetadata.Builder = ImmutableCohortMetadata.builder().evaluable(true).slotsAvailable(true)
        val metadata1: CohortMetadata = builder.cohortId("A").description("A First").open(true).blacklist(false).build()
        val metadata2: CohortMetadata = builder.cohortId("A").description("A First").open(false).blacklist(false).build()
        val metadata3: CohortMetadata = builder.cohortId("A").description("Second A").open(true).blacklist(false).build()
        val metadata4: CohortMetadata = builder.cohortId("B").description("B Third").open(true).blacklist(false).build()
        val metadata5: CohortMetadata = builder.cohortId("A").description("A First").open(false).blacklist(true).build()
        val metadata: List<CohortMetadata> =
            com.google.common.collect.Lists.newArrayList<CohortMetadata>(metadata1, metadata2, metadata3, metadata4, metadata5)
        metadata.sort(CohortMetadataComparator())
        org.junit.Assert.assertEquals(metadata1, metadata[0])
        org.junit.Assert.assertEquals(metadata2, metadata[1])
        org.junit.Assert.assertEquals(metadata5, metadata[2])
        org.junit.Assert.assertEquals(metadata3, metadata[3])
        org.junit.Assert.assertEquals(metadata4, metadata[4])
    }
}