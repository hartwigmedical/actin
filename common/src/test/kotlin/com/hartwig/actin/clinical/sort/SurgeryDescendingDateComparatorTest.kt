package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class SurgeryDescendingDateComparatorTest {

    val endDate = LocalDateTime.of(2024, 9, 19, 1, 1).toLocalDate()

    @Test
    fun `Should sort by descending based on end date`() {

        val surgeries = listOf(
            Surgery(name = "Surgery 2", endDate = endDate.minusDays(6), status = SurgeryStatus.FINISHED),
            Surgery(name = null, endDate = endDate.minusDays(4), status = SurgeryStatus.FINISHED),
            Surgery(name = "Surgery 1", endDate = endDate.minusDays(2), status = SurgeryStatus.FINISHED),
        )

        val sortedSurgeries = surgeries.sortedWith(SurgeryDescendingDateComparator())
        assertThat(sortedSurgeries.size).isEqualTo(3)
        assertThat(sortedSurgeries[0].name).isEqualTo("Surgery 1")
        assertThat(sortedSurgeries[1].name).isEqualTo(null)
        assertThat(sortedSurgeries[2].name).isEqualTo("Surgery 2")

    }
}