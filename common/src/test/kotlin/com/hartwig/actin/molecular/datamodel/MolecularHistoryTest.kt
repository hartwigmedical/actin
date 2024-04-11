package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class MolecularHistoryTest {

    @Test
    fun `Should return most recent molecular record when multiple exist`() {

        val molecularRecords = listOf(
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = null),
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = LocalDate.of(2024, 1, 1)),
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = LocalDate.of(2023, 1, 1)),
        )

        val molecularHistory = MolecularHistory.fromInputs(molecularRecords, emptyList())
        assertThat(molecularHistory.latestMolecularRecord()).isEqualTo(molecularRecords[1])
    }

    @Test
    fun `Should return undated molecular record when no dated records exist`() {

        val molecularRecords = listOf(
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = null),
        )
        val molecularHistory = MolecularHistory.fromInputs(molecularRecords, emptyList())
        assertThat(molecularHistory.latestMolecularRecord()).isEqualTo(molecularRecords[0])
    }

    @Test
    fun `Should return all priorMolecular records`() {

        val molecularRecords = listOf(
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = null),
        )

        val priorMolecularTests = listOf(
            PriorMolecularTest("IHC", item = "protein1", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("IHC", item = "protein2", impliesPotentialIndeterminateStatus = false),
        )

        val molecularHistory = MolecularHistory.fromInputs(molecularRecords, priorMolecularTests)
        assertThat(molecularHistory.allPriorMolecularTests().sortedBy { it.item }).isEqualTo(priorMolecularTests)
    }
}