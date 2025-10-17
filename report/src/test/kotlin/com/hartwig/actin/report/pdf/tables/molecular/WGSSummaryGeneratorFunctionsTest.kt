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
import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.pdf.SummaryType
import com.hartwig.actin.report.pdf.util.Tables
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WGSSummaryGeneratorFunctionsTest {

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
        val cell = WGSSummaryGeneratorFunctions.potentiallyActionableEventsCell(drivers, 2.5)

        assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo(
                "event 1 (4 copies - with tumor ploidy 2.5), event 2 (annotated as not a driver), event 3 (low driver likelihood), " +
                        "event 4 (medium driver likelihood), event 5"
            )
    }

    @Test
    fun `Should return none when list of events is empty`() {
        val drivers = emptyList<Driver>()
        val cell = WGSSummaryGeneratorFunctions.potentiallyActionableEventsCell(drivers)

        assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("None")
    }

    @Test
    fun `Should add '(low purity)' to predicted tumor origin when conclusive with sufficient quality and insufficient purity`() {
        val cell = WGSSummaryGeneratorFunctions.tumorOriginPredictionCell(molecular = molecularRecord.copy(hasSufficientPurity = false))

        assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("Melanoma (100%) (low purity)")
    }

    @Test
    fun `Should add '(low purity)' to predicted tumor origin when inconclusive with sufficient quality and insufficient purity`() {
        val cell = WGSSummaryGeneratorFunctions.tumorOriginPredictionCell(
            molecular = molecularRecord.copy(characteristics = inconclusiveCharacteristics).copy(hasSufficientPurity = false)
        )

        assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("Inconclusive (Melanoma 60%, Lung 20%) (low purity)")
    }

    @Test
    fun `Should not create tumor mutational cell when result is unknown and summary table configuration is not long summary type`() {
        val record = molecularRecord.copy(
            characteristics = molecularRecord.characteristics.copy(tumorMutationalLoad = null, tumorMutationalBurden = null)
        )
        val hasTmbTmlCells = WGSSummaryGeneratorFunctions.createTmbCells(record, false, Tables.createFixedWidthCols(100f, 100f))
        assertThat(hasTmbTmlCells).isFalse()
    }

    @Test
    fun `Should show warning in case the date of the molecular test is before the oldest version date of this test`() {
        val table = WGSSummaryGeneratorFunctions.createMolecularSummaryTable(
            SummaryType.DETAILS,
            TestPatientFactory.createProperTestPatientRecord(),
            molecularRecord.copy(targetSpecification = PanelTargetSpecification(emptyMap(), testDateIsBeforeOldestTestVersion = true)),
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
        ).isEqualTo("The date of this test is before the oldest version date of this test, the oldest version of the test is used to determine the tested genes")
    }
}