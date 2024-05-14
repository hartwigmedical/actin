package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.tumor.TestTumorFactory
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadBrainRadiationTherapyTest {

    private val radiotherapy = setOf(Radiotherapy("Radiotherapy"))

    @Test
    fun `Should pass if radiotherapy with body location brain in oncological history`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = radiotherapy, bodyLocationCategory = setOf(BodyLocationCategory.BRAIN)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasHadBrainRadiationTherapy().evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history))
        )
    }

    @Test
    fun `Should evaluate to undetermined if radiotherapy and brain metastases in history but radiotherapy location not specifically brain`() {
        val history =
            TestTumorFactory.withCnsOrBrainLesionsAndOncologicalHistory(
                hasCnsLesions = true, hasBrainLesions = true,
                TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocationCategory = null)
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadBrainRadiationTherapy().evaluate(history)
        )
    }

    @Test
    fun `Should fail if no radiotherapy in history`() {
        val treatment = setOf(treatment("Chemotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)))
        val history =
            TestTumorFactory.withCnsOrBrainLesionsAndOncologicalHistory(
                hasCnsLesions = true, hasBrainLesions = true,
                TreatmentTestFactory.treatmentHistoryEntry(treatments = treatment, bodyLocationCategory = null)
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadBrainRadiationTherapy().evaluate(history)
        )
    }

    @Test
    fun `Should fail if radiotherapy in history but no brain metastases`() {
        val history =
            TestTumorFactory.withCnsOrBrainLesionsAndOncologicalHistory(
                hasCnsLesions = true, hasBrainLesions = false,
                TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocationCategory = null)
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadBrainRadiationTherapy().evaluate(history)
        )
    }

    @Test
    fun `Should fail if oncological history is empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadBrainRadiationTherapy().evaluate(withTreatmentHistory(emptyList()))
        )
    }
}