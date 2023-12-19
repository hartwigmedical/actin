package com.hartwig.actin.trial.sort

import com.hartwig.actin.trial.datamodel.CohortMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class CohortMetadataComparatorTest {
   
    @Test
    fun `Should sort cohort metadata`() {
        val metadata1 = metadata("A", "A First", open = true, blacklist = false)
        val metadata2 = metadata("A", "A First", open = false, blacklist = false)
        val metadata3 = metadata("A", "Second A", open = true, blacklist = false)
        val metadata4 = metadata("B", "B Third", open = true, blacklist = false)
        val metadata5 = metadata("A", "A First", open = false, blacklist = true)
        val metadata = listOf(metadata1, metadata2, metadata3, metadata4, metadata5).sortedWith(CohortMetadataComparator())

        assertThat(metadata[0]).isEqualTo(metadata1)
        assertThat(metadata[1]).isEqualTo(metadata2)
        assertThat(metadata[2]).isEqualTo(metadata5)
        assertThat(metadata[3]).isEqualTo(metadata3)
        assertThat(metadata[4]).isEqualTo(metadata4)
    }

    private fun metadata(cohortId: String, description: String, open: Boolean, blacklist: Boolean): CohortMetadata {
        return CohortMetadata(
            evaluable = true,
            slotsAvailable = true,
            cohortId = cohortId,
            description = description,
            open = open,
            blacklist = blacklist
        )
    }
}