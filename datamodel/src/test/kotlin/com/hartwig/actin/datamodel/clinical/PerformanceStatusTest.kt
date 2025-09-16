package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PerformanceStatusTest {

    @Test
    fun `Should return the latest WHO status`() {
        assertThat(PerformanceStatus(emptyList(), emptyList()).latestWho).isNull()
        assertThat(PerformanceStatus(listOf(WhoStatus(LocalDate.now(), 1)), emptyList()).latestWho).isEqualTo(1)
        assertThat(
            PerformanceStatus(
                listOf(
                    WhoStatus(LocalDate.now(), 2), WhoStatus(LocalDate.now().minusDays(1), 1)
                ),
                emptyList()
            ).latestWho
        ).isEqualTo(2)
    }

    @Test
    fun `Should return the latest ASA score`() {
        assertThat(PerformanceStatus(emptyList(), emptyList()).latestAsa).isNull()
        assertThat(PerformanceStatus(emptyList(), listOf(AsaScore(LocalDate.now(), 1))).latestAsa).isEqualTo(1)
        assertThat(
            PerformanceStatus(
                emptyList(),
                listOf(
                    AsaScore(LocalDate.now(), 2), AsaScore(LocalDate.now().minusDays(1), 1)
                )
            ).latestAsa
        ).isEqualTo(2)
    }

}