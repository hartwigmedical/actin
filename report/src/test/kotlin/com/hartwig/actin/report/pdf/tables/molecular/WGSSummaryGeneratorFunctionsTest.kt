package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil
import org.assertj.core.api.Assertions
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
        TestMolecularFactory.createProperTestCharacteristics()
            .copy(predictedTumorOrigin = TestMolecularFactory.createProperPredictedTumorOrigin().copy(inconclusivePredictions))

    @Test
    fun `Should return events concatenated and with warning string`() {
        val drivers = listOf(
            TestCopyNumberFactory.createMinimal().copy(event = "event 1", driverLikelihood = null),
            TestFusionFactory.createMinimal().copy(event = "event 2", driverLikelihood = null),
            TestFusionFactory.createMinimal().copy(event = "event 3", driverLikelihood = DriverLikelihood.MEDIUM)
        )
        val cell = WGSSummaryGeneratorFunctions.potentiallyActionableEventsCell(drivers)
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("event 1, event 2 (dubious quality), event 3 (medium driver likelihood)")
    }

    @Test
    fun `Should return none when list of events is empty`() {
        val drivers = emptyList<Driver>()
        val cell = WGSSummaryGeneratorFunctions.potentiallyActionableEventsCell(drivers)
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("None")
    }

    @Test
    fun `Should return correct tumor origin prediction string in case conclusive and quality and purity sufficient`() {
        val cell = WGSSummaryGeneratorFunctions.tumorOriginPredictionCell(molecular = molecularRecord)
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("Melanoma (100%)")
    }

    @Test
    fun `Should return correct tumor origin prediction string in case inconclusive and quality and purity sufficient`() {
        val cell =
            WGSSummaryGeneratorFunctions.tumorOriginPredictionCell(molecular = molecularRecord.copy(characteristics = inconclusiveCharacteristics))
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("Inconclusive (Melanoma 60%, Lung 20%)")
    }

    @Test
    fun `Should return correct tumor origin prediction string in case conclusive and quality sufficient but purity insufficient`() {
        val cell = WGSSummaryGeneratorFunctions.tumorOriginPredictionCell(molecular = molecularRecord.copy(hasSufficientPurity = false))
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("Melanoma (100%) (low purity)")
    }

    @Test
    fun `Should return correct tumor origin prediction string in case inconclusive and quality sufficient but purity insufficient`() {
        val cell = WGSSummaryGeneratorFunctions.tumorOriginPredictionCell(
            molecular = molecularRecord.copy(characteristics = inconclusiveCharacteristics).copy(hasSufficientPurity = false)
        )
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("Inconclusive (Melanoma 60%, Lung 20%) (low purity)")
    }

    @Test
    fun `Should return correct tumor origin prediction string in case conclusive but quality and purity insufficient`() {
        val cell = WGSSummaryGeneratorFunctions.tumorOriginPredictionCell(
            molecular = molecularRecord.copy(
                hasSufficientPurity = false,
                hasSufficientQuality = false
            )
        )
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("Unknown")
    }

    @Test
    fun `Should return correct tumor origin prediction string in case inconclusive but quality and purity insufficient`() {
        val cell = WGSSummaryGeneratorFunctions.tumorOriginPredictionCell(
            molecular = molecularRecord.copy(characteristics = inconclusiveCharacteristics)
                .copy(hasSufficientPurity = false, hasSufficientQuality = false)
        )
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("Unknown")
    }

    @Test
    fun `Should return correct tumor origin prediction string in case there is no prediction in molecular record`() {
        val cell = WGSSummaryGeneratorFunctions.tumorOriginPredictionCell(
            molecular = TestMolecularFactory.createMinimalTestMolecularRecord()
        )
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("Unknown")
    }
}