package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
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
    fun `Should classify Archer tests`() {
        assertThat(MolecularTestFactory.classify(
            PriorMolecularTest("Archer FP Lung Target", item = "gene", impliesPotentialIndeterminateStatus = false))
        ).isEqualTo(ExperimentType.ARCHER)
    }

    @Test
    fun `Should classify AvL Panels`() {
        assertThat(MolecularTestFactory.classify(
            PriorMolecularTest("AvL Panel", impliesPotentialIndeterminateStatus = false))
        ).isEqualTo(ExperimentType.GENERIC_PANEL)
    }

    @Test
    fun `Should convert all prior molecular tests`() {

        val IHCTests = listOf(
            PriorMolecularTest("IHC", item = "protein1", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("IHC", item = "protein2", impliesPotentialIndeterminateStatus = false),
        )

        val archerGroup1Tests = listOf(
            PriorMolecularTest("Archer FP Lung Target", item = null, measure = "GEEN fusie(s) aangetoond", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("Archer FP Lung Target", item = "gene", measure = "c.1A>T", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("Archer FP Lung Target", item = "gene", measure = "c.5G>C", impliesPotentialIndeterminateStatus = false)
        )

        val archerGroup2Tests = listOf(
            PriorMolecularTest("Archer FP Lung Target", measureDate = LocalDate.of(2020, 1, 1), item = "gene", measure = "c.5G>C", impliesPotentialIndeterminateStatus = false),
        )

        val genericPanelTests = listOf(
            PriorMolecularTest("AvL Panel", item = null, measure = "GEEN mutaties aangetoond met behulp van het AVL Panel", impliesPotentialIndeterminateStatus = false),
        )

        val otherTests = listOf(
            PriorMolecularTest("Future-Panel", item = "gene", impliesPotentialIndeterminateStatus = false)
        )


        val priorMolecularTests = IHCTests + archerGroup1Tests + archerGroup2Tests + genericPanelTests + otherTests

        val molecularTests = MolecularTestFactory.fromPriorMolecular(priorMolecularTests)
        assertThat(molecularTests).hasSize(6)
        assertThat(molecularTests.filter { it.type == ExperimentType.IHC }).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.ARCHER }).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.GENERIC_PANEL }).hasSize(1)
        assertThat(molecularTests.filter { it.type == ExperimentType.OTHER }).hasSize(1)
    }

    @Test
    fun `Should return latest archer panel when multiple exist`() {

        val emptyPanel = ArcherPanel(null, emptyList(), emptyList())

        val archerPanels = listOf(
            emptyPanel,
            emptyPanel.copy(date = LocalDate.of(2024, 1, 1)),
            emptyPanel.copy(date = LocalDate.of(2023, 1, 1)),
        )

        val molecularHistory = MolecularHistory(archerPanels.map { ArcherMolecularTest(ExperimentType.ARCHER, it.date, it) })
        assertThat(molecularHistory.latestArcherPanel()?.date).isEqualTo(LocalDate.of(2024, 1, 1))
    }
}