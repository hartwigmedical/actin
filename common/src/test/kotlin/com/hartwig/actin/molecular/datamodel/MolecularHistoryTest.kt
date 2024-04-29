package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.archerPriorMolecularNoFusionsFoundRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.archerPriorMolecularVariantRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.avlPanelPriorMolecularNoMutationsFoundRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.avlPanelPriorMolecularVariantRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.freetextPriorMolecularFusionRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericFusion
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericVariant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
        assertThat(molecularHistory.latestOrangeMolecularRecord()).isEqualTo(molecularRecords[1])
    }

    @Test
    fun `Should return undated molecular record when no dated records exist`() {

        val molecularRecords = listOf(
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = null),
        )
        val molecularHistory = MolecularHistory.fromInputs(molecularRecords, emptyList())
        assertThat(molecularHistory.latestOrangeMolecularRecord()).isEqualTo(molecularRecords[0])
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
        assertThat(molecularHistory.latestOrangeMolecularRecord()).isEqualTo(molecularRecords.first())

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
            PriorMolecularTest(ARCHER_FP_LUNG_TARGET, item = "gene", impliesPotentialIndeterminateStatus = false))
        ).isEqualTo(ExperimentType.ARCHER)
    }

    @Test
    fun `Should classify AvL Panels`() {
        assertThat(MolecularTestFactory.classify(
            PriorMolecularTest(AVL_PANEL, impliesPotentialIndeterminateStatus = false))
        ).isEqualTo(ExperimentType.GENERIC_PANEL)
    }

    @Test
    fun `Should classify Free text curated Panels`() {
        assertThat(MolecularTestFactory.classify(
            PriorMolecularTest(FREE_TEXT_PANEL, impliesPotentialIndeterminateStatus = false))
        ).isEqualTo(ExperimentType.GENERIC_PANEL)
    }

    @Test
    fun `Should distinguish generic panel types`() {
        val genericPanelTests = listOf(
            avlPanelPriorMolecularNoMutationsFoundRecord(),
            freetextPriorMolecularFusionRecord("geneUp", "geneDown")
        )
        val molecularTests = MolecularTestFactory.fromPriorMolecular(genericPanelTests)
        assertThat(molecularTests).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.GENERIC_PANEL }).hasSize(2)

    }

    @Test
    fun `Should construct AvL panel from prior molecular`() {
        val priorMolecularTests = listOf(
            avlPanelPriorMolecularNoMutationsFoundRecord(),
            avlPanelPriorMolecularVariantRecord("gene", "c1A>T")
        )
        val molecularTests = GenericPanelMolecularTest.fromPriorMolecularTest(priorMolecularTests)

        val expected = GenericPanelMolecularTest(
            date = null,
            result = GenericPanel(GenericPanelType.AVL, variants = listOf(GenericVariant("gene", "c1A>T")))
        )
        assertThat(molecularTests).containsExactly(expected)
    }

    @Test
    fun `Should construct Freetext panel from prior molecular`() {
        val priorMolecularTests = listOf(freetextPriorMolecularFusionRecord("geneUp", "geneDown"))
        val molecularTests = GenericPanelMolecularTest.fromPriorMolecularTest(priorMolecularTests)

        val expected = GenericPanelMolecularTest(
            date = null,
            result = GenericPanel(
                GenericPanelType.FREE_TEXT,
                listOf(GenericFusion("geneUp", "geneDown"))
            )
        )
        assertThat(molecularTests).containsExactly(expected)
    }

    @Test
    fun `Should throw exception on unextractable freetext record`() {
        val record = PriorMolecularTest(
            test = "Freetext",
            item = "KRAS A1Z",
            measure = null,
            impliesPotentialIndeterminateStatus = false
        )
        val priorMolecularTests = listOf(record)
        assertThatThrownBy {
            GenericPanelMolecularTest.fromPriorMolecularTest(priorMolecularTests)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `Should construct Archer panel from prior molecular`() {
        val priorMolecularTests = listOf(archerPriorMolecularVariantRecord("gene", "c.1A>T"))
        val molecularTests = ArcherMolecularTest.fromPriorMolecularTests(priorMolecularTests)

        assertThat(molecularTests).containsExactly(
            ArcherMolecularTest(
                date = null,
                result = ArcherPanel(
                    variants = listOf(
                        ArcherVariant("gene", "c.1A>T")
                    ),
                    fusions = emptyList()
                )
            )
        )
    }

    @Test
    fun `Should convert and group all prior molecular tests`() {

        val IHCTests = listOf(
            PriorMolecularTest("IHC", item = "protein1", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("IHC", item = "protein2", impliesPotentialIndeterminateStatus = false),
        )

        val archerGroup1Tests = listOf(
            archerPriorMolecularNoFusionsFoundRecord(),
            archerPriorMolecularVariantRecord("gene", "c.1A>T"),
            archerPriorMolecularVariantRecord("gene", "c.5G>C")
        )

        val archerGroup2Tests = listOf(
            archerPriorMolecularVariantRecord("gene", "c.5G>C", LocalDate.of(2020, 1, 1))
        )

        val genericPanelTests = listOf(
            avlPanelPriorMolecularNoMutationsFoundRecord(),
            freetextPriorMolecularFusionRecord("geneUp", "geneDown")
        )

        val otherTests = listOf(
            PriorMolecularTest("Future-Panel", item = "gene", impliesPotentialIndeterminateStatus = false)
        )

        val priorMolecularTests = IHCTests + archerGroup1Tests + archerGroup2Tests + genericPanelTests + otherTests

        val molecularTests = MolecularTestFactory.fromPriorMolecular(priorMolecularTests)
        assertThat(molecularTests).hasSize(7)
        assertThat(molecularTests.filter { it.type == ExperimentType.IHC }).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.ARCHER }).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.GENERIC_PANEL }).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.OTHER }).hasSize(1)
    }
}