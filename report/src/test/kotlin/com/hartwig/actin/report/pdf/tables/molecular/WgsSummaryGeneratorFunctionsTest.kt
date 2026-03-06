package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import com.hartwig.actin.datamodel.molecular.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.datamodel.molecular.panel.TestVersion
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.pdf.SummaryType
import com.hartwig.actin.report.pdf.tables.CellTestUtil
import com.hartwig.actin.report.pdf.util.Tables
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class WgsSummaryGeneratorFunctionsTest {

    private val molecularRecord = TestMolecularFactory.createProperWholeGenomeTest()
    private val inconclusivePredictions = listOf(
        CupPrediction(
            cancerType = "Melanoma",
            likelihood = 0.60,
            snvPairwiseClassifier = 0.979,
            genomicPositionClassifier = 0.99,
            featureClassifier = 0.972,
            cuppaMode = CuppaMode.WGS
        ),
        CupPrediction(
            cancerType = "Lung",
            likelihood = 0.20,
            snvPairwiseClassifier = 0.0009,
            genomicPositionClassifier = 0.011,
            featureClassifier = 0.0102,
            cuppaMode = CuppaMode.WGS
        ),
    )
    private val inconclusiveCharacteristics =
        molecularRecord.characteristics.copy(predictedTumorOrigin = PredictedTumorOrigin(inconclusivePredictions))

    @Test
    fun `Should return events concatenated and with warning string`() {
        val drivers = listOf(
            TestCopyNumberFactory.createMinimal().copy(
                event = "event 1",
                driverLikelihood = null,
                canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(minCopies = 4)
            ),
            TestFusionFactory.createMinimal().copy(event = "event 2", driverLikelihood = null),
            TestFusionFactory.createMinimal().copy(event = "event 3", driverLikelihood = DriverLikelihood.LOW),
            TestFusionFactory.createMinimal().copy(event = "event 4", driverLikelihood = DriverLikelihood.MEDIUM),
            TestFusionFactory.createMinimal().copy(event = "event 5", driverLikelihood = DriverLikelihood.HIGH)
        )
        val cell = WgsSummaryGeneratorFunctions.potentiallyActionableEventsCell(drivers, 2.5)

        assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo(
                "event 1 (4 copies - with tumor ploidy 2.5), event 2 (annotated as not a driver), event 3 (low driver likelihood), " +
                        "event 4 (medium driver likelihood), event 5"
            )
    }

    @Test
    fun `Should return none when list of events is empty`() {
        val drivers = emptyList<Driver>()
        val cell = WgsSummaryGeneratorFunctions.potentiallyActionableEventsCell(drivers)

        assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("None")
    }

    @Test
    fun `Should add '(low purity)' to predicted tumor origin when conclusive with sufficient quality and insufficient purity`() {
        val cell = WgsSummaryGeneratorFunctions.tumorOriginPredictionCell(molecular = molecularRecord.copy(hasSufficientPurity = false))

        assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("Melanoma (100%) (low purity)")
    }

    @Test
    fun `Should add '(low purity)' to predicted tumor origin when inconclusive with sufficient quality and insufficient purity`() {
        val cell = WgsSummaryGeneratorFunctions.tumorOriginPredictionCell(
            molecular = molecularRecord.copy(characteristics = inconclusiveCharacteristics).copy(hasSufficientPurity = false)
        )

        assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("Inconclusive (Melanoma 60%, Lung 20%) (low purity)")
    }

    @Test
    fun `Should not create tumor mutational cell when result is unknown and summary table configuration is not long summary type`() {
        val record = molecularRecord.copy(
            characteristics = molecularRecord.characteristics.copy(tumorMutationalLoad = null, tumorMutationalBurden = null)
        )
        val hasTmbTmlCells = WgsSummaryGeneratorFunctions.createTmbCells(record, false, Tables.createFixedWidthCols(100f, 100f))
        assertThat(hasTmbTmlCells).isFalse()
    }

    @Test
    fun `Should show warning in case the date of the molecular test is before the oldest version date of this test`() {
        val date = LocalDate.of(2023, 9, 19)
        val table = WgsSummaryGeneratorFunctions.createMolecularSummaryTable(
            SummaryType.DETAILS,
            TestPatientFactory.createProperTestPatientRecord(),
            molecularRecord.copy(
                date = date,
                targetSpecification = PanelTargetSpecification(emptyMap(), TestVersion(date.plusYears(1), true))
            ),
            wgsMolecular = null,
            1.0F,
            1.0F,
            MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(
                TestMolecularFactory.createMinimalWholeGenomeTest().drivers,
                emptyList()
            )
        )
        assertThat(
            CellTestUtil.extractTextFromCell(
                table.getCell(
                    0,
                    0
                )
            )
        ).isEqualTo("The date of this test (2023-09-19) is older than the date of the oldest version of the test for which we could derive which genes were tested (2024-09-19). This version is still used to determine which genes were tested. This determination is potentially not correct.")
    }

    @Test
    fun `Should not include HLA-A row in panel summary table when no immunology generator is provided`() {
        val table = WgsSummaryGeneratorFunctions.createMolecularSummaryTable(
            SummaryType.DETAILS,
            TestPatientFactory.createProperTestPatientRecord(),
            TestMolecularFactory.createMinimalPanelTest(),
            wgsMolecular = null,
            100f,
            200f,
            MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(
                TestMolecularFactory.createMinimalPanelTest().drivers, emptyList()
            )
        )

        val hasHlaRow = (0 until table.numberOfRows).any { row ->
            CellTestUtil.extractTextFromCell(table.getCell(row, 0)) == "HLA-A"
        }
        assertThat(hasHlaRow).isFalse()
    }

    @Test
    fun `Should show no HLA-A alleles detected inline when alleles list is empty`() {
        val panelMolecular = TestMolecularFactory.createMinimalPanelTest().copy(
            immunology = MolecularImmunology(isReliable = true, hlaAlleles = emptySet())
        )
        val immunologyGenerator = ImmunologyGenerator(panelMolecular, ImmunologyDisplayMode.DETAILED_INLINE, "Immunology", 100f, 200f)

        val table = WgsSummaryGeneratorFunctions.createMolecularSummaryTable(
            SummaryType.DETAILS,
            TestPatientFactory.createProperTestPatientRecord(),
            panelMolecular,
            wgsMolecular = null,
            100f,
            200f,
            MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(panelMolecular.drivers, emptyList()),
            immunologyGenerator
        )

        val hlaRowIndex = (0 until table.numberOfRows).first { row ->
            CellTestUtil.extractTextFromCell(table.getCell(row, 0)) == "HLA-A"
        }
        assertThat(CellTestUtil.extractTextFromCell(table.getCell(hlaRowIndex, 1)))
            .isEqualTo("No HLA-A alleles detected")
    }

    @Test
    fun `Should include HLA-A content inline in panel summary table when immunology generator is provided`() {
        val allele = HlaAllele(
            gene = "HLA-A",
            alleleGroup = "01",
            hlaProtein = "01",
            tumorCopyNumber = 2.0,
            hasSomaticMutations = false,
            evidence = ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = emptySet()),
            event = "HLA-A*01:01"
        )
        val panelMolecular = TestMolecularFactory.createMinimalPanelTest().copy(
            immunology = MolecularImmunology(isReliable = true, hlaAlleles = setOf(allele))
        )
        val immunologyGenerator = ImmunologyGenerator(panelMolecular, ImmunologyDisplayMode.DETAILED_INLINE, "Immunology", 100f, 200f)

        val table = WgsSummaryGeneratorFunctions.createMolecularSummaryTable(
            SummaryType.DETAILS,
            TestPatientFactory.createProperTestPatientRecord(),
            panelMolecular,
            wgsMolecular = null,
            100f,
            200f,
            MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(panelMolecular.drivers, emptyList()),
            immunologyGenerator
        )

        val hlaRowIndex = (0 until table.numberOfRows).first { row ->
            CellTestUtil.extractTextFromCell(table.getCell(row, 0)) == "HLA-A"
        }
        assertThat(CellTestUtil.extractTextFromCell(table.getCell(hlaRowIndex, 1)))
            .isEqualTo("HLA-A*01:01, tumor copy nr: 2, mutated: No")
    }
}