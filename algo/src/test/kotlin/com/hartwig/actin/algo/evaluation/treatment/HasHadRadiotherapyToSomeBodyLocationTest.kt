package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadRadiotherapyToSomeBodyLocationTest {

    private val radiotherapy = setOf(treatment("Radiotherapy", isSystemic = false, categories = setOf(TreatmentCategory.RADIOTHERAPY)))
    private val TARGET_BODY_LOCATION = setOf("Spleen")
    private val TARGET_BODY_LOCATION_IN_LARGER_STRING = setOf("Lower spleen")
    private val WRONG_BODY_LOCATION = setOf("Bladder")
    private val function = HasHadRadiotherapyToSomeBodyLocation(TARGET_BODY_LOCATION.iterator().next())

    @Test
    fun `Should pass if radiotherapy with target body location in oncological history`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = TARGET_BODY_LOCATION),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = WRONG_BODY_LOCATION)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if substring of radiotherapy location matches to target body location`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = TARGET_BODY_LOCATION_IN_LARGER_STRING),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = WRONG_BODY_LOCATION)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should evaluate to undetermined if radiotherapy in oncological history but body location not defined`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = null),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocations = WRONG_BODY_LOCATION)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if radiotherapy in oncological history but wrong body location`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = radiotherapy, bodyLocations = WRONG_BODY_LOCATION
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history))
        )
    }

    @Test
    fun `Should fail if oncological history does not contain radiotherapy`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(treatment("Chemotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history))
        )
    }

    @Test
    fun `Should fail if oncological history empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }
}