package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.jupiter.api.Test

class HasHadRadiotherapyToSomeBodyLocationTest {

    private val radiotherapy = setOf(treatment("Radiotherapy", isSystemic = false, categories = setOf(TreatmentCategory.RADIOTHERAPY)))
    private val targetBodyLocation = setOf("Spleen")
    private val targetBodyLocationInLargerString = setOf("Lower spleen")
    private val wrongBodyLocation = setOf("Bladder")
    private val function = HasHadRadiotherapyToSomeBodyLocation(targetBodyLocation.iterator().next(), 2)

    @Test
    fun `Should pass if radiotherapy with target body location in oncological history and sufficient lines`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = targetBodyLocation),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = targetBodyLocation),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = wrongBodyLocation)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)),
            "Prior radiotherapy to Spleen for at least 2 lines in provided treatments"
        )
    }

    @Test
    fun `Should pass if substring of radiotherapy location matches to target body location and sufficient lines`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = targetBodyLocationInLargerString),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = targetBodyLocation)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)),
            "Prior radiotherapy to Spleen for at least 2 lines in provided treatments"
        )
    }

    @Test
    fun `Should evaluate to undetermined if radiotherapy in oncological history but body location not defined`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = null),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = wrongBodyLocation)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)),
            "Radiotherapy in provided treatments but undetermined if target location was Spleen"
        )
    }

    @Test
    fun `Should fail if radiotherapy with target body location in oncological history but insufficient lines`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = targetBodyLocation)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)),
            "No prior radiation therapy to Spleen for at least 2 lines in provided treatments"
        )
    }

    @Test
    fun `Should fail if radiotherapy in oncological history but wrong body location`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = radiotherapy, bodyLocations = wrongBodyLocation
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history)),
            "No prior radiation therapy to Spleen for at least 2 lines in provided treatments"
        )
    }

    @Test
    fun `Should fail if oncological history does not contain radiotherapy`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(treatment("Chemotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history)),
            "No prior radiation therapy to Spleen for at least 2 lines in provided treatments"
        )
    }

    @Test
    fun `Should fail if oncological history empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())),
            "No prior radiation therapy to Spleen for at least 2 lines in provided treatments"
        )
    }
}