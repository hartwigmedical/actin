package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WGSSummaryGeneratorFunctionsTest {

    private val molecularRecord = TestMolecularFactory.createProperTestMolecularRecord()
    private val inconclusivePredictions = listOf(
        CupPrediction(
            cancerType = "Melanoma",
            likelihood = 0.60,
            snvPairwiseClassifier = 0.979,
            genomicPositionClassifier = 0.99,
            featureClassifier = 0.972,
        ),
        CupPrediction(
            cancerType = "Lung",
            likelihood = 0.20,
            snvPairwiseClassifier = 0.0009,
            genomicPositionClassifier = 0.011,
            featureClassifier = 0.0102
        ),
    )
    private val inconclusiveCharacteristics =
        molecularRecord.characteristics.copy(predictedTumorOrigin = PredictedTumorOrigin(inconclusivePredictions))

    @Test
    fun `Should return events concatenated and with warning string`() {
        val drivers = listOf(
            TestCopyNumberFactory.createMinimal().copy(event = "event 1", driverLikelihood = null, minCopies = 4),
            TestFusionFactory.createMinimal().copy(event = "event 2", driverLikelihood = null),
            TestFusionFactory.createMinimal().copy(event = "event 3", driverLikelihood = DriverLikelihood.LOW),
            TestFusionFactory.createMinimal().copy(event = "event 4", driverLikelihood = DriverLikelihood.MEDIUM),
            TestFusionFactory.createMinimal().copy(event = "event 5", driverLikelihood = DriverLikelihood.HIGH)
        )
        val cell = WGSSummaryGeneratorFunctions.potentiallyActionableEventsCell(drivers)

        assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("event 1 (4 copies - no amplification or deletion), event 2 (dubious quality), event 3 (low driver likelihood), " +
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
}