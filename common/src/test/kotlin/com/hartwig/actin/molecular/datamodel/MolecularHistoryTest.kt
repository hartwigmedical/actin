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
            PriorMolecularTest("Future-Panel", item = "gene", impliesPotentialIndeterminateStatus = false)
        )

        val molecularHistory = MolecularHistory.fromInputs(molecularRecords, priorMolecularTests)
        assertThat(molecularHistory.latestMolecularRecord()).isEqualTo(molecularRecords.first())

        assertThat(molecularHistory.allIHCTests().sortedBy { it.item })
            .isEqualTo(priorMolecularTests.filter { it.test == "IHC" }.sortedBy { it.item })
    }

    @Test
    fun `Should classify IHC tests`() {
        assertThat(MolecularTestFactory.classify(
            PriorMolecularTest("IHC", item = "protein1", impliesPotentialIndeterminateStatus = false))
        ).isEqualTo(ExperimentType.IHC)
    }

    @Test
    fun `Should classify PD-L1 tests`() {
        assertThat(MolecularTestFactory.classify(
            PriorMolecularTest("", item = "PD-L1", impliesPotentialIndeterminateStatus = false))
        ).isEqualTo(ExperimentType.IHC)
    }

    @Test
    fun `Should classify unsupported tests as other`() {
        assertThat(MolecularTestFactory.classify(
            PriorMolecularTest("Future-Panel", item = "gene", impliesPotentialIndeterminateStatus = false))
        ).isEqualTo(ExperimentType.OTHER)
    }

    @Test
    fun `Should convert all prior molecular tests`() {
        val priorMolecularTests = listOf(
            PriorMolecularTest("IHC", item = "protein1", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("IHC", item = "protein2", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("Future-Panel", item = "gene", impliesPotentialIndeterminateStatus = false),
        )

        val molecularTests = MolecularTestFactory.fromPriorMolecular(priorMolecularTests)
        assertThat(molecularTests.filter { it.type == ExperimentType.IHC }).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.OTHER }).hasSize(1)
    }
}